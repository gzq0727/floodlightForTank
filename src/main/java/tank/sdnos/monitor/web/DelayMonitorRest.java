package tank.sdnos.monitor.web;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class DelayMonitorRest implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        // TODO Auto-generated method stub
        Router router = new Router(context);
        router.attach("/json/TopNDelayNoDirectLinks",TopNDelayNoDirectLinkResource.class);
        router.attach("/json/AllNoDirectLinkDelay",AllNoDirectLinkDelayResource.class);
        router.attach("/json/DescendNoDirectLinkDelay", AllDescendNoDirectLinkDelayResource.class);
        router.attach("/json/AscendNoDirectLinkDelay", AllAscendNoDirectLinkDelayResource.class);

        return router;
    }

    @Override
    public String basePath() {
        // TODO Auto-generated method stub
        return "/tank/moitor/delaymoitor";

}
}
