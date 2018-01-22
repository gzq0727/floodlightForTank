package tank.sdnos.monitor.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import tank.sdnos.monitor.IBandwidthMonitor;
import tank.sdnos.monitor.web.Serializer.NoDirectLinkSpeedArray;

public class AllDescendNoDirectLinkSpeedResource extends ServerResource {

    @Get("json")
    public NoDirectLinkSpeedArray getDescendNoDirectLinks() {
        IBandwidthMonitor bandwidthMonitorService = (IBandwidthMonitor) getContext().getAttributes()
                .get(IBandwidthMonitor.class.getCanonicalName());

        return new NoDirectLinkSpeedArray(bandwidthMonitorService.getAllDescendSpeedNoDirectLinks());

    }
}
