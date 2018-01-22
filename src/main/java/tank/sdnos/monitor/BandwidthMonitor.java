package tank.sdnos.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import sun.rmi.runtime.Log;
import tank.sdnos.monitor.CommonUse.NoDirectLink;
import tank.sdnos.monitor.web.BandwidthMonitorRest;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.linkdiscovery.internal.LinkInfo;
import net.floodlightcontroller.restserver.IRestApiService;

/**
 * 带宽获取模块
 *
 * @author gzq
 */
public class BandwidthMonitor implements IFloodlightModule, IBandwidthMonitor {
    private static final Logger log = LoggerFactory.getLogger(BandwidthMonitor.class);
    private static IStatisticsService statisticsService;
    // Floodllight实现的线程池，当然我们也可以使用Java自带的，但推荐使用这个
    private static IThreadPoolService threadPoolService;
    private static ILinkDiscoveryService linkDiscoveryService;
    private static IOFSwitchService ofSwitchService;
    private static IRestApiService restApiService;

    // Future类，不明白的可以百度 Java现成future,其实C++11也有这个玩意了
    private static ScheduledFuture<?> bandwidthMonitor;
    private static Map<NodePortTuple, SwitchPortBandwidth> bandwidth;
    private static Map<NoDirectLink, Long> allLinkSpeed = new HashMap<NoDirectLink, Long>();
    private static Map<NoDirectLink, Float> allLinkUsage = new HashMap<NoDirectLink, Float>();
    private static int TOP = 3;
    private static volatile LinkSpeed[] topNSpeedLinks = new LinkSpeed[TOP];
    private static volatile LinkUsage[] topNUsageLinks = new LinkUsage[TOP];
    /*
     * equals to net.floodlightcontroller.statistics.StatisticsCollector
     * .collectionIntervalPortStatsSeconds
     */
    private static int statsUpdateInterval = 5;
    private static boolean isEnabled = true;

    // 告诉FL，我们添加了一个模块，提供了IMonitorBandwidthService
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IBandwidthMonitor.class);
        return l;
    }

    // 我们前面声明了几个需要使用的service,在这里说明一下实现类
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> l = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        l.put(IBandwidthMonitor.class, this);
        return l;
    }

    // 告诉FL我们以来那些服务，以便于加载
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IStatisticsService.class);
        l.add(IThreadPoolService.class);
        l.add(IRestApiService.class);
        return l;
    }

    // 初始化这些service,个人理解这个要早于startUp()方法的执行，验证很简单，在两个方法里打印当前时间就可以。
    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        statisticsService = context.getServiceImpl(IStatisticsService.class);
        threadPoolService = context.getServiceImpl(IThreadPoolService.class);
        ofSwitchService = context.getServiceImpl(IOFSwitchService.class);
        linkDiscoveryService = context.getServiceImpl(ILinkDiscoveryService.class);
        restApiService = context.getServiceImpl(IRestApiService.class);

        Map<String, String> config = context
                .getConfigParams(net.floodlightcontroller.statistics.StatisticsCollector.class);
        if (config.containsKey("collectionIntervalPortStatsSecond")) {
            try {
                statsUpdateInterval = Integer.parseInt(config.get("collectionIntervalPortStatsSecond").trim());
            } catch (Exception e) {
                log.error(
                        "tank# Could not parse collectionIntervalPortStatsSecond parameter in net.floodlightcontroller"
                                + ".statistics.StatisticsCollector. Bandwidth stats update interval will be set to default {}",
                        statsUpdateInterval);
            }
        }
        log.info("tank# bandwidth statistics collection interval set to {}s", statsUpdateInterval);

        config = context.getConfigParams(this);
        if (config.containsKey("enable")) {
            try {
                isEnabled = Boolean.parseBoolean(config.get("enable").trim());
            } catch (Exception e) {
                log.error("Could not parse '{}'. Using default of {}", "enable", isEnabled);
            }
        }
        log.info("tank# bandwidth monitor {}", isEnabled ? "enabled" : "disabled");
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        if (isEnabled) {
            restApiService.addRestletRoutable(new BandwidthMonitorRest());
            startBandwidthMonitor();
            log.info("tank# BandwidthMonitor is in service");
        }

    }

    // 自定义的开始收集数据的方法，使用了线程池，定周期的执行
    private synchronized void startBandwidthMonitor() {
        bandwidthMonitor = threadPoolService.getScheduledExecutor().scheduleAtFixedRate(new BandwidthUpdateThread(),
                statsUpdateInterval, statsUpdateInterval, TimeUnit.SECONDS);
        log.info("tank# bandwidth monitor thread(s) started");
    }

    // 自定义的线程类，在上面的方法中实例化，并被调用
    /**
     * Single thread for collecting switch statistics and containing the reply.
     */
    private class BandwidthUpdateThread extends Thread implements Runnable {
        @Override
        public void run() {
            bandwidth = statisticsService.getBandwidthConsumption();
            Map<Link, LinkInfo> linksInfo = linkDiscoveryService.getLinks();

            log.debug("tank# the size of linksInfo is: {}", linksInfo.size());

            /**
             * the link speed and link usage is calculate in the unit of no
             * direction link But if you want to calculate in direction link,
             * you need to change the speed and usage calculate methods below
             */
            Set<NoDirectLink> noDirectLinks = new HashSet<NoDirectLink>();
            noDirectLinks = CommonUse.getNoDirectionLinksSet(linksInfo);
            log.debug("tank# the size of noDirectLinks is: {}", noDirectLinks.size());

            for (NoDirectLink link : noDirectLinks) {
                DatapathId srcSw = link.getSrc();
                DatapathId dstSw = link.getDst();
                OFPort srcPort = link.getSrcPort();
                OFPort dstPort = link.getDstPort();

                NodePortTuple srcNode = new NodePortTuple(srcSw, srcPort);
                NodePortTuple dstNode = new NodePortTuple(dstSw, dstPort);

                Long srcPortSpeed = convertSwitchPortBandwidthToSpeed(bandwidth.get(srcNode));
                Long dstPortSpeed = convertSwitchPortBandwidthToSpeed(bandwidth.get(dstNode));

                if (srcPortSpeed != null && dstPortSpeed != null) {
                    long linkSpeed = 0;
                    /*
                     * because the rx/tx speed in peer ports are not equal, so
                     * will use the average as the link speed
                     */
                    linkSpeed = (srcPortSpeed + dstPortSpeed) / 2;
                    allLinkSpeed.put(link, new Long(linkSpeed));
                    long linkBandwidth = 0;
                    long srcMaxPortSpeed = ofSwitchService.getSwitch(srcSw).getPort(srcPort).getMaxSpeed();
                    long dstMaxPortSpeed = ofSwitchService.getSwitch(dstSw).getPort(dstPort).getMaxSpeed();
                    if (srcMaxPortSpeed >= dstMaxPortSpeed) {
                        linkBandwidth = dstMaxPortSpeed;
                    } else {
                        linkBandwidth = srcMaxPortSpeed;
                    }

                    float linkUsage = 0;
                    linkBandwidth = 35000000L;
                    linkUsage = linkBandwidth != 0 ? (linkSpeed / (float) linkBandwidth) : 0;
                    allLinkUsage.put(link, new Float(linkUsage));
                } else {
                    /*
                     * we did not cat the port speed , so we will it from the
                     * Map
                     */
                    allLinkSpeed.remove(link);
                    allLinkUsage.remove(link);
                }
            }
            testBandwidthMonitor();
        }
    }

    @Override
    public Map<NodePortTuple, SwitchPortBandwidth> getBandwidthMap() {
        // TODO Auto-generated method stub
        return bandwidth;
    }

    @Override
    public SwitchPortBandwidth getPortBandwidth(NodePortTuple nodePortTuple) {
        return bandwidth.get(nodePortTuple);
    }

    /* unit Mbits/s */
    @Override
    public Long getPortSpeed(NodePortTuple nodePortTuple) {
        SwitchPortBandwidth switchPortBand = bandwidth.get(nodePortTuple);
        Long bdwth = null;
        bdwth = convertSwitchPortBandwidthToSpeed(switchPortBand);
        return bdwth;
    }

    /* link related */
    @Override
    public Long getNoDirectLinkSpeed(NoDirectLink noDirectLink) {
        return allLinkSpeed.get(noDirectLink);
    }

    @Override
    public Long getNoDirectLinkSpeed(Link link) {
        NoDirectLink noDirectLink = new NoDirectLink(link.getSrc(), link.getSrcPort(), link.getDst(),
                link.getDstPort());
        return allLinkSpeed.get(noDirectLink);
    }

    @Override
    public Long getNoDirectLinkSpeed(DatapathId srcSw, int srcPort, DatapathId dstSw, int dstPort) {
        NoDirectLink noDirectLink = new NoDirectLink(srcSw, OFPort.of(srcPort), dstSw, OFPort.of(dstPort));
        return allLinkSpeed.get(noDirectLink);
    }

    @Override
    public Map<NoDirectLink, Long> getAllNoDirectLinkSpeed() {
        if (allLinkSpeed.size() == 0) {
            return null;
        }
        return allLinkSpeed;
    }

    @Override
    public Float getNoDirectLinkUsage(NoDirectLink noDirectLink) {
        if (allLinkUsage.size() == 0) {
            return null;
        }
        return allLinkUsage.get(noDirectLink);
    }

    @Override
    public Float getNoDirectLinkUsage(Link link) {
        NoDirectLink noDirectLink = CommonUse.getNoDirectionLink(link);
        return allLinkUsage.get(noDirectLink);
    }

    @Override
    public Float getNoDirectLinkUsage(DatapathId srcSw, int srcPort, DatapathId dstSw, int dstPort) {
        NoDirectLink noDirectLink = new NoDirectLink(srcSw, OFPort.of(srcPort), dstSw, OFPort.of(dstPort));
        return allLinkUsage.get(noDirectLink);
    }

    @Override
    public Map<NoDirectLink, Float> getAllNoDirectLinkUsage() {
        return allLinkUsage;
    }

    /* unit Kbits/s */
    private Long convertSwitchPortBandwidthToSpeed(SwitchPortBandwidth swtichPortBandwidth) {
        Long bdwth = null;
        if (swtichPortBandwidth != null) {
            bdwth = (swtichPortBandwidth.getBitsPerSecondRx().getValue()
                    + swtichPortBandwidth.getBitsPerSecondTx().getValue()) / 1024;
            return bdwth;
        }
        return bdwth;
    }

    @Override
    public Long getMaxNoDirectLinkSpeed() {
        long maxLinkSpeed = 0;
        for (Link link : allLinkSpeed.keySet()) {
            if (allLinkSpeed.get(link) > maxLinkSpeed) {
                maxLinkSpeed = allLinkSpeed.get(link);
            }
        }
        return maxLinkSpeed;
    }

    @Override
    public Float getMaxNoDirectLinkUsage() {
        float maxLinkUsage = 0;
        for (Link link : allLinkUsage.keySet()) {
            if (allLinkUsage.get(link) > maxLinkUsage) {
                maxLinkUsage = allLinkUsage.get(link);
            }
        }

        return maxLinkUsage;
    }

    @Override
    public LinkSpeed getMaxNoDirectLinkSpeedDetail() {
        long maxLinkSpeed = 0;
        Link idealLink = null;
        for (Link link : allLinkSpeed.keySet()) {
            if (allLinkSpeed.get(link) >= maxLinkSpeed) {
                maxLinkSpeed = allLinkSpeed.get(link);
                idealLink = link;
            }
        }
        if (idealLink != null) {
            LinkSpeed linkStatis = new LinkSpeed(idealLink, maxLinkSpeed);
            return linkStatis;
        } else {
            return null;
        }
    }

    @Override
    public LinkUsage getMaxNoDirectLinkUsageDetail() {
        float maxLinkUsage = 0;
        Link idealLink = null;
        for (Link link : allLinkUsage.keySet()) {
            if (allLinkUsage.get(link) >= maxLinkUsage) {
                maxLinkUsage = allLinkUsage.get(link);
                idealLink = link;
            }
        }
        if (idealLink != null) {
            LinkUsage linkStatis = new LinkUsage(idealLink, maxLinkUsage);
            return linkStatis;
        } else {
            return null;
        }
    }

    /*
     * one class type to store the Link,linkSpeed or Link, linkUsage information
     */
    public static class LinkSpeed {
        private Link link;
        private Long linkSpeed;

        LinkSpeed(Link link, Long linkSpeed) {
            this.link = link;
            this.linkSpeed = linkSpeed;
        }

        public Link getLink() {
            return link;
        }

        public void setLink(Link link) {
            this.link = link;
        }

        public Long getLinkSpeed() {
            return linkSpeed;
        }

        public void setLinkSpeed(Long linkSpeed) {
            this.linkSpeed = linkSpeed;
        }
    }

    public static class LinkUsage {
        private Link link;
        private Float linkUsage;

        public LinkUsage(Link link, Float linkUsage) {
            this.link = link;
            this.linkUsage = linkUsage;
        }

        public Link getLink() {
            return link;
        }

        public void setLink(Link link) {
            this.link = link;
        }

        public Float getLinkUsage() {
            return linkUsage;
        }

        public void setLinkUsage(Float linkUsage) {
            this.linkUsage = linkUsage;
        }

    }

    public void testBandwidthMonitor() {
        LinkSpeed maxLinkSpeed = getMaxNoDirectLinkSpeedDetail();
        if (maxLinkSpeed != null) {
            log.debug("tank# the max link speed is: {}", maxLinkSpeed.getLinkSpeed());
        } else {
            log.debug("tank# max link speed is null");
        }

        LinkUsage maxLinkUsage = getMaxNoDirectLinkUsageDetail();
        if (maxLinkUsage != null) {
            log.debug("tank# the max link usage is: {}", maxLinkUsage.getLinkUsage());
        } else {
            log.debug("tank# max link usage is null");
        }

        LinkSpeed[] top3LinkSpeed = getTopNSpeedNoDirectLinks();
        if (top3LinkSpeed != null) {
            for (int i = 0; i < top3LinkSpeed.length; i++) {
                if (top3LinkSpeed[i] != null) {
                    log.info("tank# top {} link speed is: {}", i + 1, top3LinkSpeed[i].getLinkSpeed());
                }
            }
        } else {
            log.info("tank# no link speed statistics is got now");
        }
        LinkUsage[] top3LinkUsage = getTopNUsageNoDirectLinks();
        if (top3LinkUsage != null) {
            for (int i = 0; i < top3LinkUsage.length; i++) {
                if (top3LinkUsage[i] != null) {
                    log.info("tank# top {} link usage is: {}", i + 1, top3LinkUsage[i].getLinkUsage());
                }
            }
        } else {
            log.info("tank# no link usage statistics is got now");
        }
    }

    /**
     * return the top N linkstatis of max link speed
     */
    @Override
    public LinkSpeed[] getTopNSpeedNoDirectLinks() {
        // TODO Auto-generated method stub
        List<Entry<NoDirectLink, Long>> sortedList = CommonUse.sortByValue(allLinkSpeed);
        log.info("tank# sortedList size : {}", sortedList.size());
        if (sortedList.size() == 0) {
            return null;
        }
        int i = 0;

        try {
            for (i = 0; i < TOP; i++) {
                Entry<NoDirectLink, Long> link = sortedList.get(i);
                LinkSpeed linkStatis = new LinkSpeed(link.getKey(), link.getValue());
                topNSpeedLinks[i] = linkStatis;
            }

        } catch (IndexOutOfBoundsException e) {
            for (int j = i; j < TOP; j++) {
                topNSpeedLinks[j] = null;
            }
        }

        return topNSpeedLinks;
    }

    /**
     * return the top N linkStatis of max link usage
     */
    @Override
    public LinkUsage[] getTopNUsageNoDirectLinks() {
        // TODO Auto-generated method stub
        List<Entry<NoDirectLink, Float>> sortedList = CommonUse.sortByValue(allLinkUsage);
        if (sortedList.size() == 0) {
            return null;
        }
        int i = 0;

        try {
            for (i = 0; i < TOP; i++) {
                Entry<NoDirectLink, Float> link = sortedList.get(i);
                LinkUsage linkStatis = new LinkUsage(link.getKey(), link.getValue());
                topNUsageLinks[i] = linkStatis;
            }

        } catch (IndexOutOfBoundsException e) {
            for (int j = i; j < TOP; j++) {
                topNUsageLinks[j] = null;
            }
        }

        return topNUsageLinks;
    }

    @Override
    public LinkUsage[] getAllDescendUsageNoDirectLinks() {
        List<Entry<NoDirectLink, Float>> sortedList = CommonUse.sortByValue(allLinkUsage);
        if (sortedList.size() == 0) {
            return null;
        }
        LinkUsage[] descendNoDirectLinkUsage = new LinkUsage[sortedList.size()];

        for (int i = 0; i < sortedList.size(); i++) {
            Entry<NoDirectLink, Float> link = sortedList.get(i);
            LinkUsage linkStatis = new LinkUsage(link.getKey(), link.getValue());
            descendNoDirectLinkUsage[i] = linkStatis;
        }

        return descendNoDirectLinkUsage;
    }

    @Override
    public LinkUsage[] getAllAscendUsageNoDirectLinks() {

        List<Entry<NoDirectLink, Float>> sortedList = CommonUse.sortByValue(allLinkUsage);
        if (sortedList.size() == 0) {
            return null;
        }
        LinkUsage[] ascendNoDirectLinkUsage = new LinkUsage[sortedList.size()];

        int listLength = sortedList.size();

        for (int i = listLength - 1; i >= 0; i--) {
            Entry<NoDirectLink, Float> link = sortedList.get(i);
            LinkUsage linkStatis = new LinkUsage(link.getKey(), link.getValue());
            ascendNoDirectLinkUsage[listLength - i - 1] = linkStatis;
        }

        return ascendNoDirectLinkUsage;
    }

    @Override
    public LinkSpeed[] getAllDescendSpeedNoDirectLinks() {
        // TODO Auto-generated method stub
        List<Entry<NoDirectLink, Long>> sortedList = CommonUse.sortByValue(allLinkSpeed);
        if (sortedList.size() == 0) {
            return null;
        }
        LinkSpeed[] descendNoDirectLinkSpeed = new LinkSpeed[sortedList.size()];

        for (int i = 0; i < sortedList.size(); i++) {
            Entry<NoDirectLink, Long> link = sortedList.get(i);
            LinkSpeed linkStatis = new LinkSpeed(link.getKey(), link.getValue());
            descendNoDirectLinkSpeed[i] = linkStatis;
        }

        return descendNoDirectLinkSpeed;
    }

    @Override
    public LinkSpeed[] getAllAscendSpeedNoDirectLinks() {
        // TODO Auto-generated method stub
        List<Entry<NoDirectLink, Long>> sortedList = CommonUse.sortByValue(allLinkSpeed);

        if (sortedList.size() == 0) {
            return null;
        }
        LinkSpeed[] ascendNoDirectLinkSpeed = new LinkSpeed[sortedList.size()];
        int listLength = sortedList.size();

        for (int i = listLength - 1; i >= 0; i--) {
            Entry<NoDirectLink, Long> link = sortedList.get(i);
            LinkSpeed linkStatis = new LinkSpeed(link.getKey(), link.getValue());
            ascendNoDirectLinkSpeed[listLength - i - 1] = linkStatis;
        }

        return ascendNoDirectLinkSpeed;
    }

    @Override
    public Long getDirectLinkSpeed(Link link) {
        // TODO Auto-generated method stub

        DatapathId srcSw = link.getSrc();
        DatapathId dstSw = link.getDst();
        OFPort srcPort = link.getSrcPort();
        OFPort dstPort = link.getDstPort();
        NodePortTuple srcNodePort = new NodePortTuple(srcSw, srcPort);
        NodePortTuple dstNodePort = new NodePortTuple(dstSw, dstPort);
        SwitchPortBandwidth srcPortBandwitdh = bandwidth.get(srcNodePort);
        SwitchPortBandwidth dstPortBandwitdh = bandwidth.get(dstNodePort);

        if (srcPortBandwitdh != null && dstPortBandwitdh != null) {
            Long speed = (srcPortBandwitdh.getBitsPerSecondTx().getValue()
                    + dstPortBandwitdh.getBitsPerSecondRx().getValue()) / 2;
            return speed;
        } else {
            return null;
        }
    }

    @Override
    public Long getDirectLinkSpeed(DatapathId srcSw, int srcPort, DatapathId dstSw, int dstPort) {
        // TODO Auto-generated method stub
        NodePortTuple srcNodePort = new NodePortTuple(srcSw, OFPort.of(srcPort));
        NodePortTuple dstNodePort = new NodePortTuple(dstSw, OFPort.of(dstPort));
        SwitchPortBandwidth srcPortBandwitdh = bandwidth.get(srcNodePort);
        SwitchPortBandwidth dstPortBandwitdh = bandwidth.get(dstNodePort);

        if (srcPortBandwitdh != null && dstPortBandwitdh != null) {
            Long speed = (srcPortBandwitdh.getBitsPerSecondTx().getValue()
                    + dstPortBandwitdh.getBitsPerSecondRx().getValue()) / 2;
            return speed;
        } else {
            return null;
        }
    }

    @Override
    public Map<Link, Long> getAllDirectLinkSpeed() {
        // TODO Auto-generated method stuba
        Map<Link, Long> linksSpeed = new HashMap<Link, Long>();
        Map<Link, LinkInfo> linksInfo = linkDiscoveryService.getLinks();
        for (Link link : linksInfo.keySet()) {
            linksSpeed.put(link, getDirectLinkSpeed(link));
        }
        return linksSpeed;
    }

}
