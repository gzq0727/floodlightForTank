package tank.sdnos.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFPortStatsEntry;
import org.projectfloodlight.openflow.protocol.OFPortStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.IStatisticsService;

/**
 * 获取丢包率模块
 *
 * @author gzq
 *
 */

public class PacketLossMonitor implements IFloodlightModule, IPacketLossMonitor {
    private static final Logger log = LoggerFactory.getLogger(PacketLossMonitor.class);
    private static IOFSwitchService switchService;
    private static IThreadPoolService threadPoolService;
    private static ISwitchStatisticsCollector swStatisticsCollector;
    private static ScheduledFuture<?> packetLossStatsCollector;

    private static volatile HashMap<NodePortTuple, Long> DPID_PK_LOSS = new HashMap<NodePortTuple, Long>();
    private static int statsUpdateInterval = 5;
    private static boolean isEnabled = true;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IPacketLossMonitor.class);
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> l = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        l.put(IPacketLossMonitor.class, this);
        return l;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IOFSwitchService.class);
        l.add(IThreadPoolService.class);
        l.add(ISwitchStatisticsCollector.class);
        l.add(IStatisticsService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        switchService = context.getServiceImpl(IOFSwitchService.class);
        threadPoolService = context.getServiceImpl(IThreadPoolService.class);
        swStatisticsCollector = context.getServiceImpl(ISwitchStatisticsCollector.class);

        Map<String, String> config = context
                .getConfigParams(net.floodlightcontroller.statistics.StatisticsCollector.class);
        if (config.containsKey("collectionIntervalPortStatsSecond")) {
            try {
                statsUpdateInterval = Integer.parseInt(config.get("collectionIntervalPortStatsSecond").trim());
            } catch (Exception e) {
                log.error(
                        "tank# Could not parse collectionIntervalPortStatsSecond parameter in net.floodlightcontroller"
                                + ".statistics.StatisticsCollector. Packet loss stats update interval will be set to default {}",
                        statsUpdateInterval);
            }
        }
        log.info("tank# packet loss statistics collection interval set to {}s", statsUpdateInterval);

        config = context.getConfigParams(this);
        if (config.containsKey("enable")) {
            try {
                isEnabled = Boolean.parseBoolean(config.get("enable").trim());
            } catch (Exception e) {
                log.error("Could not parse '{}'. Using default of {}", "enable", isEnabled);
            }
        }
        log.info("tank# packetloss monitor {}", isEnabled ? "enabled" : "disabled");
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        if (isEnabled) {
            startStatisticsCollection();
            log.info("tank# PacketLossMonitor is in service");
        }
    }

    /**
     * Start all stats threads.
     */
    private synchronized void startStatisticsCollection() {
        packetLossStatsCollector = threadPoolService.getScheduledExecutor().scheduleAtFixedRate(
                new PacketLossStatsCollector(), statsUpdateInterval, statsUpdateInterval, TimeUnit.SECONDS);
        log.info("tank# packet loss rate collection thread(s) started");
    }

    /**
     * Run periodically to collect all port statistics. This only collects
     * bandwidth stats right now, but it could be expanded to record other
     * information as well. The difference between the most recent and the
     * current RX/TX bytes is used to determine the "elapsed" bytes. A timestamp
     * is saved each time stats results are saved to compute the bits per second
     * over the elapsed time. There isn't a better way to compute the precise
     * bandwidth unless the switch were to include a timestamp in the stats
     * reply message, which would be nice but isn't likely to happen. It would
     * be even better if the switch recorded bandwidth and reported bandwidth
     * directly.
     *
     * Stats are not reported unless at least two iterations have occurred for a
     * single switch's reply. This must happen to compare the byte counts and to
     * get an elapsed time.
     *
     */
    private class PacketLossStatsCollector implements Runnable {
        @Override
        public void run() {
            Map<DatapathId, List<OFStatsReply>> replies = swStatisticsCollector
                    .getSwitchsStatistics(switchService.getAllSwitchDpids(), OFStatsType.PORT);
            for (Entry<DatapathId, List<OFStatsReply>> e : replies.entrySet()) {
                for (OFStatsReply r : e.getValue()) {
                    OFPortStatsReply psr = (OFPortStatsReply) r;
                    for (OFPortStatsEntry pse : psr.getEntries()) {
                        long pk_loss = 0;

                        if (e.getKey().toString().equals("") || e.getKey() == null) {
                        }
                        NodePortTuple npt = new NodePortTuple(e.getKey(), pse.getPortNo());
                        if ((pse.getRxBytes().getValue() + pse.getTxBytes().getValue()) != 0l) {
                            pk_loss = (pse.getRxDropped().getValue() + pse.getTxDropped().getValue())
                                    / (pse.getRxBytes().getValue() + pse.getTxBytes().getValue());

                        } else {
                            pk_loss = 0;
                        }
                        log.info("tank# packet loss rate: {} {} {}", new Object[] { npt.getNodeId().toString(),
                                npt.getPortId().toString(), String.valueOf(pk_loss) });
                        DPID_PK_LOSS.put(npt, pk_loss);
                    }
                }
            }
            getAllFlow();
        }
    }

    @Override
    public Long getPortPacketLossRate(String dpid, String port) {
        for (NodePortTuple nodePortTuple : DPID_PK_LOSS.keySet()) {
            if (nodePortTuple.getNodeId().toString().equals(dpid)
                    && nodePortTuple.getPortId().toString().equals(port)) {
                return DPID_PK_LOSS.get(nodePortTuple);
            }
        }
        log.info("tank# pleace check your port info ===> dpid: {} port: {}", dpid, port);
        return null;
    }

    @Override
    public Long getPortPacketLossRate(NodePortTuple nodePortTuple) {
        if (DPID_PK_LOSS.containsKey(nodePortTuple)) {
            return DPID_PK_LOSS.get(nodePortTuple);
        } else {
            log.info("tank# pleace check your port info ===> dpid: {} port: {}", nodePortTuple.getNodeId(),
                    nodePortTuple.getPortId());
            return null;
        }
    }

    @Override
    public Map<NodePortTuple, Long> getAllPortPacketLossRate() {
        return DPID_PK_LOSS;
    }

    public void getAllFlow() {
        DatapathId switchId = DatapathId.of("00:00:00:00:00:00:00:01");
        Set<DatapathId> dpSet = new HashSet<DatapathId>();
        dpSet.add(switchId);
        Map<DatapathId, List<OFStatsReply>> ofStatsReply = new HashMap<DatapathId, List<OFStatsReply>>();
        ofStatsReply = swStatisticsCollector.getSwitchsStatistics(dpSet, OFStatsType.FLOW);

        // for (OFStatsReply sr : ofStatsReply){
        // ofFlowStatsReply.add((OFFlowStatsReply)sr);
        // }
        log.info("tank#  s1 all flows {}", ofStatsReply.toString());

        ofStatsReply = swStatisticsCollector.getSwitchsStatistics(dpSet, OFStatsType.AGGREGATE);
        log.info("tank#  s1 all flows {}", ofStatsReply.toString());
        // switchId = DatapathId.of("00:00:00:00:00:00:00:02");
        // ofStatsReply = getSwitchStatistics(switchId, OFStatsType.FLOW);
        // log.info("tank# s2 all flows {}", ofStatsReply.toString());
        //
        // switchId = DatapathId.of("00:00:00:00:00:00:00:03");
        // ofStatsReply = getSwitchStatistics(switchId, OFStatsType.FLOW);
        // log.info("tank# s3 all flows {}", ofStatsReply.toString());
    }

}