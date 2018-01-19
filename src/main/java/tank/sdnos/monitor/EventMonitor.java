package tank.sdnos.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFControllerStatus;
import org.projectfloodlight.openflow.protocol.OFErrorMsg;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.protocol.OFGroupMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMeterMod;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFPortMod;
import org.projectfloodlight.openflow.protocol.OFTableMod;
import org.projectfloodlight.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LDUpdate;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.UpdateOperation;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;

public class EventMonitor implements IFloodlightModule, IOFMessageListener, ITopologyListener {
    private static Logger logger = LoggerFactory.getLogger(EventMonitor.class);
    private static IFloodlightProviderService floodlightProviderService;
    private static ITopologyService topologyService;

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
        // TODO Auto-generated method stub
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(ITopologyService.class);
        return null;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
        topologyService = context.getServiceImpl(ITopologyService.class);

    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        floodlightProviderService.addOFMessageListener(OFType.PACKET_IN, this);
//        floodlightProviderService.addOFMessageListener(OFType.PACKET_OUT, this);
        floodlightProviderService.addOFMessageListener(OFType.ERROR, this);
        floodlightProviderService.addOFMessageListener(OFType.FLOW_REMOVED, this);
        floodlightProviderService.addOFMessageListener(OFType.FLOW_MOD, this);
        floodlightProviderService.addOFMessageListener(OFType.PORT_MOD, this);
        floodlightProviderService.addOFMessageListener(OFType.GROUP_MOD, this);
        floodlightProviderService.addOFMessageListener(OFType.TABLE_MOD, this);
        floodlightProviderService.addOFMessageListener(OFType.METER_MOD, this);
        floodlightProviderService.addOFMessageListener(OFType.CONTROLLER_STATUS, this);
        topologyService.addListener(this);

    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "EventMonitor";
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

    @Override
    public void topologyChanged(List<LDUpdate> linkUpdates) {
        // TODO Auto-generated method stub
        for (LDUpdate ldUpdate : linkUpdates) {
            if (ldUpdate.getOperation() == UpdateOperation.PORT_DOWN) {
                logger.error("tank# {}", ldUpdate.toString());
            } else {
                logger.info("tank# {}", ldUpdate.toString());
            }
        }
    }

    @Override
    public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
            FloodlightContext cntx) {
        // TODO Auto-generated method stub
        switch (msg.getType()) {
        case PACKET_IN:
            OFPacketIn packetInMsg = (OFPacketIn) msg;
            logger.info("tank# {}", packetInMsg.toString());
            break;
//        case PACKET_OUT:
//            OFPacketOut packetOutMsg = (OFPacketOut) msg;
//            logger.info("tank# {}", packetOutMsg.toString());
//            break;
        case ERROR:
            OFErrorMsg errorMsg = (OFErrorMsg) msg;
            logger.info("tank# {}", errorMsg.toString());
            break;
        case FLOW_REMOVED:
            OFFlowRemoved flowRemovedMsg = (OFFlowRemoved) msg;
            logger.info("tank# {}", flowRemovedMsg.toString());
            break;
        case FLOW_MOD:
            OFFlowMod flowModMsg = (OFFlowMod) msg;
            logger.info("tank# {}", flowModMsg.toString());
            break;
        case PORT_MOD:
            OFPortMod portModMsg = (OFPortMod) msg;
            logger.info("tank# {}", portModMsg.toString());
            break;
        case GROUP_MOD:
            OFGroupMod groupModMsg = (OFGroupMod) msg;
            logger.info("tank# {}", groupModMsg.toString());
            break;
        case TABLE_MOD:
            OFTableMod tableModMsg = (OFTableMod) msg;
            logger.info("tank# {}", tableModMsg.toString());
            break;
        case METER_MOD:
            OFMeterMod meterModMsg = (OFMeterMod) msg;
            logger.info("tank# {}", meterModMsg.toString());
            break;
        case CONTROLLER_STATUS:
            OFControllerStatus controllerStatusMsg = (OFControllerStatus) msg;
            logger.info("tank# {}", controllerStatusMsg.toString());
            break;
        default:
            logger.info(msg.toString());
        }

        return Command.CONTINUE;
    }

}
