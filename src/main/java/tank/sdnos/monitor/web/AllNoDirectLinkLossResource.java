package tank.sdnos.monitor.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import tank.sdnos.monitor.IPacketLossMonitor;
import tank.sdnos.monitor.web.Serializer.NoDirectLinkLossMap;

public class AllNoDirectLinkLossResource extends ServerResource {
    @Get("json")
    public NoDirectLinkLossMap getAllNoDirectLinkLossResource() {
        IPacketLossMonitor packetLossMonitorService = (IPacketLossMonitor) getContext().getAttributes()
                .get(IPacketLossMonitor.class.getCanonicalName());

        return new NoDirectLinkLossMap(packetLossMonitorService.getAllNoDirectLinkLossRate());
    }
}
