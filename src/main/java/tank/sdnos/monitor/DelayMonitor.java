package tank.sdnos.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

public class DelayMonitor implements IFloodlightModule, IDelayMonitor {
    private static final Logger log = LoggerFactory.getLogger(DelayMonitor.class);
    private static ILinkDiscoveryService linkDiscoveryService;
    private static IThreadPoolService threadPoolService;

    private static volatile Map<Link, LatencyLet> linksDelay = new HashMap<Link, LatencyLet>();
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
            Map<Link, LinkInfo> linkInfo = linkDiscoveryService.getLinks();
            Iterator<Entry<Link, LinkInfo>> iter = linkInfo.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Link, LinkInfo> link = iter.next();
                LatencyLet latencyLet = new LatencyLet();
                latencyLet.setLatency(link.getKey().getLatency());
                latencyLet.setCurrentLatency(link.getValue().getCurrentLatency());
                if (link.getValue().getLatencyHistoryAverageForTank() != null) {
                    latencyLet.setAverageLatency(link.getValue().getLatencyHistoryAverageForTank());
                } else {
                    latencyLet.setAverageLatency(null);
                }
                linksDelay.put(link.getKey(), latencyLet);
            }
            testDelayMonitor();
        }
    }

    private void testDelayMonitor() {
        String srcSw = "00:00:00:00:00:00:00:01";
        String dstSw = "00:00:00:00:00:00:00:02";
        String srcPort = "2";
        String dstPort = "2";
        log.info("tank# latency: {} ", getLatency(srcSw, srcPort, dstSw, dstPort).getValue());
        log.info("tank# currentLatency: {}", getCurrentLatency(srcSw, srcPort, dstSw, dstPort).getValue());
        if (getAverageLatency(srcSw, srcPort, dstSw, dstPort) != null) {
            log.info("average latency: {}", getAverageLatency(srcSw, srcPort, dstSw, dstPort).getValue());
        }
    }

    public static class LatencyLet {
        private U64 latency;
        private U64 currentLatency;
        private U64 averageLatency;

        public U64 getLatency() {
            return latency;
        }

        public void setLatency(U64 latency) {
            this.latency = latency;
        }

        public U64 getCurrentLatency() {
            return currentLatency;
        }

        public void setCurrentLatency(U64 currentLatency) {
            this.currentLatency = currentLatency;
        }

        public U64 getAverageLatency() {
            return averageLatency;
        }

        public void setAverageLatency(U64 averageLatency) {
            this.averageLatency = averageLatency;
        }

    }

    /*
     * return the link latency caulate
     * by @net.floodlightcontroller.linkdiscovery.
     * internal.LinkInfo.addObservedLatency(U64 latency) depended on the average
     * latency and currentlatency
     */
    @Override
    public U64 getLatency(String srcSw, String srcPort, String dstSw, String dstPort) {
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
    public U64 getLatency(Link link) {
        for (Link mlink : linksDelay.keySet()) {
            if (mlink.equals(link)) {
                return mlink.getLatency();
            }
        }
        log.error("tank# can not find thd link: {}", "srcSw:" + link.getSrc() + " srcPort:" + link.getSrcPort()
                + " dstSw:" + link.getDst() + " dstPort" + link.getDstPort());
        return null;
    }

    /* return the link current latency = the last latency measured */
    @Override
    public U64 getCurrentLatency(String srcSw, String srcPort, String dstSw, String dstPort) {
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
    public U64 getCurrentLatency(Link link) {
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
    public U64 getAverageLatency(String srcSw, String srcPort, String dstSw, String dstPort) {
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
    public U64 getAverageLatency(Link link) {
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
}