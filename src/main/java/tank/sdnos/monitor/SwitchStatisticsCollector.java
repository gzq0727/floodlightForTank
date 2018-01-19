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
import org.projectfloodlight.openflow.protocol.OFGroupStatsEntry;
import org.projectfloodlight.openflow.protocol.OFGroupStatsReply;
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
import org.projectfloodlight.openflow.protocol.ver13.OFMeterSerializerVer13;
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

public class SwitchStatisticsCollector implements IFloodlightModule, ISwitchStatisticsCollector {
    private static final Logger log = LoggerFactory.getLogger(SwitchStatisticsCollector.class);

    private IStatisticsService statisticsService;
    /*
     * will set the value euqals the collectionIntervalPortStatsSecond defined
     * in floodlightdefault.properties
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
        l.add(IThreadPoolService.class);
        l.add(IStatisticsService.class);

        l.add(IFloodlightProviderService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        statisticsService = context.getServiceImpl(IStatisticsService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        log.info("SwitchStatisticsCollector is in service");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<OFStatsReply> getSwitchStatistics(DatapathId switchId, OFStatsType statsType) {
        List<OFStatsReply> values = null;
        values = statisticsService.getSwitchStatistics(switchId, statsType);

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
        Map<DatapathId, List<OFStatsReply>> model = new HashMap<DatapathId, List<OFStatsReply>>();
        model = statisticsService.getSwitchStatistics(dpids, statsType);

        return model;
    }

    @Override
    public Map<DatapathId, List<OFFlowStatsEntry>> getFlowStats(Set<DatapathId> sws) {
        Map<DatapathId, List<OFFlowStatsEntry>> flowStatsReplyEntry = new HashMap<DatapathId, List<OFFlowStatsEntry>>();

        Map<DatapathId, List<OFStatsReply>> statsReply = getSwitchsStatistics(sws, OFStatsType.FLOW);
        /* convert OFStatsReply to OFFlowStatsReply */
        Map<DatapathId, List<OFFlowStatsReply>> flowStatsReply = new HashMap<DatapathId, List<OFFlowStatsReply>>();
        for (DatapathId dpid : statsReply.keySet()) {
            List<OFFlowStatsReply> flowStatsReplyList = new ArrayList<OFFlowStatsReply>();
            for (OFStatsReply ofStatsReply : statsReply.get(dpid)) {
                flowStatsReplyList.add((OFFlowStatsReply) ofStatsReply);
            }
            flowStatsReply.put(dpid, flowStatsReplyList);
        }
        /* get flow entrt from OFFlowStatsReply */
        for (DatapathId dpid : flowStatsReply.keySet()) {
            List<OFFlowStatsEntry> flowStatsEntryList = new ArrayList<OFFlowStatsEntry>();
            for (OFFlowStatsReply ofStatsReply : flowStatsReply.get(dpid)) {
                flowStatsEntryList.addAll(ofStatsReply.getEntries());
            }
            flowStatsReplyEntry.put(dpid, flowStatsEntryList);
        }

        return flowStatsReplyEntry;
    }

    @Override
    public List<OFFlowStatsEntry> getFlowStats(DatapathId sw) {
        List<OFFlowStatsEntry> flowStatsEntryList = new ArrayList<OFFlowStatsEntry>();

        List<OFStatsReply> statsReply = getSwitchStatistics(sw, OFStatsType.FLOW);

        List<OFFlowStatsReply> flowStatsReply = new ArrayList<OFFlowStatsReply>();

        /* convert OFStatsReply to OFFlowStatsReply */
        for (OFStatsReply ofStatsReply : statsReply) {
            flowStatsReply.add((OFFlowStatsReply) ofStatsReply);
        }

        /* get flow entrt from OFFlowStatsReply */
        for (OFFlowStatsReply sr : flowStatsReply) {
            flowStatsEntryList.addAll(sr.getEntries());
        }

        return flowStatsEntryList;
    }

    @Override
    public Map<DatapathId, List<OFGroupStatsEntry>> getGroupStats(Set<DatapathId> sws) {
        Map<DatapathId, List<OFGroupStatsEntry>> groupStatsReplyEntry = new HashMap<DatapathId, List<OFGroupStatsEntry>>();

        Map<DatapathId, List<OFStatsReply>> statsReply = getSwitchsStatistics(sws, OFStatsType.GROUP);
        /* convert OFStatsReply to OFFlowStatsReply */
        Map<DatapathId, List<OFGroupStatsReply>> groupStatsReply = new HashMap<DatapathId, List<OFGroupStatsReply>>();
        for (DatapathId dpid : statsReply.keySet()) {
            List<OFGroupStatsReply> groupStatsReplyList = new ArrayList<OFGroupStatsReply>();
            for (OFStatsReply ofStatsReply : statsReply.get(dpid)) {
                groupStatsReplyList.add((OFGroupStatsReply) ofStatsReply);
            }
            groupStatsReply.put(dpid, groupStatsReplyList);
        }
        /* get flow entrt from OFFlowStatsReply */
        for (DatapathId dpid : groupStatsReply.keySet()) {
            List<OFGroupStatsEntry> groupStatsEntryList = new ArrayList<OFGroupStatsEntry>();
            for (OFGroupStatsReply ofStatsReply : groupStatsReply.get(dpid)) {
                groupStatsEntryList.addAll(ofStatsReply.getEntries());
            }
            groupStatsReplyEntry.put(dpid, groupStatsEntryList);
        }

        return groupStatsReplyEntry;
    }

    @Override
    public List<OFGroupStatsEntry> getGroupStats(DatapathId sw) {
        List<OFGroupStatsEntry> groupStatsEntryList = new ArrayList<OFGroupStatsEntry>();

        List<OFStatsReply> statsReply = getSwitchStatistics(sw, OFStatsType.GROUP);

        List<OFGroupStatsReply> groupStatsReply = new ArrayList<OFGroupStatsReply>();

        /* convert OFStatsReply to OFFlowStatsReply */
        for (OFStatsReply ofStatsReply : statsReply) {
            groupStatsReply.add((OFGroupStatsReply) ofStatsReply);
        }

        /* get flow entrt from OFFlowStatsReply */
        for (OFGroupStatsReply sr : groupStatsReply) {
            groupStatsEntryList.addAll(sr.getEntries());
        }

        return groupStatsEntryList;
    }

    @Override
    public Map<DatapathId, List<OFMeterStats>> getMeterStats(Set<DatapathId> sws) {
        Map<DatapathId, List<OFMeterStats>> meterStats = new HashMap<DatapathId, List<OFMeterStats>>();
        Map<DatapathId, List<OFMeterStatsReply>> meterStatsReply = new HashMap<DatapathId, List<OFMeterStatsReply>>();

        Map<DatapathId, List<OFStatsReply>> statsReply = getSwitchsStatistics(sws, OFStatsType.METER);
        if (statsReply == null) {
            return null;
        }

        for (DatapathId dpid : statsReply.keySet()) {
            List<OFMeterStatsReply> meterStatsEntryList = new ArrayList<OFMeterStatsReply>();
            for (OFStatsReply ofStatsReply : statsReply.get(dpid)) {
                meterStatsEntryList.add((OFMeterStatsReply) ofStatsReply);
            }
            meterStatsReply.put(dpid, meterStatsEntryList);
        }

        for (DatapathId dpid : meterStatsReply.keySet()) {
            List<OFMeterStats> ms = new ArrayList<OFMeterStats>();
            for (OFMeterStatsReply msr : meterStatsReply.get(dpid)) {
                ms.addAll(msr.getEntries());
            }
            meterStats.put(dpid, ms);

        }

        return meterStats;
    }

    @Override
    public List<OFMeterStats> getMeterStats(DatapathId sw) {
        List<OFMeterStats> meterStatsEntryList = new ArrayList<OFMeterStats>();
        List<OFMeterStatsReply> meterStatsReply = new ArrayList<OFMeterStatsReply>();

        List<OFStatsReply> statsReply = getSwitchStatistics(sw, OFStatsType.METER);
        if (statsReply == null) {
            return null;
        }

        for (OFStatsReply ofStatsReply : statsReply) {
            meterStatsReply.add((OFMeterStatsReply) ofStatsReply);
        }

        for (OFMeterStatsReply meterReply : meterStatsReply) {
            meterStatsEntryList.addAll(meterReply.getEntries());
        }

        return meterStatsEntryList;
    }

    @Override
    public OFMeterStats getMeterStats(DatapathId sw, int meterId) {
        // TODO Auto-generated method stub
        List<OFMeterStats> meterStats = getMeterStats(sw);
        if (meterStats == null) {
            return null;
        }

        for (OFMeterStats oneMeter : meterStats) {
            if (oneMeter.getMeterId() == meterId) {
                return oneMeter;
            }
        }

        return null;
    }

    @Override
    public List<OFMeterFeatures> getMeterFeaturesStats(DatapathId sw) {
        // TODO Auto-generated method stub

        List<OFMeterFeatures> meterFeaturesStatsEntryList = new ArrayList<OFMeterFeatures>();
        List<OFMeterFeaturesStatsReply> meterFeaturesStatsReply = new ArrayList<OFMeterFeaturesStatsReply>();

        List<OFStatsReply> statsReply = getSwitchStatistics(sw, OFStatsType.METER_FEATURES);
        if (statsReply == null) {
            return null;
        }

        for (OFStatsReply ofStatsReply : statsReply) {
            meterFeaturesStatsReply.add((OFMeterFeaturesStatsReply) ofStatsReply);
        }

        for (OFMeterFeaturesStatsReply meterFeaturesReply : meterFeaturesStatsReply) {
            meterFeaturesStatsEntryList.add(meterFeaturesReply.getFeatures());
        }

        return meterFeaturesStatsEntryList;

    }

    @Override
    public Map<DatapathId, List<OFMeterFeatures>> getMeterFeaturesStats(Set<DatapathId> sws) {
        // TODO Auto-generated method stub
        Map<DatapathId, List<OFMeterFeatures>> meterFeatures = new HashMap<DatapathId, List<OFMeterFeatures>>();
        Map<DatapathId, List<OFMeterFeaturesStatsReply>> meterFeaturesStatsReply = new HashMap<DatapathId, List<OFMeterFeaturesStatsReply>>();

        Map<DatapathId, List<OFStatsReply>> statsReply = getSwitchsStatistics(sws, OFStatsType.METER_FEATURES);
        if (statsReply == null) {
            return null;
        }

        for (DatapathId dpid : statsReply.keySet()) {
            List<OFMeterFeaturesStatsReply> meterFeaturesStatsEntryList = new ArrayList<OFMeterFeaturesStatsReply>();
            for (OFStatsReply ofStatsReply : statsReply.get(dpid)) {
                meterFeaturesStatsEntryList.add((OFMeterFeaturesStatsReply) ofStatsReply);
            }
            meterFeaturesStatsReply.put(dpid, meterFeaturesStatsEntryList);
        }

        for (DatapathId dpid : meterFeaturesStatsReply.keySet()) {
            List<OFMeterFeatures> mf = new ArrayList<OFMeterFeatures>();
            for (OFMeterFeaturesStatsReply mfsr : meterFeaturesStatsReply.get(dpid)) {
                mf.add(mfsr.getFeatures());
            }
            meterFeatures.put(dpid, mf);

        }

        return meterFeatures;
    }

    @Override
    public Map<DatapathId, List<OFMeterConfig>> getMeterConfig(Set<DatapathId> sws) {
        // TODO Auto-generated method stub

        Map<DatapathId, List<OFMeterConfig>> meterConfig = new HashMap<DatapathId, List<OFMeterConfig>>();
        Map<DatapathId, List<OFMeterConfigStatsReply>> meterConfigStatsReply = new HashMap<DatapathId, List<OFMeterConfigStatsReply>>();

        Map<DatapathId, List<OFStatsReply>> statsReply = getSwitchsStatistics(sws, OFStatsType.METER_CONFIG);
        if (statsReply == null) {
            return null;
        }

        for (DatapathId dpid : statsReply.keySet()) {
            List<OFMeterConfigStatsReply> meterFeaturesStatsEntryList = new ArrayList<OFMeterConfigStatsReply>();
            for (OFStatsReply ofStatsReply : statsReply.get(dpid)) {
                meterFeaturesStatsEntryList.add((OFMeterConfigStatsReply) ofStatsReply);
            }
            meterConfigStatsReply.put(dpid, meterFeaturesStatsEntryList);
        }

        for (DatapathId dpid : meterConfigStatsReply.keySet()) {
            List<OFMeterConfig> mc = new ArrayList<OFMeterConfig>();
            for (OFMeterConfigStatsReply mcsr : meterConfigStatsReply.get(dpid)) {
                mc.addAll(mcsr.getEntries());
            }
            meterConfig.put(dpid, mc);

        }

        return meterConfig;
    }

    @Override
    public List<OFMeterConfig> getMeterConfig(DatapathId sw) {
        // TODO Auto-generated method stub

        List<OFMeterConfig> meterConfigStatsEntryList = new ArrayList<OFMeterConfig>();
        List<OFMeterConfigStatsReply> meterConfigStatsReply = new ArrayList<OFMeterConfigStatsReply>();

        List<OFStatsReply> statsReply = getSwitchStatistics(sw, OFStatsType.METER_CONFIG);
        if (statsReply == null) {
            return null;
        }

        for (OFStatsReply ofStatsReply : statsReply) {
            meterConfigStatsReply.add((OFMeterConfigStatsReply) ofStatsReply);
        }

        for (OFMeterConfigStatsReply meterConfigReply : meterConfigStatsReply) {
            meterConfigStatsEntryList.addAll(meterConfigReply.getEntries());
        }

        return meterConfigStatsEntryList;
    }

    @Override
    public OFMeterConfig getMeterConfig(DatapathId sw, int meterId) {
        // TODO Auto-generated method stub
        List<OFMeterConfig> meterConfig = getMeterConfig(sw);
        if (meterConfig == null) {
            return null;
        }

        for (OFMeterConfig mc : meterConfig) {
            if (mc.getMeterId() == meterId) {
                return mc;
            }
        }

        return null;
    }

}