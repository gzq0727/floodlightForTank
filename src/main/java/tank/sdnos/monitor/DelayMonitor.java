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
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.internal.LinkInfo;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import tank.sdnos.monitor.BandwidthMonitor.LinkUsage;
import tank.sdnos.monitor.CommonUse.NoDirectLink;

public class DelayMonitor implements IFloodlightModule, IDelayMonitor {
    private static final Logger log = LoggerFactory.getLogger(DelayMonitor.class);
    private static ILinkDiscoveryService linkDiscoveryService;
    private static IThreadPoolService threadPoolService;

    private static volatile Map<Link, LatencyLet> linksDelay = new HashMap<Link, LatencyLet>();

    private static volatile Map<NoDirectLink, LatencyLet> noDirectlinksDelay = new HashMap<NoDirectLink, LatencyLet>();

    private static int TOP = 2;
    private static LinkDelay[] topNDelayDirectLinks = new LinkDelay[TOP * 2];
    private static LinkDelay[] topNDelayNoDirectLinks = new LinkDelay[TOP];

    private static int statsUpdateInterval = 5;
    private static boolean isEnabled = true;

    /*
     * The latency data is obtained through the linkDiscoveryService implemented
     * by LinkDiscoveryManager
     *
     * @author gzq
     */

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IDelayMonitor.class);
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> l = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        l.put(IDelayMonitor.class, this);
        return l;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IThreadPoolService.class);
        l.add(IOFSwitchService.class);
        l.add(ILinkDiscoveryService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        linkDiscoveryService = context.getServiceImpl(ILinkDiscoveryService.class);
        threadPoolService = context.getServiceImpl(IThreadPoolService.class);
        Map<String, String> config = context
                .getConfigParams(net.floodlightcontroller.statistics.StatisticsCollector.class);
        if (config.containsKey("collectionIntervalPortStatsSecond")) {
            try {
                statsUpdateInterval = Integer.parseInt(config.get("collectionIntervalPortStatsSecond").trim());
            } catch (Exception e) {
                log.error(
                        "tank# Could not parse collectionIntervalPortStatsSecond parameter in net.floodlightcontroller"
                                + ".statistics.StatisticsCollector. Delay stats update interval will be set to default {}",
                        statsUpdateInterval);
            }
        }
        log.info("tank# delay statistics collection interval set to {}s", statsUpdateInterval);

        config = context.getConfigParams(this);
        if (config.containsKey("enable")) {
            try {
                isEnabled = Boolean.parseBoolean(config.get("enable").trim());
            } catch (Exception e) {
                log.error("Could not parse '{}'. Using default of {}", "enable", isEnabled);
            }
        }
        log.info("tank# delay monitor {}", isEnabled ? "enabled" : "disabled");
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        if (isEnabled) {
            startDelayMonitor();
            log.info("tank# DelayMonitor is in service");
        }
    }

    public void startDelayMonitor() {
        threadPoolService.getScheduledExecutor().scheduleAtFixedRate(new DelayCollectorThread(), statsUpdateInterval,
                statsUpdateInterval, TimeUnit.SECONDS);
    }

    private class DelayCollectorThread implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Map<Link, LinkInfo> linksInfo = linkDiscoveryService.getLinks();

            /* the link delay is calculate in the unit of direction link */
            Iterator<Entry<Link, LinkInfo>> iter = linksInfo.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Link, LinkInfo> link = iter.next();
                LatencyLet directLatencyLet = new LatencyLet();

                directLatencyLet.setLatency(link.getKey().getLatency().getValue());
                directLatencyLet.setCurrentLatency(link.getValue().getCurrentLatency().getValue());
                if (link.getValue().getLatencyHistoryAverageForTank() != null) {
                    directLatencyLet.setAverageLatency(link.getValue().getLatencyHistoryAverageForTank().getValue());
                } else {
                    directLatencyLet.setAverageLatency(null);
                }
                linksDelay.put(link.getKey(), directLatencyLet);

                /* set information about noDirectLinksDelay */
                NoDirectLink noDirectLink = CommonUse.getNoDirectionLink(link.getKey());
                if (!noDirectlinksDelay.containsKey(noDirectLink)) {

                    LatencyLet noDirectLatencyLet = new LatencyLet();
                    Link reverseLink = new Link();
                    reverseLink.setSrc(link.getKey().getDst());
                    reverseLink.setSrcPort(link.getKey().getDstPort());
                    reverseLink.setDst(link.getKey().getSrc());
                    reverseLink.setDstPort(link.getKey().getSrcPort());

                    for (Link linkDetail : linksInfo.keySet()) {
                        if (linkDetail.equals(reverseLink)) {
                            reverseLink = linkDetail;
                            break;
                        }
                    }

                    noDirectLatencyLet.setLatency(
                            (link.getKey().getLatency().getValue() + reverseLink.getLatency().getValue()) / 2);
                    noDirectLatencyLet.setCurrentLatency((link.getValue().getCurrentLatency().getValue()
                            + linksInfo.get(reverseLink).getCurrentLatency().getValue()) / 2);
                    if (link.getValue().getLatencyHistoryAverageForTank() != null) {
                        noDirectLatencyLet
                                .setAverageLatency((link.getValue().getLatencyHistoryAverageForTank().getValue()
                                        + linksInfo.get(reverseLink).getLatencyHistoryAverageForTank().getValue()) / 2);
                    } else {
                        noDirectLatencyLet.setAverageLatency(null);
                    }

                    noDirectlinksDelay.put(noDirectLink, noDirectLatencyLet);
                }

            }

            log.info("tank# the size of noDirectLinks is: {}", noDirectlinksDelay.size());

            testDelayMonitor();
        }
    }

    private void testDelayMonitor() {

        LinkDelay[] linkDelays = new LinkDelay[TOP];
        linkDelays = getTopNDelayDirectLinks();
        for (int i = 0; i < TOP * 2; i++) {
            if (linkDelays[i] != null) {
                log.info("tank# the top {} direct link delay is: {}", i + 1, linkDelays[i].getDelay());
            } else {
                log.info("tank# the top {} direct link delay is: {}", i + 1, null);
            }
        }

        linkDelays = getTopNDelayNoDirectLinks();
        for (int i = 0; i < TOP; i++) {
            if (linkDelays[i] != null) {
                log.info("tank# the top {} no direct link delay is: {}", i + 1, linkDelays[i].getDelay());
            } else {
                log.info("tank# the top {} no direct link delay is: {}", i + 1, null);
            }
        }

    }

    public static class LatencyLet implements Comparable {
        private Long latency;
        private Long currentLatency;
        private Long averageLatency;

        public Long getLatency() {
            return latency;
        }

        public void setLatency(Long latency) {
            this.latency = latency;
        }

        public Long getCurrentLatency() {
            return currentLatency;
        }

        public void setCurrentLatency(Long currentLatency) {
            this.currentLatency = currentLatency;
        }

        public Long getAverageLatency() {
            return averageLatency;
        }

        public void setAverageLatency(Long averageLatency) {
            this.averageLatency = averageLatency;
        }

        @Override
        public int compareTo(Object object) {
            // TODO Auto-generated method stub
            if (object == null) {
                throw new NullPointerException();
            }
            if (!(object instanceof LatencyLet)) {
                throw new ClassCastException();
            }
            if (this.getLatency() > ((LatencyLet) object).getLatency()) {
                return 1;
            } else if (this.getLatency() == ((LatencyLet) object).getLatency()) {
                return 0;
            } else {
                return -1;
            }

        }

    }

    /*
     * return the link latency caulate
     * by @net.floodlightcontroller.linkdiscovery.
     * internal.LinkInfo.addObservedLatency(U64 latency) depended on the average
     * latency and currentlatency
     */
    @Override
    public Long getDirectLatency(String srcSw, String srcPort, String dstSw, String dstPort) {
        Iterator<Entry<Link, LatencyLet>> iter = linksDelay.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Link, LatencyLet> link = iter.next();
            if (srcSw.equals(link.getKey().getSrc().toString()) && srcPort.equals(link.getKey().getSrcPort().toString())
                    && dstSw.equals(link.getKey().getDst().toString())
                    && dstPort.equals(link.getKey().getDstPort().toString())) {
                return link.getValue().getLatency();
            }
        }
        log.error("tank# can not find thd link: {}",
                new Object[] { "srcSw:" + srcSw, "srcPort:" + srcPort, "dstSw:" + dstSw, "dstPort" + dstPort });
        return null;
    }

    @Override
    public Long getDirectLatency(Link link) {
        for (Link mlink : linksDelay.keySet()) {
            if (mlink.equals(link)) {
                return mlink.getLatency().getValue();
            }
        }
        log.error("tank# can not find thd link: {}", "srcSw:" + link.getSrc() + " srcPort:" + link.getSrcPort()
                + " dstSw:" + link.getDst() + " dstPort" + link.getDstPort());
        return null;
    }

    /* return the link current latency = the last latency measured */
    @Override
    public Long getDirectCurrentLatency(String srcSw, String srcPort, String dstSw, String dstPort) {
        Iterator<Entry<Link, LatencyLet>> iter = linksDelay.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Link, LatencyLet> link = iter.next();
            if (srcSw.equals(link.getKey().getSrc().toString()) && srcPort.equals(link.getKey().getSrcPort().toString())
                    && dstSw.equals(link.getKey().getDst().toString())
                    && dstPort.equals(link.getKey().getDstPort().toString())) {
                return link.getValue().getCurrentLatency();
            }
        }
        log.error("tank# can not find thd link: {}",
                new Object[] { "srcSw:" + srcSw, "srcPort:" + srcPort, "dstSw:" + dstSw, "dstPort" + dstPort });
        return null;
    }

    @Override
    public Long getDirectCurrentLatency(Link link) {
        for (Link mlink : linksDelay.keySet()) {
            if (mlink.equals(link)) {
                return linksDelay.get(mlink).getCurrentLatency();
            }
        }
        log.error("tank# can not find thd link: {}", "srcSw:" + link.getSrc() + " srcPort:" + link.getSrcPort()
                + " dstSw:" + link.getDst() + " dstPort" + link.getDstPort());
        return null;
    }

    /*
     * return the link average latency calcute by
     * net.floodlightcontroller.linkdiscovery.internal.LinkInfo.
     * getLatencyHistoryAverageForTank()
     */
    @Override
    public Long getDirectAverageLatency(String srcSw, String srcPort, String dstSw, String dstPort) {
        Iterator<Entry<Link, LatencyLet>> iter = linksDelay.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Link, LatencyLet> link = iter.next();
            if (srcSw.equals(link.getKey().getSrc().toString()) && srcPort.equals(link.getKey().getSrcPort().toString())
                    && dstSw.equals(link.getKey().getDst().toString())
                    && dstPort.equals(link.getKey().getDstPort().toString())) {
                if (link.getValue().getAverageLatency() == null) {
                    log.info("tank# wait the latencyHistoryWindow to bu full, return null now");
                }
                return link.getValue().getAverageLatency();
            }
        }
        log.error("tank# can not find thd link: {}",
                new Object[] { "srcSw:" + srcSw, "srcPort:" + srcPort, "dstSw:" + dstSw, "dstPort" + dstPort });
        return null;
    }

    @Override
    public Long getDirectAverageLatency(Link link) {
        for (Link mlink : linksDelay.keySet()) {
            if (mlink.equals(link)) {
                return linksDelay.get(mlink).getAverageLatency();
            }
        }
        log.error("tank# can not find thd link: {}", "srcSw:" + link.getSrc() + " srcPort:" + link.getSrcPort()
                + " dstSw:" + link.getDst() + " dstPort" + link.getDstPort());
        return null;
    }

    public Map<Link, LatencyLet> getAllLatency() {
        return linksDelay;
    }

    public static class LinkDelay {
        private Link link;
        private Long delay;

        public Link getLink() {
            return link;
        }

        public void setLink(Link link) {
            this.link = link;
        }

        public Long getDelay() {
            return delay;
        }

        public void setDelay(Long delay) {
            this.delay = delay;
        }

        public LinkDelay(Link link, Long delay) {
            this.link = link;
            this.delay = delay;
        }
    }

    @Override
    public Long getMaxDirectLinkDelay() {
        // TODO Auto-generated method stub

        LinkDelay linkDelay = getMaxDirectLinkDelayDetail();

        if (linkDelay == null) {
            return null;
        } else {
            return linkDelay.getDelay();
        }
    }

    @Override
    public LinkDelay getMaxDirectLinkDelayDetail() {
        Long maxLinkDelay = new Long(0);
        Link idealLink = null;
        for (Link link : linksDelay.keySet()) {
            if (linksDelay.get(link).getLatency() >= maxLinkDelay) {
                maxLinkDelay = linksDelay.get(link).getLatency();
                idealLink = link;
            }
        }
        if (idealLink != null) {
            LinkDelay linkDelay = new LinkDelay(idealLink, maxLinkDelay);
            return linkDelay;
        } else {
            return null;
        }
    }

    @Override
    public LinkDelay[] getTopNDelayDirectLinks() {
        // TODO Auto-generated method stub

        List<Map.Entry<Link, LatencyLet>> sortedDelaylinks = CommonUse.sortByValue(linksDelay);
        int i = 0;
        try {
            for (i = 0; i < TOP * 2; i++) {
                LinkDelay linkDelay = new LinkDelay(sortedDelaylinks.get(i).getKey(),
                        sortedDelaylinks.get(i).getValue().getLatency());
                topNDelayDirectLinks[i] = linkDelay;
            }
        } catch (IndexOutOfBoundsException e) {
            for (int j = i; j < TOP *2; j++) {
                topNDelayDirectLinks[j] = null;
            }
        }
        return topNDelayDirectLinks;
    }

    @Override
    public Long getMaxNoDirectLinkDelay() {
        // TODO Auto-generated method stub
        LinkDelay linkDelay = getMaxNoDirectLinkDelayDetail();
        if (linkDelay != null) {
            return linkDelay.getDelay();
        } else {
            return null;
        }
    }

    @Override
    public LinkDelay getMaxNoDirectLinkDelayDetail() {
        // TODO Auto-generated method stub

        Long maxLinkDelay = new Long(0);
        Link idealLink = null;
        for (NoDirectLink link : noDirectlinksDelay.keySet()) {
            if (noDirectlinksDelay.get(link).getLatency() >= maxLinkDelay) {
                maxLinkDelay = noDirectlinksDelay.get(link).getLatency();
                idealLink = link;
            }
        }
        if (idealLink != null) {
            LinkDelay linkDelay = new LinkDelay(idealLink, maxLinkDelay);
            return linkDelay;
        } else {
            return null;
        }
    }

    @Override
    public LinkDelay[] getTopNDelayNoDirectLinks() {
        // TODO Auto-generated method stub

        List<Map.Entry<NoDirectLink, LatencyLet>> sortedDelaylinks = CommonUse.sortByValue(noDirectlinksDelay);
        int i = 0;
        try {
            for (i = 0; i < TOP; i++) {
                LinkDelay linkDelay = new LinkDelay(sortedDelaylinks.get(i).getKey(),
                        sortedDelaylinks.get(i).getValue().getLatency());
                topNDelayNoDirectLinks[i] = linkDelay;
            }
        } catch (IndexOutOfBoundsException e) {
            for (int j = i; j < TOP; j++) {
                topNDelayNoDirectLinks[j] = null;
            }
        }
        return topNDelayNoDirectLinks;

    }

}