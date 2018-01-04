package tank.sdnos.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import tank.sdnos.flowutils.FlowUtils;
import java.util.concurrent.TimeUnit;

/* test flow related operations */
public class Test1 implements IFloodlightModule{
    protected static Logger logger = LoggerFactory.getLogger(Test1.class);
    protected IFloodlightProviderService floodlightProvider;
    protected IOFSwitchService switchService;
    static boolean executed = false;
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
        l.add(IOFSwitchService.class);

        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);

    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        try {

            TimeUnit.SECONDS.sleep(30);
            addOneFlow();

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean addOneFlow(){
        logger.info("tank#dpids: {}",switchService.getAllSwitchDpids());
        String dpid = "00:00:00:00:00:00:00:01";
        DatapathId ofDpid = DatapathId.of(dpid);
        IOFSwitch sw = switchService.getSwitch(ofDpid);
        if(sw == null){
            logger.error("switch of dpid {} is not found",ofDpid);
        }else{
            String match = "in_port=5,eth_type=0x0800,ipv4_dst=192.168.0.0";
            String actions = "output=30";
            return FlowUtils.addFlow(sw, match, actions);
       }
        return false;
    }

}
