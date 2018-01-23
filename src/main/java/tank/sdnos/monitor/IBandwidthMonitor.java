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
     * @param nodePortTuple
     *            {@link NodePortTuple}
     * @return the port speed in the unit of Kbps
     */
    public Long getPortSpeed(NodePortTuple nodePortTuple);

    /* link related */
    /**
     * get the directional link speed the calculate method is:
     * (srcPort.getBitsPerSecondTx().getValue() +
     * dstPortBandwitdh.getBitsPerSecondRx().getValue()) / 2
     *
     * @param link
     *            {@link Link}
     * @return link speed
     */
    public Long getDirectLinkSpeed(Link link);

    /**
     * get the directional link speed the calculate method is:
     * (srcPort.getBitsPerSecondTx().getValue() +
     * dstPortBandwitdh.getBitsPerSecondRx().getValue()) / 2
     *
     * @param srcSw
     *            the {@link DatapathId} for source switch
     * @param srcPort
     *            the port number of source port
     * @param dstSw
     *            the {@link DatapathId} for destination switch
     * @param dstPort
     *            the port number of destination port
     * @return link speed
     */
    public Long getDirectLinkSpeed(DatapathId srcSw, int srcPort, DatapathId dstSw, int dstPort);

    /**
     * get the unirectional link speed the calculate method is: linkSpeed =
     * (srcPortSpeed + dstPortSpeed) / 2;
     *
     * @param link
     * @return link speed
     */
    public Long getNoDirectLinkSpeed(Link link);

    /**
     * get the unirectional link speed the calculate method is: linkSpeed =
     * (srcPortSpeed + dstPortSpeed) / 2;
     *
     * @param DatapathId
     *            srcSw, int srcPort, DatapathId dstSw, int dstPort
     * @return link speed
     */
    public Long getNoDirectLinkSpeed(DatapathId srcSw, int srcPort, DatapathId dstSw, int dstPort);

    /**
     * get link speed for all directional links
     *
     */
    public Map<Link, Long> getAllDirectLinkSpeed();

    /**
     * get link speed for all undirected links
     *
     * @return Map<{@link tank.sdnos.monitor.commuse.NoDirectLink},Long>
     */
    public Map<NoDirectLink, Long> getAllNoDirectLinkSpeed();

    /**
     * get link bandwidth usage for one undirected link, the calculate method
     * is: linkSpeed / linkBandwidth
     *
     * @param link
     *            {@link Link}
     * @return link bandwidth usage
     */
    public Float getNoDirectLinkUsage(Link link);

    /**
     * get link bandwidth usage for one undirected link, the calculate method
     * is: linkSpeed / linkBandwidth
     *
     * @param srcSw
     *            the {@link DatapathId} for source switch
     * @param srcPort
     *            the port number of source port
     * @param dstSw
     *            the {@link DatapathId} for destination switch
     * @param dstPort
     *            the port number of destination port
     * @return link usage
     */
    public Float getNoDirectLinkUsage(DatapathId srcSw, int srcPort, DatapathId dstSw, int dstPort);

    /**
     * get link usage for all undirected links
     *
     * @return link usage map
     */
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
