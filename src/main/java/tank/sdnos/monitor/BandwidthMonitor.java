package tank.sdnos.monitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.statistics.StatisticsCollector;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;

/**
 * 带宽获取模块
 * @author gzq
 *
 */
public class BandwidthMonitor implements IFloodlightModule,IMonitorBandwidthService{
    private static final Logger log = LoggerFactory.getLogger(BandwidthMonitor.class);
    protected static IFloodlightProviderService floodlightProvider;
    protected static IStatisticsService statisticsService;
    //Floodllight实现的线程池，当然我们也可以使用Java自带的，但推荐使用这个
    private static IThreadPoolService threadPoolService;
    //Future类，不明白的可以百度 Java现成future,其实C++11也有这个玩意了
    private static ScheduledFuture<?> portBandwidthCollector;
    private static IOFSwitchService switchService;
    private static Map<NodePortTuple,SwitchPortBandwidth> bandwidth;
    private static final int portBandwidthInterval = 5;

    //告诉FL，我们添加了一个模块，提供了IMonitorBandwidthService
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IMonitorBandwidthService.class);
        return l;
    }

    //我们前面声明了几个需要使用的service,在这里说明一下实现类
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(IMonitorBandwidthService.class, this);
        return m;
    }

    //告诉FL我们以来那些服务，以便于加载
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IStatisticsService.class);
        l.add(IOFSwitchService.class);
        l.add(IThreadPoolService.class);
        return l;
    }

    //初始化这些service,个人理解这个要早于startUp()方法的执行，验证很简单，在两个方法里打印当前时间就可以。
    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        statisticsService = context.getServiceImpl(IStatisticsService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
        threadPoolService = context.getServiceImpl(IThreadPoolService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        startCollectBandwidth();
    }

    //自定义的开始收集数据的方法，使用了线程池，定周期的执行
    private synchronized void startCollectBandwidth(){
        portBandwidthCollector = threadPoolService.getScheduledExecutor().scheduleAtFixedRate(new GetBandwidthThread(), portBandwidthInterval, portBandwidthInterval, TimeUnit.SECONDS);
        log.info("tank# bandwidth collection thread(s) started");
    }

    //自定义的线程类，在上面的方法中实例化，并被调用
    /**
     * Single thread for collecting switch statistics and
     * containing the reply.
     */
    private class GetBandwidthThread extends Thread implements Runnable  {
        private Map<NodePortTuple,SwitchPortBandwidth> bandwidth;

        public Map<NodePortTuple, SwitchPortBandwidth> getBandwidth() {
            return bandwidth;
        }

        @Override
        public void run() {
            log.info("tank# GetBandwidthThread run ....");
            bandwidth = getBandwidthMap();
            log.info("tank# bandwidth size: {}", bandwidth.size());
            log.info("tank# bandwidth: {}", bandwidth.toString());
        }
    }

    /**
     * 获取带宽使用情况
     * 需要简单的换算
        根据 switchPortBand.getBitsPerSecondRx().getValue()/(8*1024) + switchPortBand.getBitsPerSecondTx().getValue()/(8*1024)
        计算带宽
     */
    @Override
    public Map<NodePortTuple,SwitchPortBandwidth> getBandwidthMap(){
        bandwidth = statisticsService.getBandwidthConsumption();
        Iterator<Entry<NodePortTuple,SwitchPortBandwidth>> iter = bandwidth.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<NodePortTuple,SwitchPortBandwidth> entry = iter.next();
            NodePortTuple tuple  = entry.getKey();
            SwitchPortBandwidth switchPortBand = entry.getValue();
            String info = tuple.getNodeId()+","+tuple.getPortId().getPortNumber()+","+(switchPortBand.getBitsPerSecondRx().getValue() + switchPortBand.getBitsPerSecondTx().getValue())/1024/1024 + " Mbits/s";
            //String info = tuple.getNodeId()+","+tuple.getPortId().getPortNumber()+","+(switchPortBand.getBitsPerSecondRx().getValue()/(8*1024) + switchPortBand.getBitsPerSecondTx().getValue()/(8*1024));
            log.info("tank# sw port speed: {}",info);
        }

        return bandwidth;
    }
}
