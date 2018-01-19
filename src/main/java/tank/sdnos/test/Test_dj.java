package tank.sdnos.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import tank.sdnos.utils.FlowUtils;
import java.util.concurrent.TimeUnit;

/* test flow related operations */
public class Test_dj implements IFloodlightModule, IOFSwitchListener {
    protected static Logger logger = LoggerFactory.getLogger(Test_dj.class);
    protected IFloodlightProviderService floodlightProvider;
    protected IOFSwitchService switchService;
    static boolean executed = false;
    public static Map<DatapathId, IOFSwitch> switchs;
    public static Set<DatapathId> dpids;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        // TODO Auto-generated method stub
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IOFSwitchService.class);

        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);

    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        try {

            TimeUnit.SECONDS.sleep(1);
            //switchs = switchService.getAllSwitchMap();
            //dpids = switchs.keySet();

            //addOneGroup();
            //TimeUnit.SECONDS.sleep(1);
            //addOneFlow();
            TimeUnit.SECONDS.sleep(1);

            //delOneFlow();
           // modOneGroup();

            //TimeUnit.SECONDS.sleep(60);
            //delOneGroup();

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean addOneFlow() {
        logger.info("[addOneFlow] tank#dpids: {}", switchService.getAllSwitchDpids());
        String dpid = "00:00:58:69:6c:d0:fb:94";
        DatapathId ofDpid = DatapathId.of(dpid);
        IOFSwitch sw = switchService.getSwitch(ofDpid);
        // String match = "in_port=1,eth_type=0x0800,ipv4_dst=10.0.0.2";
        if (sw == null) {
            logger.error("switch of dpid {} is not found", ofDpid);
        } else {
            // String match = "in_port=49";
            String match = "in_port=49,eth_type=0x0800";
            // String actions = "group=1";
            String actions = "group=1";
            System.err.println("group-----------");
            // String otherMatch = "priority=5";
            return FlowUtils.addFlow(sw, match, actions, 0, 0, 5);
        }
        return false;
    }

    public boolean delOneFlow() {
        // logger.info("[addOneFlow] tank#dpids:
        // {}",switchService.getAllSwitchDpids());
        String dpid = "00:00:58:69:6c:d0:fb:94";
        DatapathId ofDpid = DatapathId.of(dpid);
        IOFSwitch sw = switchService.getSwitch(ofDpid);
        // String match = "in_port=1,eth_type=0x0800,ipv4_dst=10.0.0.2";
        if (sw == null) {
            logger.error("switch of dpid {} is not found", ofDpid);
        } else {

            // String match = "output_port=53";
            String match = "output_port=51";
            System.err.println("flow del-----------");
            // String otherMatch = "priority=5";
            return FlowUtils.delFlow(sw, match, false);
        }
        return false;
    }

    // public boolean addOneGroup() {
    //
    // logger.info("[addOneGroup] tank#dpids:
    // {}",switchService.getAllSwitchDpids());
    // String dpid = "00:00:00:00:00:00:00:01";
    // DatapathId ofDpid = DatapathId.of(dpid);
    // IOFSwitch sw = switchService.getSwitch(ofDpid);
    // String groupType = "indirect";
    // int groupNumber = 1;
    // List<String> actionBuckets = new ArrayList<String>();
    // actionBuckets.add("output=2");
    //
    // return FlowUtils.addGroup(sw, groupNumber, groupType, actionBuckets );
    //
    // }

    public boolean addOneGroup() {
        // switchs = switchService.getAllSwitchMap();
        // Set<DatapathId> dps = switchs.keySet();
        // for(DatapathId dp:dps) {
        // System.err.println(dp.toString() +
        // switchs.get(dp).getEnabledPorts());
        // //switchs.get(dp);
        // }

        logger.info("[addOneGroup] tank#dpids " + switchService.getAllSwitchDpids());
        String dpid = "00:00:58:69:6c:d0:fb:94";
        // String dpid = "00:00:00:00:00:00:00:01";
        DatapathId ofDpid = DatapathId.of(dpid);
        IOFSwitch sw = switchService.getSwitch(ofDpid);
        int groupNumber = 1;
        List<String> actionBuckets = new ArrayList<String>();
        // actionBuckets.add("output=2,output=3");

        actionBuckets.add("output=51");
        // actionBuckets.add("output=49");
        // actionBuckets.add("output=53");
        actionBuckets.add("output=47");
        // actionBuckets.add("output=53");
        // group_id=1,type=all,bucket=actions=output:2,output:3,bucket=actions=output:3

        // return FlowUtils.addGroup(sw, groupNumber, groupType, actionBuckets
        // );
        return FlowUtils.addAllGroup(sw, groupNumber, actionBuckets);

    }

    // public boolean modOneGroup() {
    //
    // logger.info("[modOneGroup] tank#dpids");
    // String dpid = "00:00:00:00:00:00:00:01";
    // DatapathId ofDpid = DatapathId.of(dpid);
    // IOFSwitch sw = switchService.getSwitch(ofDpid);
    // //String groupType = "all";
    // int groupNumber = 1;
    // String actionBucket = "output=2";
    // //actionBuckets.add("output=2,output=3");
    //
    // return FlowUtils.modifyGroup(sw, groupNumber, actionBucket);
    // }

    public boolean delOneGroup() {
        logger.info("[delOneGroup] tank#dpids");
        System.err.println("del-----------------");
        String dpid = "00:00:00:00:00:00:00:01";
        DatapathId ofDpid = DatapathId.of(dpid);
        IOFSwitch sw = switchService.getSwitch(ofDpid);

        return FlowUtils.delAllGroups(sw);
    }

    // public boolean addOneGroup() {
    // //which is weight?
    // logger.info("[addOneGroup] tank#dpids:
    // {}",switchService.getAllSwitchDpids());
    // String dpid = "00:00:58:69:6c:d0:fb:94";
    // DatapathId ofDpid = DatapathId.of(dpid);
    // IOFSwitch sw = switchService.getSwitch(ofDpid);
    // String groupType = "select";
    // int groupNumber = 1;
    // Map<String,String> actionBuckets = new HashMap<String,String>();
    // actionBuckets.put("output=51", "100");
    // actionBuckets.put("output=53", "20");
    // //测试中自己添加了 watch_group 与weight 字段，结果如下.4294967292 代表/** All groups */
    // //
    // group_id=1,type=select,bucket=weight:0,watch_group:4294967292,actions=output:2,bucket=weight:0,watch_group:4294967292,actions=output:3
    //
    // //在手动添加watch_group方法后，设为ANY，但结果却没有改参数
    // //
    // group_id=1,type=select,bucket=weight:0,actions=output:2,bucket=weight:0,actions=output:3
    //
    // //在将weight 改为1 后输出的结果中没有weight参数这一项
    // //
    // group_id=1,type=select,bucket=watch_group:4294967292,actions=output:2,bucket=watch_group:4294967292,actions=output:3
    //
    // //在将weight 改为100(200) 后输出的结果有weight参数这一项
    // //
    // group_id=1,type=select,bucket=weight:100,watch_group:4294967292,actions=output:2,bucket=weight:100,watch_group:4294967292,actions=output:3
    //
    // return FlowUtils.addSelectGroup(sw, groupNumber, groupType, actionBuckets
    // );
    //
    // }

    // public boolean addOneGroup() {
    //
    // logger.info("[addOneGroup] tank#dpids:
    // {}",switchService.getAllSwitchDpids());
    //
    // String dpid = "00:00:00:00:00:00:00:01";
    // DatapathId ofDpid = DatapathId.of(dpid);
    // IOFSwitch sw = switchService.getSwitch(ofDpid);
    // String groupType = "ff";
    // int groupNumber = 1;
    // List<String> actionBuckets = new ArrayList<String>();
    // actionBuckets.add("output=2");
    // //group_id=1,type=ff,bucket=watch_group:0,actions=output:2
    // actionBuckets.add("output=3");
    // //group_id=1,type=ff,bucket=watch_group:0,actions=output:2,bucket=watch_group:0,actions=output:3
    // //group_id=1,type=ff,bucket=watch_port:65528,watch_group:0,actions=output:2,bucket=watch_port:65528,watch_group:0,actions=output:3
    //
    // return FlowUtils.addGroup(sw, groupNumber, groupType, actionBuckets );
    //
    // }

    @Override
    public void switchAdded(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchRemoved(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchActivated(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchChanged(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchDeactivated(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

}
