package tank.sdnos.monitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface ISwitchStatisticsCollector extends IFloodlightService {
    public List<OFStatsReply> getSwitchStatistics(DatapathId switchId, OFStatsType statsType);

    public Map<DatapathId, List<OFStatsReply>> getSwitchsStatistics(Set<DatapathId> dpids, OFStatsType statsType);

}
