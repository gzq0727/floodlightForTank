package tank.sdnos.monitor.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import tank.sdnos.monitor.IPacketLossMonitor;
import tank.sdnos.monitor.web.Serializer.NoDirectLinkLossArray;

public class TopNLossNoDirectLinkResource extends ServerResource {

    @Get("json")
    public NoDirectLinkLossArray getTopNLossNoDirectLinksResource() {
        IPacketLossMonitor lossMonitorService = (IPacketLossMonitor) getContext().getAttributes()
                .get(IPacketLossMonitor.class.getCanonicalName());

        return new NoDirectLinkLossArray(lossMonitorService.getTopNLossLinks());
    }
}
