package tank.sdnos.monitor.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import tank.sdnos.monitor.IDelayMonitor;
import tank.sdnos.monitor.web.Serializer.NoDirectLinkDelayMap;

public class AllNoDirectLinkDelayResource extends ServerResource {

    @Get("json")
    public NoDirectLinkDelayMap getAllNoDirectLinkDelayResource() {
        IDelayMonitor delayMonitorService = (IDelayMonitor) getContext().getAttributes()
                .get(IDelayMonitor.class.getCanonicalName());

        return new NoDirectLinkDelayMap(delayMonitorService.getAllNoDirectLinkLatency());
    }
}
