package tank.sdnos.monitor.web;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class LossMonitorRest implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        // TODO Auto-generated method stub
        Router router = new Router(context);
        router.attach("/json/TopNLossNoDirectLinks", TopNLossNoDirectLinkResource.class);
        router.attach("/json/AllNoDirectLinkLoss", AllNoDirectLinkLossResource.class);
        router.attach("/json/DescendNoDirectLinkLoss", AllDescendNoDirectLinkLossResource.class);
        router.attach("/json/AscendNoDirectLinkLoss", AllAscendNoDirectLinkLossResource.class);

        return router;
    }

    @Override
    public String basePath() {
        // TODO Auto-generated method stub
        return "/tank/moitor/lossmoitor";
    }
}
