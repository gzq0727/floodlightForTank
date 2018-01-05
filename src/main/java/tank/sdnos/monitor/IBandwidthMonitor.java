package tank.sdnos.monitor;

import java.util.Map;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;

public interface IBandwidthMonitor extends IFloodlightService{

    public Map<NodePortTuple, SwitchPortBandwidth> getBandwidth();

    public SwitchPortBandwidth getPortBandwidth(NodePortTuple nodePortTuple);

    public Map<NodePortTuple,SwitchPortBandwidth> getBandwidthMap();
}
