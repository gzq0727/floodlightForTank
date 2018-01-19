package tank.sdnos.monitor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFGroupStatsEntry;
import org.projectfloodlight.openflow.protocol.OFMeterConfig;
import org.projectfloodlight.openflow.protocol.OFMeterFeatures;
import org.projectfloodlight.openflow.protocol.OFMeterStats;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.IFloodlightService;

public interface ISwitchStatisticsCollector extends IFloodlightService {

    public List<OFStatsReply> getSwitchStatistics(DatapathId switchId, OFStatsType statsType);

    public Map<DatapathId, List<OFStatsReply>> getSwitchsStatistics(Set<DatapathId> dpids, OFStatsType statsType);

    /** meter features get **/
    public List<OFMeterFeatures> getMeterFeaturesStats(DatapathId sw);

    public Map<DatapathId, List<OFMeterFeatures>> getMeterFeaturesStats(Set<DatapathId> sws);

    /** get meter entries in switches */
    public Map<DatapathId, List<OFMeterStats>> getMeterStats(Set<DatapathId> sws);

    /** get meter entries in one switch */
    public List<OFMeterStats> getMeterStats(DatapathId sw);

    /** meter stats get **/
    public OFMeterStats getMeterStats(DatapathId sw, int meterId);

    /** get meter config in switch **/
    public Map<DatapathId, List<OFMeterConfig>> getMeterConfig(Set<DatapathId> sws);

    public List<OFMeterConfig> getMeterConfig(DatapathId sw);

    public OFMeterConfig getMeterConfig(DatapathId sw, int meterId);

    /** get flow entries in switches */
    public Map<DatapathId, List<OFFlowStatsEntry>> getFlowStats(Set<DatapathId> sws);

    /** get flow entries in one switch */
    public List<OFFlowStatsEntry> getFlowStats(DatapathId sw);

    /** get group entries in switches */
    public Map<DatapathId, List<OFGroupStatsEntry>> getGroupStats(Set<DatapathId> sws);

    /** get group entries in one switch */
    public List<OFGroupStatsEntry> getGroupStats(DatapathId sw);

}
