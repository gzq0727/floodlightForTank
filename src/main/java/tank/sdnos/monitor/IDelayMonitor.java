package tank.sdnos.monitor;

import org.projectfloodlight.openflow.types.U64;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.Link;

public interface IDelayMonitor extends IFloodlightService {

    /*
     * return the link latency caulate
     * by @net.floodlightcontroller.linkdiscovery.
     * internal.LinkInfo.addObservedLatency(U64 latency) depended on the average
     * latency and currentlatency
     */
    public U64 getLatency(String srcSw, String srcPort, String dstSw, String dstPort);

    public U64 getLatency(Link link);

    /* return the link current latency = the last latency measured */
    public U64 getCurrentLatency(String srcSw, String srcPort, String dstSw, String dstPort);

    public U64 getCurrentLatency(Link link);

    /*
     * return the link average latency calcute by
     * net.floodlightcontroller.linkdiscovery.internal.LinkInfo.
     * getLatencyHistoryAverageForTank()
     */
    public U64 getAverageLatency(String srcSw, String srcPort, String dstSw, String dstPort);

    public U64 getAverageLatency(Link link);
}