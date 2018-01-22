package tank.sdnos.monitor.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import tank.sdnos.monitor.IBandwidthMonitor;
import tank.sdnos.monitor.web.Serializer.NoDirectLinkUsageMap;

public class AllNoDirectLinkUsageResource extends ServerResource {

    @Get("json")
    public NoDirectLinkUsageMap getAllNoDirectLinkUsage() {
        IBandwidthMonitor bandwidthMonitorService = (IBandwidthMonitor) getContext().getAttributes()
                .get(IBandwidthMonitor.class.getCanonicalName());
        return new NoDirectLinkUsageMap(bandwidthMonitorService.getAllNoDirectLinkUsage());

    }

}
