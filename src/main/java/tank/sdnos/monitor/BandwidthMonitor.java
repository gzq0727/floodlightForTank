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
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.core.types.NodePortTuple;

/**
 * 带宽获取模块
 *
 * @author gzq
 *
 */
public class BandwidthMonitor implements IFloodlightModule, IBandwidthMonitor {
    private static final Logger log = LoggerFactory.getLogger(BandwidthMonitor.class);
    private static IStatisticsService statisticsService;
    // Floodllight实现的线程池，当然我们也可以使用Java自带的，但推荐使用这个
    private static IThreadPoolService threadPoolService;
    // Future类，不明白的可以百度 Java现成future,其实C++11也有这个玩意了
    private static ScheduledFuture<?> bandwidthMonitor;
    private static Map<NodePortTuple, SwitchPortBandwidth> bandwidth;

    /*
     * equals to net.floodlightcontroller.statistics.StatisticsCollector
     * .collectionIntervalPortStatsSeconds
     */
    private static int statsUpdateInterval = 5;
    private static boolean isEnabled = true;

    // 告诉FL，我们添加了一个模块，提供了IMonitorBandwidthService
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IBandwidthMonitor.class);
        return l;
    }

    // 我们前面声明了几个需要使用的service,在这里说明一下实现类
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> l = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        l.put(IBandwidthMonitor.class, this);
        return l;
    }

    // 告诉FL我们以来那些服务，以便于加载
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IStatisticsService.class);
        l.add(IThreadPoolService.class);
        return l;
    }

    // 初始化这些service,个人理解这个要早于startUp()方法的执行，验证很简单，在两个方法里打印当前时间就可以。
    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        statisticsService = context.getServiceImpl(IStatisticsService.class);
        threadPoolService = context.getServiceImpl(IThreadPoolService.class);

        Map<String, String> config = context
                .getConfigParams(net.floodlightcontroller.statistics.StatisticsCollector.class);
        if (config.containsKey("collectionIntervalPortStatsSecond")) {
            try {
                statsUpdateInterval = Integer.parseInt(config.get("collectionIntervalPortStatsSecond").trim());
            } catch (Exception e) {
                log.error(
                        "tank# Could not parse collectionIntervalPortStatsSecond parameter in net.floodlightcontroller"
                                + ".statistics.StatisticsCollector. Bandwidth stats update interval will be set to default {}",
                        statsUpdateInterval);
            }
        }
        log.info("tank# bandwidth statistics collection interval set to {}s", statsUpdateInterval);

        config = context.getConfigParams(this);
        if (config.containsKey("enable")) {
            try {
                isEnabled = Boolean.parseBoolean(config.get("enable").trim());
            } catch (Exception e) {
                log.error("Could not parse '{}'. Using default of {}", "enable", isEnabled);
            }
        }
        log.info("tank# bandwidth monitor {}", isEnabled ? "enabled" : "disabled");
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        if (isEnabled) {
            startBandwidthMonitor();
            log.info("tank# BandwidthMonitor is in service");
        }
    }

    // 自定义的开始收集数据的方法，使用了线程池，定周期的执行
    private synchronized void startBandwidthMonitor() {
        bandwidthMonitor = threadPoolService.getScheduledExecutor().scheduleAtFixedRate(new BandwidthUpdateThread(),
                statsUpdateInterval, statsUpdateInterval, TimeUnit.SECONDS);
        log.info("tank# bandwidth monitor thread(s) started");
    }

    // 自定义的线程类，在上面的方法中实例化，并被调用
    /**
     * Single thread for collecting switch statistics and containing the reply.
     */
    private class BandwidthUpdateThread extends Thread implements Runnable {

        @Override
        public void run() {
            bandwidth = statisticsService.getBandwidthConsumption();
            log.info("tank# bandwidth size: {}", bandwidth.size());
            log.info("tank# bandwidth: {}", bandwidth.toString());
            testBandwidthMonitor();
        }
    }

    @Override
    public Map<NodePortTuple, SwitchPortBandwidth> getBandwidthMap() {
        // TODO Auto-generated method stub
        return bandwidth;
    }

    @Override
    public SwitchPortBandwidth getPortBandwidth(NodePortTuple nodePortTuple) {
        return bandwidth.get(nodePortTuple);
    }

    /* unit Mbits/s */
    @Override
    public Long getPortSpeed(NodePortTuple nodePortTuple) {
        SwitchPortBandwidth switchPortBand = bandwidth.get(nodePortTuple);
        Long bdwth = null;
        if (switchPortBand != null) {
            bdwth = (switchPortBand.getBitsPerSecondRx().getValue() + switchPortBand.getBitsPerSecondTx().getValue())
                    / 1024 / 1024;
            return bdwth;
        }
        return bdwth;
    }

    /* unit Mbits/s */
    @Override
    public Long convertSwitchPortBandwidthToSpeed(SwitchPortBandwidth swtichPortBandwidth) {
        Long bdwth = null;
        if (swtichPortBandwidth != null) {
            bdwth = (swtichPortBandwidth.getBitsPerSecondRx().getValue()
                    + swtichPortBandwidth.getBitsPerSecondTx().getValue()) / 1024 / 1024;
            return bdwth;
        }
        return bdwth;
    }

    /**
     * 获取带宽使用情况 需要简单的换算 根据
     * switchPortBand.getBitsPerSecondRx().getValue()/(8*1024) +
     * switchPortBand.getBitsPerSecondTx().getValue()/(8*1024) 计算带宽
     */

    public void testBandwidthMonitor() {
        Iterator<Entry<NodePortTuple, SwitchPortBandwidth>> iter = getBandwidthMap().entrySet().iterator();
        while (iter.hasNext()) {
            Entry<NodePortTuple, SwitchPortBandwidth> entry = iter.next();
            NodePortTuple tuple = entry.getKey();

            SwitchPortBandwidth switchPortBand = getPortBandwidth(tuple);
            String info = tuple.getNodeId() + "," + tuple.getPortId().getPortNumber() + ","
                    + convertSwitchPortBandwidthToSpeed(switchPortBand) + " Mbits/s";
            log.info("tank# sw port speed: {}", info);
        }

    }
}
