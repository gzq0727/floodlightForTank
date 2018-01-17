package tank.sdnos.monitor;

import java.util.Map;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.linkdiscovery.Link;
import tank.sdnos.monitor.CommonUse.NoDirectLink;
import tank.sdnos.monitor.PacketLossMonitor.LinkLoss;

public interface IPacketLossMonitor extends IFloodlightService {
    public Long getPortPacketLossRate(String dpid, String port);

    public Long getPortPacketLossRate(NodePortTuple nodePortTuple);

    public Map<NodePortTuple, Long> getAllPortPacketLossRate();

    public Long getNoDirectLinkLossRate(Link link);

    public Long getNoDirectLinkLossRate(NoDirectLink link);

    public Long getNoDirectLinkLossRate(DatapathId srcSw, OFPort srcPort, DatapathId dstSw, OFPort dstPort);

    public Long getNoDirectMaxLoss();

    public LinkLoss getMaxLossNoDirectLink();

    public LinkLoss[] getTopNLossLinks();
}
