package tank.flowmanager.flowbackup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.projectfloodlight.openflow.protocol.OFActionType;
import org.projectfloodlight.openflow.protocol.OFCapabilities;
import org.projectfloodlight.openflow.protocol.OFFeaturesReply;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessage.Builder;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.util.HexString;
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
import net.floodlightcontroller.packet.Ethernet;

public class FlowBackup implements IOFMessageListener,IFloodlightModule {
    protected IFloodlightProviderService floodlightProvider;
    protected Set<Long> macAddress;
    protected static final Logger logger = LoggerFactory.getLogger(FlowBackup.class);

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "FlowBackup";
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
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);

        return l;

    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        floodlightProvider =  context.getServiceImpl(IFloodlightProviderService.class);
        macAddress = new ConcurrentSkipListSet<Long>();

    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        floodlightProvider.addOFMessageListener(OFType.FEATURES_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.FLOW_MOD, this);

    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg,
            FloodlightContext cntx) {
        // TODO Auto-generated method stub
        switch(msg.getType()){
        case PACKET_IN:
            Set<OFCapabilities> caps = sw.getCapabilities();
            for (OFCapabilities cap : caps){

                logger.info("gzq#"+ cap.GROUP_STATS.toString());
            }
            break;
        case FEATURES_REPLY:
            logger.info("gzq#feature reply");
            OFFeaturesReply.Builder featureReply = ((OFFeaturesReply)msg).createBuilder();
            Set<OFActionType> actiontypes = featureReply.getActions();
            for (OFActionType a : actiontypes){
                logger.info("gzq#"+ a.toString());
            }
            Set<OFCapabilities> capacities = featureReply.getCapabilities();
            for (OFCapabilities cap : capacities){
                logger.info("gzq#"+ cap.toString());
            }
            featureReply.getDatapathId();
            featureReply.getNTables();
            featureReply.getPorts();
            featureReply.getReserved();
            featureReply.getType();


            break;
        case FLOW_MOD:
            /*锐捷交换机不支持多action,因此在这里需要进行一下过滤或者转化为组表实现*/
            OFFlowMod.Builder builder = ((OFFlowMod)msg).createBuilder();
            //List<OFAction> actions = builder.getActions();
            OFBufferId bufferId = builder.getBufferId();
            OFFlowModCommand command = builder.getCommand();
            switch(command){
            case ADD:
                break;
            case MODIFY:
                break;
            case DELETE:
                break;
            default:
                break;
            }
            U64 cookie = builder.getCookie();
            U64 cookieMask = builder.getCookieMask();
            Set<OFFlowModFlags> flowmodFlags = builder.getFlags();
            int hardTimeout = builder.getHardTimeout();
            int idleTimeout = builder.getIdleTimeout();
            //int importance = builder.getImportance();
            List<OFInstruction> instructions = builder.getInstructions();
            Match match = builder.getMatch();
            OFGroup ofgroup = builder.getOutGroup();
            OFPort outport = builder.getOutPort();
            int priority = builder.getPriority();
            TableId tableId = builder.getTableId();
            break;
        default:
            break;
        }

        Ethernet ethernet = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
        if (ethernet != null){
            Long sourceMacHash = Ethernet.toLong(ethernet.getSourceMACAddress().getBytes());
            if (!macAddress.contains(sourceMacHash)){
                macAddress.add(sourceMacHash);
                logger.info("gzq#Mac Address:{} seen on switch:{}", HexString.toHexString(sourceMacHash), sw.getId());
            }
            return Command.CONTINUE;
        }else{
            logger.info("gzq#ethernet is null");
            return null;
        }
    }
}

