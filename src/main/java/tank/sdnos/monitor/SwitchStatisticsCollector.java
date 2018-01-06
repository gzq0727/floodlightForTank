package tank.sdnos.monitor;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.ver13.OFMeterSerializerVer13;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.UnsignedLong;
import com.google.common.util.concurrent.ListenableFuture;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.threadpool.IThreadPoolService;

public class SwitchStatisticsCollector implements IFloodlightModule, ISwitchStatisticsCollector {
    private static final Logger log = LoggerFactory.getLogger(SwitchStatisticsCollector.class);

    private static IOFSwitchService switchService;

    /*
     * will set the value euqals the collectionIntervalPortStatsSecond defined
     * in floodlightdefault.properties
     */
    private static int replyTimeout = 5;

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
        // TODO Auto-generated method stub
        Map<Class<? extends IFloodlightService>, IFloodlightService> l = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        l.put(ISwitchStatisticsCollector.class, this);
        return l;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        // TODO Auto-generated method stub
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IOFSwitchService.class);
        l.add(IThreadPoolService.class);
        l.add(IStatisticsService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        switchService = context.getServiceImpl(IOFSwitchService.class);
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
        // TODO Auto-generated method stub
        log.info("SwitchStatisticsCollector is in service");
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
                match = sw.getOFFactory().buildMatch().build();
                req = sw.getOFFactory().buildFlowStatsRequest().setMatch(match).setOutPort(OFPort.ANY)
                        .setTableId(TableId.ALL).build();
                break;
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
                if (sw.getOFFactory().getVersion().compareTo(OFVersion.OF_13) >= 0) {
                    req = sw.getOFFactory().buildMeterStatsRequest().setMeterId(OFMeterSerializerVer13.ALL_VAL).build();
                }
                break;

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
                if (sw.getOFFactory().getVersion().compareTo(OFVersion.OF_13) >= 0) {
                    req = sw.getOFFactory().buildMeterConfigStatsRequest().build();
                }
                break;

            case METER_FEATURES:
                if (sw.getOFFactory().getVersion().compareTo(OFVersion.OF_13) >= 0) {
                    req = sw.getOFFactory().buildMeterFeaturesStatsRequest().build();
                }
                break;

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

}