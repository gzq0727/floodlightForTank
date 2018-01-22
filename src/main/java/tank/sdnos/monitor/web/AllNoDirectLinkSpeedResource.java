package tank.sdnos.monitor.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import tank.sdnos.monitor.IBandwidthMonitor;
import tank.sdnos.monitor.web.Serializer.NoDirectLinkSpeedMap;

public class AllNoDirectLinkSpeedResource extends ServerResource {

    @Get("json")
    public NoDirectLinkSpeedMap getAllNoDirectLinkSpeed() {
        IBandwidthMonitor bandwidthMonitorService = (IBandwidthMonitor) getContext().getAttributes()
                .get(IBandwidthMonitor.class.getCanonicalName());

        return new NoDirectLinkSpeedMap(bandwidthMonitorService.getAllNoDirectLinkSpeed());

    }

}
