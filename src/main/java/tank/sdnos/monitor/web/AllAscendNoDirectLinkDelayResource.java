package tank.sdnos.monitor.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import tank.sdnos.monitor.IDelayMonitor;
import tank.sdnos.monitor.web.Serializer.NoDirectLinkDelayArray;;

public class AllAscendNoDirectLinkDelayResource extends ServerResource {

    @Get("json")
    public NoDirectLinkDelayArray getTopNDelayNoDirectLinksResource() {
        IDelayMonitor delayMonitorService = (IDelayMonitor) getContext().getAttributes()
                .get(IDelayMonitor.class.getCanonicalName());

        return new NoDirectLinkDelayArray(delayMonitorService.getAllAscendDelayNoDirectLinks());
    }
}
