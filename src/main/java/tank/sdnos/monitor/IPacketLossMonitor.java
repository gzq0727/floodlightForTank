package tank.sdnos.monitor;

import java.util.Map;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;

public interface IPacketLossMonitor extends IFloodlightService {
    public Long getPortPacketLossRate(String dpid, String port);

    public Long getPortPacketLossRate(NodePortTuple nodePortTuple);

    public Map<NodePortTuple, Long> getAllPortPacketLossRate();
}
