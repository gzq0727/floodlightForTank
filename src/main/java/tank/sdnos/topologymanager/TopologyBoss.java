package tank.sdnos.topologymanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LDUpdate;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyManagerBackend;
import net.floodlightcontroller.topology.ITopologyService;

public class TopologyBoss implements IFloodlightModule, ITopologyListener {

   private static ITopologyService topologyService;
   private static ITopologyManagerBackend topologyManagerBackend;


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
        l.add(ITopologyService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        topologyService = context.getServiceImpl(ITopologyService.class);
        topologyManagerBackend = (ITopologyManagerBackend)topologyService;
        topologyService.addListener(this);

    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub

    }

    /* liseten the topology events such as : port down/up link up/down  when they happened */
    @Override
    public void topologyChanged(List<LDUpdate> linkUpdates) {
        // TODO Auto-generated method stub

    }

}
