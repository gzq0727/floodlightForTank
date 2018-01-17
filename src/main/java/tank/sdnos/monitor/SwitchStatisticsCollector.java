package tank.sdnos.monitor;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFFlowStatsRequest;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMeterConfig;
import org.projectfloodlight.openflow.protocol.OFMeterConfigStatsReply;
import org.projectfloodlight.openflow.protocol.OFMeterFeatures;
import org.projectfloodlight.openflow.protocol.OFMeterFeaturesStatsReply;
import org.projectfloodlight.openflow.protocol.OFMeterStats;
import org.projectfloodlight.openflow.protocol.OFMeterStatsReply;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.UnsignedLong;
import com.google.common.util.concurrent.ListenableFuture;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import tank.sdnos.qos.meter.IMeterService;

public class SwitchStatisticsCollector
        implements IFloodlightModule, IOFSwitchListener, ISwitchStatisticsCollector, IOFMessageListener {
    private static final Logger log = LoggerFactory.getLogger(SwitchStatisticsCollector.class);
    private final OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);
    private final static long METER_DEFAULT_COLLECT_TIME = 5; /*
                                                               * meter stats
                                                               * default collect
                                                               * time 5 s
                                                               */
    private final static TimeUnit METER_COLLECT_DEFAULE_TIME_UNIT = TimeUnit.SECONDS; /*
                                                                                       * defaule
                                                                                       * time
                                                                                       * unit
                                                                                       */

    private static IOFSwitchService switchService;
    private static IMeterService meterService;
    private IFloodlightProviderService floodlightProvider = null;
    private IThreadPoolService threadPoolService = null;
    private boolean enableUpdateInTime = true;
    private ScheduledFuture<?> meterStatsCollector = null;
    /*
     * will set the value euqals the collectionIntervalPortStatsSecond defined
     * in floodlightdefault.properties
     */
    private static int replyTimeout = 5;
    private long meterCollectInterval = 0;
    private TimeUnit meterCollectTimeUnit = TimeUnit.SECONDS;
    private Map<IOFSwitch, List<OFMeterStats>> switchsMeterStats = Collections.synchronizedMap(
            new HashMap<IOFSwitch, List<OFMeterStats>>()); /*
                                                            * store all switch
                                                            * meter stats
                                                            */
    private Map<IOFSwitch, List<OFMeterConfig>> switchsMeter = Collections.synchronizedMap(
            new HashMap<IOFSwitch, List<OFMeterConfig>>()); /*
                                                             * store all switch
                                                             * meter
                                                             */

    private Map<IOFSwitch, List<OFFlowStatsEntry>> switchsFlowStats = Collections.synchronizedMap(
            new HashMap<IOFSwitch, List<OFFlowStatsEntry>>()); /*
                                                                * store flow
                                                                * entry stats
                                                                */

    private Map<IOFSwitch, Long> switchsMeterStatsXid = Collections.synchronizedMap(
            new HashMap<IOFSwitch, Long>()); /* store meter stats latest xid */
    private Map<IOFSwitch, Long> switchsMeterXid = Collections.synchronizedMap(
            new HashMap<IOFSwitch, Long>()); /* store meter latest xid */
    private Map<IOFSwitch, Long> switchsFlowStatsXid = Collections.synchronizedMap(
            new HashMap<IOFSwitch, Long>()); /* store flow stats latest xid */

    private Map<IOFSwitch, OFMeterFeatures> switchsMeterFeatures = Collections.synchronizedMap(
            new HashMap<IOFSwitch, OFMeterFeatures>()); /*
                                                         * store all switchs'
                                                         */

    /**
     * Get statistics from a switch.
     *
     * @param switchId
     * @param statsType
     * @return
     */

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // TODO Auto-generated method stub
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(ISwitchStatisticsCollector.class);
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> l = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        l.put(ISwitchStatisticsCollector.class, this);
        return l;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IOFSwitchService.class);
        l.add(IThreadPoolService.class);
        l.add(IStatisticsService.class);
        l.add(IMeterService.class);

        l.add(IFloodlightProviderService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
        threadPoolService = context.getServiceImpl(IThreadPoolService.class);
        meterService = context.getServiceImpl(IMeterService.class);
        Map<String, String> config = context
                .getConfigParams(net.floodlightcontroller.statistics.StatisticsCollector.class);

        if (config.containsKey("collectionIntervalPortStatsSecond")) {
            try {
                replyTimeout = Integer.parseInt(config.get("collectionIntervalPortStatsSecond").trim());
            } catch (Exception e) {
                log.error(
                        "tank# Could not parse collectionIntervalPortStatsSecond parameter in net.floodlightcontroller"
                                + ".statistics.StatisticsCollector. replyTimeout will be set to default {}",
                        replyTimeout);
            }
        }
        log.info("tank# switch statistics reply timeout set to {}s", replyTimeout);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        log.info("SwitchStatisticsCollector is in service");
        floodlightProvider.addOFMessageListener(OFType.STATS_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.METER_MOD, this);
        floodlightProvider.addOFMessageListener(OFType.FLOW_MOD, this);
        switchService.addOFSwitchListener(this);
        meterStatsCollector = threadPoolService.getScheduledExecutor().scheduleAtFixedRate(new StatsCollector(), 0,
                (meterCollectInterval == 0 ? METER_DEFAULT_COLLECT_TIME : meterCollectInterval),
                (meterCollectInterval == 0 ? METER_COLLECT_DEFAULE_TIME_UNIT : meterCollectTimeUnit));
    }

    /**
     * Single thread for collecting switch statistics and containing the reply.
     */
    private class GetStatisticsThread extends Thread {
        private List<OFStatsReply> statsReply;
        private DatapathId switchId;
        private OFStatsType statType;

        public GetStatisticsThread(DatapathId switchId, OFStatsType statType) {
            this.switchId = switchId;
            this.statType = statType;
            this.statsReply = null;
        }

        public List<OFStatsReply> getStatisticsReply() {
            return statsReply;
        }

        public DatapathId getSwitchId() {
            return switchId;
        }

        @Override
        public void run() {
            statsReply = getSwitchStatistics(switchId, statType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<OFStatsReply> getSwitchStatistics(DatapathId switchId, OFStatsType statsType) {
        IOFSwitch sw = switchService.getSwitch(switchId);
        ListenableFuture<?> future;
        List<OFStatsReply> values = null;
        Match match;
        if (sw != null) {
            OFStatsRequest<?> req = null;
            switch (statsType) {
            case FLOW:
                // TODO
            case AGGREGATE:
                match = sw.getOFFactory().buildMatch().build();
                req = sw.getOFFactory().buildAggregateStatsRequest().setMatch(match).setOutPort(OFPort.ANY)
                        .setTableId(TableId.ALL).build();
                break;
            case PORT:
                req = sw.getOFFactory().buildPortStatsRequest().setPortNo(OFPort.ANY).build();
                break;
            case QUEUE:
                req = sw.getOFFactory().buildQueueStatsRequest().setPortNo(OFPort.ANY)
                        .setQueueId(UnsignedLong.MAX_VALUE.longValue()).build();
                break;
            case DESC:
                req = sw.getOFFactory().buildDescStatsRequest().build();
                break;
            case GROUP:
                if (sw.getOFFactory().getVersion().compareTo(OFVersion.OF_10) > 0) {
                    req = sw.getOFFactory().buildGroupStatsRequest().build();
                }
                break;

            case METER:
                // TODO

            case GROUP_DESC:
                if (sw.getOFFactory().getVersion().compareTo(OFVersion.OF_10) > 0) {
                    req = sw.getOFFactory().buildGroupDescStatsRequest().build();
                }
                break;

            case GROUP_FEATURES:
                if (sw.getOFFactory().getVersion().compareTo(OFVersion.OF_10) > 0) {
                    req = sw.getOFFactory().buildGroupFeaturesStatsRequest().build();
                }
                break;

            case METER_CONFIG:
                // TODO

            case METER_FEATURES:
                // TODO

            case TABLE:
                if (sw.getOFFactory().getVersion().compareTo(OFVersion.OF_10) > 0) {
                    req = sw.getOFFactory().buildTableStatsRequest().build();
                }
                break;

            case TABLE_FEATURES:
                if (sw.getOFFactory().getVersion().compareTo(OFVersion.OF_10) > 0) {
                    req = sw.getOFFactory().buildTableFeaturesStatsRequest().build();
                }
                break;
            case PORT_DESC:
                if (sw.getOFFactory().getVersion().compareTo(OFVersion.OF_13) >= 0) {
                    req = sw.getOFFactory().buildPortDescStatsRequest().build();
                }
                break;
            case EXPERIMENTER:
            default:
                log.error("Stats Request Type {} not implemented yet", statsType.name());
                break;
            }
            try {
                if (req != null) {
                    future = sw.writeStatsRequest(req);
                    values = (List<OFStatsReply>) future.get(replyTimeout / 2, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                log.error("Failure retrieving statistics from switch {}. {}", sw, e);
            }
        }
        return values;
    }

    /**
     * Retrieve the statistics from all switches in parallel.
     *
     * @param dpids
     * @param statsType
     * @return
     */
    @Override
    public Map<DatapathId, List<OFStatsReply>> getSwitchsStatistics(Set<DatapathId> dpids, OFStatsType statsType) {
        HashMap<DatapathId, List<OFStatsReply>> model = new HashMap<DatapathId, List<OFStatsReply>>();

        List<GetStatisticsThread> activeThreads = new ArrayList<GetStatisticsThread>(dpids.size());
        List<GetStatisticsThread> pendingRemovalThreads = new ArrayList<GetStatisticsThread>();
        GetStatisticsThread t;
        for (DatapathId d : dpids) {
            t = new GetStatisticsThread(d, statsType);
            activeThreads.add(t);
            t.start();
        }

        /*
         * Join all the threads after the timeout. Set a hard timeout of 12
         * seconds for the threads to finish. If the thread has not finished the
         * switch has not replied yet and therefore we won't add the switch's
         * stats to the reply.
         */
        for (int iSleepCycles = 0; iSleepCycles < replyTimeout; iSleepCycles++) {
            for (GetStatisticsThread curThread : activeThreads) {
                if (curThread.getState() == State.TERMINATED) {
                    model.put(curThread.getSwitchId(), curThread.getStatisticsReply());
                    pendingRemovalThreads.add(curThread);
                }
            }

            /*
             * remove the threads that have completed the queries to the
             * switches
             */
            for (GetStatisticsThread curThread : pendingRemovalThreads) {
                activeThreads.remove(curThread);
            }

            /* clear the list so we don't try to double remove them */
            pendingRemovalThreads.clear();

            /* if we are done finish early */
            if (activeThreads.isEmpty()) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for statistics", e);
            }
        }

        return model;
    }

    @Override
    public OFMeterFeatures getMeterFeaturesStats(IOFSwitch sw) {
        return switchsMeterFeatures.get(sw);
    }

    @Override
    public Map<IOFSwitch, OFMeterFeatures> getMeterFeaturesStats(Set<IOFSwitch> sws) {
        Map<IOFSwitch, OFMeterFeatures> rst = new HashMap<IOFSwitch, OFMeterFeatures>();
        for (IOFSwitch sw : sws) {
            rst.put(sw, switchsMeterFeatures.get(sw));
        }
        return rst;
    }

    @Override
    public Map<IOFSwitch, List<OFMeterStats>> getMeterStats(Set<IOFSwitch> sws) {
        Map<IOFSwitch, List<OFMeterStats>> rst = new HashMap<IOFSwitch, List<OFMeterStats>>();
        for (IOFSwitch sw : sws) {
            rst.put(sw, switchsMeterStats.get(sw));
        }

        return rst;
    }

    @Override
    public List<OFMeterStats> getMeterStats(IOFSwitch sw) {
        return switchsMeterStats.get(sw);
    }

    @Override
    public OFMeterStats getMeterStats(IOFSwitch sw, int meterId) {
        for (OFMeterStats ofms : switchsMeterStats.get(sw)) {
            if (ofms.getMeterId() == meterId) {
                return ofms;
            }
        }
        return null;
    }

    @Override
    public Map<IOFSwitch, OFMeterStats> getMeterStats(Set<IOFSwitch> sws, int meterId) {
        Map<IOFSwitch, OFMeterStats> rst = new HashMap<IOFSwitch, OFMeterStats>();
        for (IOFSwitch sw : sws) {
            for (OFMeterStats ofms : switchsMeterStats.get(sw)) {
                if (ofms.getMeterId() == meterId) {
                    rst.put(sw, ofms);
                }
            }
        }
        return rst;
    }

    @Override
    public Map<IOFSwitch, List<OFMeterConfig>> getMeter(Set<IOFSwitch> sws) {
        Map<IOFSwitch, List<OFMeterConfig>> rst = new HashMap<IOFSwitch, List<OFMeterConfig>>();
        for (IOFSwitch sw : sws) {
            rst.put(sw, switchsMeter.get(sw));
        }
        return rst;
    }

    @Override
    public List<OFMeterConfig> getMeter(IOFSwitch sw) {
        return switchsMeter.get(sw);
    }

    @Override
    public OFMeterConfig getMeter(IOFSwitch sw, int meterId) {
        for (OFMeterConfig ofms : switchsMeter.get(sw)) {
            if (ofms.getMeterId() == meterId) {
                return ofms;
            }
        }
        return null;
    }

    @Override
    public Map<IOFSwitch, OFMeterConfig> getMeter(Set<IOFSwitch> sws, int meterId) {
        Map<IOFSwitch, OFMeterConfig> rst = new HashMap<IOFSwitch, OFMeterConfig>();
        for (IOFSwitch sw : sws) {
            for (OFMeterConfig ofms : switchsMeter.get(sw)) {
                if (ofms.getMeterId() == meterId) {
                    rst.put(sw, ofms);
                }
            }
        }
        return rst;
    }

    @Override
    public Map<IOFSwitch, List<OFFlowStatsEntry>> getFlowStats(Set<IOFSwitch> sws) {
        Map<IOFSwitch, List<OFFlowStatsEntry>> map = new HashMap<IOFSwitch, List<OFFlowStatsEntry>>();
        for (IOFSwitch sw : sws) {
            map.put(sw, switchsFlowStats.get(sw));
        }
        return map;
    }

    @Override
    public List<OFFlowStatsEntry> getFlowStats(IOFSwitch sw) {
        return switchsFlowStats.get(sw);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        // Listening meter mod
        if (msg.getType() == OFType.METER_MOD) {
            if (enableUpdateInTime) {
                sw.write(meterService.buildMeterStatsRequest());
                sw.write(meterService.buildMeterConfigStatsRequest());
            }
            return Command.CONTINUE;
        } else if (msg.getType() == OFType.FLOW_MOD) {
            if (enableUpdateInTime) {
                sw.write(buildFlowStatsRequest());
            }
            return Command.CONTINUE;
        }

        // Listening meter stats replay
        OFStatsReply ofsr = (OFStatsReply) msg;
        switch (ofsr.getStatsType()) {
        case METER_FEATURES: // get meter features
            OFMeterFeaturesStatsReply ofmfsr = (OFMeterFeaturesStatsReply) ofsr;
            switchsMeterFeatures.put(sw, ofmfsr.getFeatures());
            break;
        case METER: // get all meter entry
            OFMeterStatsReply ofmsr = (OFMeterStatsReply) ofsr;
            if (ofmsr.getXid() == switchsMeterStatsXid.get(sw)) {
                switchsMeterStats.get(sw).addAll(ofmsr.getEntries());
            } else {
                switchsMeterStatsXid.put(sw, ofmsr.getXid());
                List<OFMeterStats> list = new LinkedList<OFMeterStats>();
                list.addAll(ofmsr.getEntries());
                switchsMeterStats.put(sw, list);
            }
            break;
        case METER_CONFIG: // get all meter entry
            OFMeterConfigStatsReply ofmcsr = (OFMeterConfigStatsReply) ofsr;
            if (ofmcsr.getXid() == switchsMeterXid.get(sw)) {
                switchsMeter.get(sw).addAll(ofmcsr.getEntries());
            } else {
                switchsMeterXid.put(sw, ofmcsr.getXid());
                List<OFMeterConfig> list = new LinkedList<OFMeterConfig>();
                list.addAll(ofmcsr.getEntries());
                switchsMeter.put(sw, list);
            }
            break;
        case FLOW: // get all flow stats
            OFFlowStatsReply offsr = (OFFlowStatsReply) ofsr;
            log.info("reply:"+offsr.toString());
            if (offsr.getXid() == switchsMeterXid.get(sw)) {
                switchsFlowStats.get(sw).addAll(offsr.getEntries());
            } else {
                switchsFlowStatsXid.put(sw, offsr.getXid());
                List<OFFlowStatsEntry> list = new LinkedList<OFFlowStatsEntry>();
                list.addAll(offsr.getEntries());
                switchsFlowStats.put(sw, list);
            }
            break;
        default:
            return Command.CONTINUE;
        }
        return Command.STOP;
    }

    @Override
    public void switchAdded(DatapathId switchId) {
        switchsMeterStatsXid.put(switchService.getAllSwitchMap().get(switchId), (long) -1);
        switchsMeterXid.put(switchService.getAllSwitchMap().get(switchId), (long) -1);
        switchsFlowStatsXid.put(switchService.getAllSwitchMap().get(switchId), (long) -1);

        switchService.getAllSwitchMap().get(switchId).write(meterService.buildMeterFeaturesStatsRequest());
        switchService.getAllSwitchMap().get(switchId).write(meterService.buildMeterStatsRequest());
        switchService.getAllSwitchMap().get(switchId).write(meterService.buildMeterConfigStatsRequest());
        switchService.getAllSwitchMap().get(switchId).write(buildFlowStatsRequest());

    }

    @Override
    public void switchRemoved(DatapathId switchId) {
        switchsMeterFeatures.remove(switchService.getSwitch(switchId));
        switchsMeterStats.remove(switchService.getSwitch(switchId));
        switchsMeter.remove(switchService.getSwitch(switchId));
        switchsFlowStats.remove(switchService.getSwitch(switchId));

    }

    @Override
    public void switchActivated(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchChanged(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchDeactivated(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    //////////////////////////////////////// flow stats
    /**
     * build flow stats request
     *
     * @return
     */
    private OFFlowStatsRequest buildFlowStatsRequest() {
        Match match = factory.buildMatch().build();
        return factory.buildFlowStatsRequest().setMatch(match).setOutPort(OFPort.ANY).setTableId(TableId.ALL)
                .setOutGroup(OFGroup.ANY).build();
    }

    /* period to collect meter stats of switchs */
    private class StatsCollector implements Runnable {

        @Override
        public void run() {
            for (DatapathId dpid : switchService.getAllSwitchDpids()) {
                switchService.getAllSwitchMap().get(dpid).write(buildFlowStatsRequest());
                switchService.getAllSwitchMap().get(dpid).write(meterService.buildMeterStatsRequest());
                switchService.getAllSwitchMap().get(dpid).write(meterService.buildMeterConfigStatsRequest());
                log.info("Sending meter stats & config request to all enable switchs. Period:"
                        + (meterCollectInterval == 0 ? METER_DEFAULT_COLLECT_TIME : meterCollectInterval) + ""
                        + (meterCollectInterval == 0 ? METER_COLLECT_DEFAULE_TIME_UNIT : meterCollectTimeUnit));
            }

            log.info(switchsFlowStats.toString());
        }
    }

    @Override
    public void setMeterStatsInterval(long interval, TimeUnit unit) {
        meterCollectInterval = interval;
        meterCollectTimeUnit = unit;
        log.info("set meter stats period: " + interval + "" + unit);
        stopMeterStats();
        log.info("restart meter stats thread");
        meterStatsCollector = threadPoolService.getScheduledExecutor().scheduleAtFixedRate(new StatsCollector(), 0,
                (meterCollectInterval == 0 ? METER_DEFAULT_COLLECT_TIME : meterCollectInterval),
                (meterCollectInterval == 0 ? METER_COLLECT_DEFAULE_TIME_UNIT : meterCollectTimeUnit));
    }

    @Override
    public void setUpdateInTime(boolean enable) {
        enableUpdateInTime = enable;
    }

    @Override
    public void stopMeterStats() {
        if (!meterStatsCollector.cancel(false)) {
            log.error("Could not cancel port stats thread");
        } else {
            log.warn("meter collection thread(s) stopped");
        }
    }
}