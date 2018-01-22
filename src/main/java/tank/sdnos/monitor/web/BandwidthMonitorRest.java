package tank.sdnos.monitor.web;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class BandwidthMonitorRest implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        // TODO Auto-generated method stub
        Router router = new Router(context);
        router.attach("/json/TopNSpeedNoDirectLinks",TopNSpeedNoDirectLinksResource.class);
        router.attach("/json/TopNUsageNoDirectLinks",TopNUsageNoDirectLinksResource.class);
        router.attach("/json/AllNoDirectLinkSpeed",AllNoDirectLinkSpeedResource.class);
        router.attach("/json/AllNoDirectLinkUsage",AllNoDirectLinkUsageResource.class);
        router.attach("/json/DescendNoDirectLinkUsage",AllDescendNoDirectLinkUsageResource.class);
        router.attach("/json/AscendNoDirectLinkUsage",AllAscendNoDirectLinkUsageResource.class);
        router.attach("/json/DescendNoDirectLinkSpeed",AllDescendNoDirectLinkSpeedResource.class);
        router.attach("/json/AscendNoDirectLinkSpeed",AllAscendNoDirectLinkSpeedResource.class);

        return router;
    }

    @Override
    public String basePath() {
        // TODO Auto-generated method stub
        return "/tank/moitor/bandwidthmoitor";
    }

}
