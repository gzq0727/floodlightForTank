package tank.sdnos.monitor.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import tank.sdnos.monitor.IBandwidthMonitor;
import tank.sdnos.monitor.web.Serializer.NoDirectLinkUsageArray;

public class AllDescendNoDirectLinkUsageResource extends ServerResource {

    @Get("json")
    public NoDirectLinkUsageArray getDescendNoDirectLinks() {
        IBandwidthMonitor bandwidthMonitorService = (IBandwidthMonitor) getContext().getAttributes()
                .get(IBandwidthMonitor.class.getCanonicalName());

        return new NoDirectLinkUsageArray(bandwidthMonitorService.getAllDescendUsageNoDirectLinks());

    }
}
