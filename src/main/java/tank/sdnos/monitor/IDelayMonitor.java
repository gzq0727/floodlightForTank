package tank.sdnos.monitor;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.Link;
import tank.sdnos.monitor.DelayMonitor.LinkDelay;

public interface IDelayMonitor extends IFloodlightService {

    /*
     * return the link latency caulate
     * by @net.floodlightcontroller.linkdiscovery.
     * internal.LinkInfo.addObservedLatency(U64 latency) depended on the average
     * latency and currentlatency
     */
    public Long getDirectLatency(String srcSw, String srcPort, String dstSw, String dstPort);

    public Long getDirectLatency(Link link);

    /* return the link current latency = the last latency measured */
    public Long getDirectCurrentLatency(String srcSw, String srcPort, String dstSw, String dstPort);

    public Long getDirectCurrentLatency(Link link);

    /*
     * return the link average latency calcute by
     * net.floodlightcontroller.linkdiscovery.internal.LinkInfo.
     * getLatencyHistoryAverageForTank()
     */
    public Long getDirectAverageLatency(String srcSw, String srcPort, String dstSw, String dstPort);

    public Long getDirectAverageLatency(Link link);

    public Long getMaxDirectLinkDelay();

    public LinkDelay getMaxDirectLinkDelayDetail();

    public LinkDelay[] getTopNDelayDirectLinks();

    public Long getMaxNoDirectLinkDelay();

    public LinkDelay getMaxNoDirectLinkDelayDetail();

    public LinkDelay[] getTopNDelayNoDirectLinks();

}