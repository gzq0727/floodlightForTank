package tank.sdnos.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMeterConfigStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionMeter;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LDUpdate;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;
import tank.sdnos.monitor.ISwitchStatisticsCollector;
import tank.sdnos.utils.FlowUtils;

public class Test_gzq implements IFloodlightModule, IOFMessageListener, ITopologyListener {
    protected static final Logger logger = LoggerFactory.getLogger(Test1.class);
    protected IFloodlightProviderService floodlightProvider;
    protected IOFSwitchService switchService;
    protected IStatisticsService statisticsService;
    protected ITopologyService toplologyService;
    protected ISwitchStatisticsCollector switchStatisticsCollector;
    static boolean is = false;
    static boolean executed = false;
    private IThreadPoolService threadPool;
    private boolean start = true;
    private final OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IOFSwitchService.class);
        l.add(IOFSwitchService.class);
        l.add(IThreadPoolService.class);
        l.add(ITopologyService.class);
        l.add(IStatisticsService.class);
        l.add(ISwitchStatisticsCollector.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
        statisticsService = context.getServiceImpl(IStatisticsService.class);
        switchStatisticsCollector = context.getServiceImpl(ISwitchStatisticsCollector.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        Thread newTestThread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                IOFSwitch sw = switchService.getSwitch(DatapathId.of("00:00:00:00:00:00:00:01"));

                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                FlowUtils.addFlow(sw, "in_port=1", "output=2");
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                String reply = switchStatisticsCollector.getFlowStats(DatapathId.of("00:00:00:00:00:00:00:01"))
                        .toString();
                logger.info("tank# flows: {}", reply);

                List<String> buckets = new ArrayList<String>();
                buckets.add("output=1");
                buckets.add("output=2");
                FlowUtils.addAllGroup(sw, 1, buckets);
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                reply = switchStatisticsCollector.getGroupStats(DatapathId.of("00:00:00:00:00:00:00:01")).toString();
                logger.info("tank# groups: {}", reply);
            }
        };

        newTestThread.start();

    }

    @SuppressWarnings("unchecked")
    @Override
    public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
            FloodlightContext cntx) {

        logger.info(msg.getType() + "");
        switch (msg.getType()) {
        case FLOW_MOD:
            OFFlowMod offmod = (OFFlowMod) msg;
            if (!offmod.getCookie().equals(U64.of(0x123)) && offmod.getCommand() == OFFlowModCommand.ADD
                    && offmod.getMatch().get(MatchField.IPV4_SRC) != null
                    && offmod.getMatch().get(MatchField.IPV4_DST) != null
                    && (offmod.getMatch().get(MatchField.IPV4_SRC).equals(IPv4Address.of("172.32.0.1"))
                            || offmod.getMatch().get(MatchField.IPV4_SRC).equals(IPv4Address.of("172.32.0.2")))) {

                OFInstructionMeter meter = factory.instructions().buildMeter().setMeterId(0x01).build();
                List<OFInstruction> instructions = new ArrayList<OFInstruction>();
                instructions.add(meter);
                instructions.add(factory.instructions().applyActions(offmod.getActions()));
                OFFlowAdd flowAdd = factory.buildFlowAdd().setInstructions(instructions).setMatch(offmod.getMatch())
                        .setPriority(500).setCookie(U64.of(0x123)).build();
                sw.write(flowAdd);
                logger.error(sw + " add meter actions");
            }
            if (is) {
                is = false;
                logger.info("recive ICMP packet");
                logger.info("send meter: Rate 100 kbps ; Burst size = 100kb");

                // sw.write(MeterConstructor.meterBuildDelete(0x01));
                logger.info("modify meter: 0x01");
                // sw.write(MeterConstructor.buildMeterStatsReqst());

                // logger.info("send meter stats request");

                // sw.write(MeterConstructor.buildMeterFeaturesStatsReqst());
                // logger.info("send meter features request");

                // statisticsService.collectStatistics(true);
                // logger.info(statisticsService.getSwitchStatistics(switchService.getAllSwitchDpids(),
                // OFStatsType.METER)
                // + "");

                addOneFlow();
                // sw.write(MeterConstructor.buildFlow());
                logger.info("flod mod flow");

                // sw.write(MeterConstructor.build());
                // logger.info("send flow stats ");
                /*
                 *
                 * ListenableFuture<?> future = sw.writeStatsRequest(
                 * sw.getOFFactory().buildMeterStatsRequest().setMeterId(
                 * OFMeterSerializerVer13. ALL_VAL).build());
                 *
                 * try {
                 *
                 * List<OFStatsReply> values = (List<OFStatsReply>)
                 * future.get(); logger.info(values + ""); } catch
                 * (InterruptedException | ExecutionException e) {
                 * logger.error(e.getMessage()); }
                 */

            }
            break;
        case STATS_REQUEST:
            OFStatsRequest<?> ofSR = (OFStatsRequest<?>) msg;
            if (ofSR.getStatsType() == OFStatsType.METER_FEATURES) {
                logger.info("capture the meter features request!");
            } else if (ofSR.getStatsType() == OFStatsType.METER) {
                logger.info("capture the meter stats request!");
            } else if (ofSR.getStatsType() == OFStatsType.METER_CONFIG) {
                logger.info("capture the meter config request!");
            }
            break;
        case STATS_REPLY:

            OFStatsReply ofSRP = (OFStatsReply) msg;
            // logger.info("stats reply:" + ofSRP.getType());
            // if (ofSRP.getStatsType() == OFStatsType.METER_FEATURES) {
            // OFMeterFeaturesStatsReply ofmfsr = (OFMeterFeaturesStatsReply)
            // ofSRP;
            // logger.info("capture the meter features reply!" +
            // ofmfsr.getFeatures().toString());
            // meterService.stopMeterStats();
            // } else if (ofSRP.getStatsType() == OFStatsType.METER) {
            //// OFMeterStatsReply ofmsr = (OFMeterStatsReply) ofSRP;
            // logger.info("capture the meter stats reply!: " +
            //// ofmsr.getEntries().get(0).getFlowCount());
            /* } else */if (ofSRP.getStatsType() == OFStatsType.METER_CONFIG) {
                OFMeterConfigStatsReply ofmsr = (OFMeterConfigStatsReply) ofSRP;
                logger.error("xid: " + ofmsr.getXid() + "size: " + ofmsr.getEntries().size() + "");
                // logger.info("capture the meter config reply!");
            } /*
               * else if (ofSRP.getStatsType() == OFStatsType.FLOW) {
               * OFFlowStatsReply ofs = (OFFlowStatsReply) ofSRP;
               * logger.info("capture the flow stats reply! flow: " +
               * ofs.getEntries().get(0).toString()); }
               */

            break;
        default:
            break;

        }

        return Command.CONTINUE;
    }

    @Override
    public void topologyChanged(List<LDUpdate> linkUpdates) {
        if (start) {
            start = !start;
            threadPool.getScheduledExecutor().schedule(new TestMeter(), 10, TimeUnit.SECONDS);
        }
    }

    /* test thread */
    private class TestMeter implements Runnable {

        @Override
        public void run() {
        }
    }

    public boolean addOneFlow() {
        logger.info("tank#dpids: {}", switchService.getAllSwitchDpids());
        for (DatapathId dpid : switchService.getAllSwitchDpids()) {
            IOFSwitch sw = switchService.getSwitch(dpid);
            String match = "in_port=5,eth_type=0x0800,ipv4_dst=192.168.0.0";
            String actions = "output=30";
            return FlowUtils.addFlow(sw, match, actions);
        }
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return Test1.class.getSimpleName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }

}
