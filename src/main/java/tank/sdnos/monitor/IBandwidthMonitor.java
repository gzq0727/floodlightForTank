package tank.sdnos.monitor;

import java.util.Map;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;

public interface IBandwidthMonitor extends IFloodlightService {

    public SwitchPortBandwidth getPortBandwidth(NodePortTuple nodePortTuple);

    public Map<NodePortTuple, SwitchPortBandwidth> getBandwidthMap();

    public Long getPortSpeed(NodePortTuple nodePortTuple);

    public Long convertSwitchPortBandwidthToSpeed(SwitchPortBandwidth swtichPortBandwidth);
}
