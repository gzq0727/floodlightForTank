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
        Map<Class<? extends IFloodlightService>, IFloodlightService> l =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
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
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        try {
            log.info("tank# delay monitor start");
            while (true) {
                TimeUnit.SECONDS.sleep(5);
                testFunc();
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        Map<Link, LinkInfo> linkInfos = linkDiscoveryService.getLinks();
        Iterator<Entry<Link, LinkInfo>> iter = linkInfos.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Link, LinkInfo> link = iter.next();
            if (srcSw.equals(link.getKey().getSrc().toString()) && srcPort.equals(link.getKey().getSrcPort().toString())
                    && dstSw.equals(link.getKey().getDst().toString())
                    && dstPort.equals(link.getKey().getDstPort().toString())) {
                return link.getKey().getLatency();
            }
        }
        log.error("tank# can not find thd link: {}",
                new Object[] { "srcSw:" + srcSw, "srcPort:" + srcPort, "dstSw:" + dstSw, "dstPort" + dstPort });
        return null;
    }

    @Override
    public U64 getLatency(Link link) {
        Map<Link, LinkInfo> linkInfos = linkDiscoveryService.getLinks();
        for (Link mlink : linkInfos.keySet()) {
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
        Map<Link, LinkInfo> linkInfos = linkDiscoveryService.getLinks();
        Iterator<Entry<Link, LinkInfo>> iter = linkInfos.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Link, LinkInfo> link = iter.next();
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
        Map<Link, LinkInfo> linkInfos = linkDiscoveryService.getLinks();
        for (Link mlink : linkInfos.keySet()) {
            if (mlink.equals(link)) {
                return linkInfos.get(mlink).getCurrentLatency();
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
        Map<Link, LinkInfo> linkInfos = linkDiscoveryService.getLinks();
        Iterator<Entry<Link, LinkInfo>> iter = linkInfos.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Link, LinkInfo> link = iter.next();
            if (srcSw.equals(link.getKey().getSrc().toString()) && srcPort.equals(link.getKey().getSrcPort().toString())
                    && dstSw.equals(link.getKey().getDst().toString())
                    && dstPort.equals(link.getKey().getDstPort().toString())) {
                if (link.getValue().getLatencyHistoryAverageForTank() == null) {
                    log.info("tank# wait the latencyHistoryWindow to bu full, return null now");
                }
                return link.getValue().getLatencyHistoryAverageForTank();
            }
        }
        log.error("tank# can not find thd link: {}",
                new Object[] { "srcSw:" + srcSw, "srcPort:" + srcPort, "dstSw:" + dstSw, "dstPort" + dstPort });
        return null;
    }

    @Override
    public U64 getAverageLatency(Link link) {
        Map<Link, LinkInfo> linkInfos = linkDiscoveryService.getLinks();
        for (Link mlink : linkInfos.keySet()) {
            if (mlink.equals(link)) {
                return linkInfos.get(mlink).getLatencyHistoryAverageForTank();
            }
        }
        log.error("tank# can not find thd link: {}", "srcSw:" + link.getSrc() + " srcPort:" + link.getSrcPort()
                + " dstSw:" + link.getDst() + " dstPort" + link.getDstPort());
        return null;
    }

    public void testFunc() {
        Map<Link, LinkInfo> linkInfo = linkDiscoveryService.getLinks();
        Iterator<Entry<Link, LinkInfo>> iter = linkInfo.entrySet().iterator();
        if (!iter.hasNext()) {
            log.info("tank# link is null");
        }
        String srcSw = "00:00:00:00:00:00:00:01";
        String dstSw = "00:00:00:00:00:00:00:02";
        String srcPort = "2";
        String dstPort = "2";
        System.out.println("latency: " + getLatency(srcSw, srcPort, dstSw, dstPort).getValue());
        System.out.println("currentLatency: " + getCurrentLatency(srcSw, srcPort, dstSw, dstPort).getValue());
        if (getAverageLatency(srcSw, srcPort, dstSw, dstPort) != null) {
            System.out.println("average latency: " + getAverageLatency(srcSw, srcPort, dstSw, dstPort).getValue());
        }

        // while(iter.hasNext()){
        // Entry<Link, LinkInfo> node = iter.next();
        // System.out.println("srcSw:
        // "+node.getKey().getSrc().toString()+",srcPort：
        // "+node.getKey().getSrcPort());
        // System.out.println("dstSw:
        // "+node.getKey().getDst().toString()+",dstPort：
        // "+node.getKey().getDstPort());
        // System.out.println("linkDelay:
        // "+node.getKey().getLatency().getValue());
        // System.out.println("currentDelay：
        // "+node.getValue().getCurrentLatency().getValue());
        // if(node.getValue().getLatencyHistoryAverageForTank() != null){
        // System.out.println("averageDelay: " +
        // node.getValue().getLatencyHistoryAverageForTank().getValue());
        // }
        // }
    }
}