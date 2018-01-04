package tank.sdnos.monitor;

import java.util.Map;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;

public interface IMonitorBandwidthService extends IFloodlightService {
    //带宽使用情况
    public Map<NodePortTuple,SwitchPortBandwidth> getBandwidthMap();
}