package net.floodlightcontroller.statistics;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;

import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IStatisticsService extends IFloodlightService {

    public SwitchPortBandwidth getBandwidthConsumption(DatapathId dpid, OFPort p);

    public Map<NodePortTuple, SwitchPortBandwidth> getBandwidthConsumption();

    public void collectStatistics(boolean collect);

    public Map<DatapathId, List<OFStatsReply>> getSwitchStatistics(Set<DatapathId> dpids, OFStatsType statsType);

    public List<OFStatsReply> getSwitchStatistics(DatapathId switchId, OFStatsType statsTyp);
}
