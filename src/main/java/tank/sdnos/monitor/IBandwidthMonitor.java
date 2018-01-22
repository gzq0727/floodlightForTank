package tank.sdnos.monitor;

import java.util.Map;

import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import tank.sdnos.monitor.BandwidthMonitor.LinkSpeed;
import tank.sdnos.monitor.BandwidthMonitor.LinkUsage;
import tank.sdnos.monitor.CommonUse.NoDirectLink;

public interface IBandwidthMonitor extends IFloodlightService {

    public Map<NodePortTuple, SwitchPortBandwidth> getBandwidthMap();

    public SwitchPortBandwidth getPortBandwidth(NodePortTuple nodePortTuple);

    /**
     * get switch port speed in one switch the calculate method is speed =
     * port.getBitsPerSecondRx + port.getBitsPerSecondTx
     *
     * @return the port speed in the unit of Mbps
     */
    public Long getPortSpeed(NodePortTuple nodePortTuple);

    /* link related */
    /**
     * get the directional link speed the calculate method is:
     * (srcPort.getBitsPerSecondTx().getValue() +
     * dstPortBandwitdh.getBitsPerSecondRx().getValue()) / 2
     * @param link
     * @return
     */
    public Long getDirectLinkSpeed(Link link);

    public Long getDirectLinkSpeed(DatapathId srcSw, int srcPort, DatapathId dstSw, int dstPort);

    public Long getNoDirectLinkSpeed(Link link);

    public Long getNoDirectLinkSpeed(DatapathId srcSw, int srcPort, DatapathId dstSw, int dstPort);

    public Map<Link, Long> getAllDirectLinkSpeed();

    public Map<NoDirectLink, Long> getAllNoDirectLinkSpeed();

    public Float getNoDirectLinkUsage(Link link);

    public Float getNoDirectLinkUsage(DatapathId srcSw, int srcPort, DatapathId dstSw, int dstPort);

    public Map<NoDirectLink, Float> getAllNoDirectLinkUsage();

    /* get the class bean including the Link and Speed */
    public Long getMaxNoDirectLinkSpeed();

    public Float getMaxNoDirectLinkUsage();

    public LinkSpeed getMaxNoDirectLinkSpeedDetail();

    public LinkUsage getMaxNoDirectLinkUsageDetail();

    public LinkSpeed[] getTopNSpeedNoDirectLinks();

    public LinkUsage[] getTopNUsageNoDirectLinks();

    public Float getNoDirectLinkUsage(NoDirectLink noDirectLink);

    public Long getNoDirectLinkSpeed(NoDirectLink noDirectLink);

    public LinkUsage[] getAllDescendUsageNoDirectLinks();

    public LinkSpeed[] getAllAscendSpeedNoDirectLinks();

    public LinkSpeed[] getAllDescendSpeedNoDirectLinks();

    public LinkUsage[] getAllAscendUsageNoDirectLinks();

}
