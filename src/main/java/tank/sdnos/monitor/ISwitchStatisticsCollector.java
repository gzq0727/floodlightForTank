package tank.sdnos.monitor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
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
	public OFMeterFeatures getMeterFeaturesStats(IOFSwitch sw);

	public Map<IOFSwitch, OFMeterFeatures> getMeterFeaturesStats(Set<IOFSwitch> sws);

	/** meter stats get **/
	public Map<IOFSwitch, List<OFMeterStats>> getMeterStats(Set<IOFSwitch> sws);

	public List<OFMeterStats> getMeterStats(IOFSwitch sw);

	public OFMeterStats getMeterStats(IOFSwitch sw, int meterId);

	public Map<IOFSwitch, OFMeterStats> getMeterStats(Set<IOFSwitch> sws, int meterId);

	/** get meter config in switch **/
	public Map<IOFSwitch, List<OFMeterConfig>> getMeter(Set<IOFSwitch> sws);

	public List<OFMeterConfig> getMeter(IOFSwitch sw);

	public OFMeterConfig getMeter(IOFSwitch sw, int meterId);

	public Map<IOFSwitch, OFMeterConfig> getMeter(Set<IOFSwitch> sws, int meterId);

	/****/
	public Map<IOFSwitch, List<OFFlowStatsEntry>> getFlowStats(Set<IOFSwitch> sws);

	public List<OFFlowStatsEntry> getFlowStats(IOFSwitch sw);

	/** service setting **/
	public void setMeterStatsInterval(long interval, TimeUnit unit);

	public void stopMeterStats();

	public void setUpdateInTime(boolean enable);
}
