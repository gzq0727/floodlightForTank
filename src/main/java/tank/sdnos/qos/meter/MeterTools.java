package tank.sdnos.qos.meter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFFlowStatsRequest;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMeterConfig;
import org.projectfloodlight.openflow.protocol.OFMeterConfigStatsReply;
import org.projectfloodlight.openflow.protocol.OFMeterConfigStatsRequest;
import org.projectfloodlight.openflow.protocol.OFMeterFeatures;
import org.projectfloodlight.openflow.protocol.OFMeterFeaturesStatsReply;
import org.projectfloodlight.openflow.protocol.OFMeterFeaturesStatsRequest;
import org.projectfloodlight.openflow.protocol.OFMeterFlags;
import org.projectfloodlight.openflow.protocol.OFMeterMod;
import org.projectfloodlight.openflow.protocol.OFMeterModCommand;
import org.projectfloodlight.openflow.protocol.OFMeterStats;
import org.projectfloodlight.openflow.protocol.OFMeterStatsReply;
import org.projectfloodlight.openflow.protocol.OFMeterStatsRequest;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBand;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBandDrop;
import org.projectfloodlight.openflow.protocol.ver13.OFMeterSerializerVer13;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import tank.sdnos.qos.meter.Bands.Band;

public class MeterTools implements IFloodlightModule, IMeterService {

	private static final Logger log = LoggerFactory.getLogger(MeterTools.class);

	private final static int METER_RATE_DEFAULT = 100; /* meter rate (kbps) */
	private final static int METER_BURST_SIZE_DEFAULT = METER_RATE_DEFAULT * 1; /* meter burst size (kilobits) */
	private final OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);

	private MeterConstructor mc = new MeterConstructor();

	@Override
	public boolean meterAdd(IOFSwitch sw, int meterId, long rate, long burst, boolean useKBPS, boolean useStats) {
		sw.write(mc.meterBuildAdd(meterId, rate, burst, useKBPS, useStats));
		return true;
	}

	@Override
	public boolean meterAdd(IOFSwitch sw, int meterId, long rate, boolean useKBPS, boolean useStats) {
		sw.write(mc.meterBuildAdd(meterId, rate, useKBPS, useStats));
		return true;
	}

	@Override
	public boolean meterAdd(IOFSwitch sw, int meterId, boolean useStats) {
		sw.write(mc.meterBuildAdd(meterId, useStats));
		return true;
	}

	@Override
	public boolean meterAdd(IOFSwitch sw, long meterId, long rate, long meterBurstSize) {
		sw.write(mc.meterBuildAdd(meterId, rate, meterBurstSize));
		return true;
	}

	@Override
	public boolean meterAdd(Set<IOFSwitch> sws, int meterId, long rate, long burst, boolean useKBPS, boolean useStats) {
		OFMeterMod ofmmod = mc.meterBuildAdd(meterId, rate, burst, useKBPS, useStats);
		for (IOFSwitch sw : sws) {
			sw.write(ofmmod);
		}
		return true;
	}

	@Override
	public boolean meterAdd(Set<IOFSwitch> sws, int meterId, long rate, boolean useKBPS, boolean useStats) {
		OFMeterMod ofmmod = mc.meterBuildAdd(meterId, rate, useKBPS, useStats);
		for (IOFSwitch sw : sws) {
			sw.write(ofmmod);
		}
		return true;
	}

	@Override
	public boolean meterAdd(Set<IOFSwitch> sws, int meterId, boolean useStats) {
		OFMeterMod ofmmod = mc.meterBuildAdd(meterId, useStats);
		for (IOFSwitch sw : sws) {
			sw.write(ofmmod);
		}
		return true;
	}

	@Override
	public boolean meterAdd(Set<IOFSwitch> sws, long meterId, long rate, long meterBurstSize) {
		OFMeterMod ofmmod = mc.meterBuildAdd(meterId, rate, meterBurstSize);
		for (IOFSwitch sw : sws) {
			sw.write(ofmmod);
		}
		return true;
	}

	@Override
	public boolean meterModify(IOFSwitch sw, Meter meter) {
		sw.write(mc.meterBuildModify(meter));
		return true;
	}

	@Override
	public boolean meterModify(IOFSwitch sw, int meterId, long rate, long burst, boolean useKBPS, boolean useStats) {
		sw.write(mc.meterBuildModify(meterId, rate, burst, useKBPS, useStats));
		return true;
	}

	@Override
	public boolean meterModify(Set<IOFSwitch> sws, Meter meter) {
		OFMeterMod ofmmod = mc.meterBuildModify(meter);
		for (IOFSwitch sw : sws) {
			sw.write(ofmmod);
		}
		return true;
	}

	@Override
	public boolean meterModify(Set<IOFSwitch> sws, int meterId, long rate, long burst, boolean useKBPS,
			boolean useStats) {
		OFMeterMod ofmmod = mc.meterBuildModify(meterId, rate, burst, useKBPS, useStats);
		for (IOFSwitch sw : sws) {
			sw.write(ofmmod);
		}
		return true;
	}

	@Override
	public boolean meterDelete(IOFSwitch sw, int meterID) {
		sw.write(mc.meterBuildDelete(meterID));
		return true;
	}

	@Override
	public boolean meterDelete(IOFSwitch sw, Meter meter) {
		sw.write(mc.meterBuildDelete(meter));
		return true;
	}

	@Override
	public boolean meterDelete(Set<IOFSwitch> sws, Meter meter) {
		OFMeterMod ofmmod = mc.meterBuildDelete(meter);
		for (IOFSwitch sw : sws) {
			sw.write(ofmmod);
		}
		return true;
	}

	@Override
	public boolean meterDelete(Set<IOFSwitch> sws, int meterID) {
		OFMeterMod ofmmod = mc.meterBuildDelete(meterID);
		for (IOFSwitch sw : sws) {
			sw.write(ofmmod);
		}
		return true;
	}

	@Override
	public boolean meterDelete(IOFSwitch sw) {
		sw.write(mc.meterBuildDelete());
		return false;
	}

	@Override
	public boolean meterDelete(Set<IOFSwitch> sws) {
		OFMeterMod ofmmod = mc.meterBuildDelete();
		for (IOFSwitch sw : sws) {
			sw.write(ofmmod);
		}
		return false;
	}

	@Override
	public boolean meterAdd(IOFSwitch sw, Meter meter) {
		sw.write(mc.meterBuildAdd(meter));
		return false;
	}

	@Override
	public boolean meterAdd(Set<IOFSwitch> sws, Meter meter) {
		OFMeterMod ofmmod = mc.meterBuildAdd(meter);
		for (IOFSwitch sw : sws) {
			sw.write(ofmmod);
		}
		return true;
	}

	/**
	 * 
	 * @author caihe inner tool class
	 *
	 */
	private class MeterConstructor {
		/**
		 * singletone
		 */
		private MeterConstructor() {
		}

		/*****************************************************
		 * * for build meter add * caihe * *
		 *****************************************************/

		/**
		 * @author caihe
		 * @param meterId
		 * @param rate
		 * @param burst
		 * @param useKBPS
		 * @param useStats
		 * @return
		 */
		public OFMeterMod meterBuildAdd(int meterId, long rate, long burst, boolean useKBPS, boolean useStats) {
			return meterBuild(OFMeterModCommand.ADD, meterId, rate, burst, useKBPS, useStats);
		}

		/**
		 * @author caihe
		 * @param meterId
		 * @param rate
		 * @param useKBPS
		 * @param useStats
		 * @return for no burst flag
		 */
		public OFMeterMod meterBuildAdd(int meterId, long rate, boolean useKBPS, boolean useStats) {
			return meterBuild(OFMeterModCommand.ADD, meterId, rate, 0, useKBPS, useStats);
		}

		/**
		 * @author caihe
		 * @return OFMeterMod use default meter rate k00kbps, burst_size 100kb
		 * 
		 */
		public OFMeterMod meterBuildAdd(int meterId, boolean useStats) {
			return meterBuild(OFMeterModCommand.ADD, meterId, METER_RATE_DEFAULT, METER_BURST_SIZE_DEFAULT, true,
					useStats);
		}

		public OFMeterMod meterBuildAdd(Meter meter) {
			return meterBuild(OFMeterModCommand.ADD, meter);
		}

		/**
		 * 
		 * @param meterRate
		 * @param meterBurstSize
		 * @return OFMeterMod
		 */
		public OFMeterMod meterBuildAdd(long meterId, long meterRate, long meterBurstSize) {
			// meter flags : suport: KBPS, BURST
			OFMeterFlags kbps = OFMeterFlags.KBPS;
			OFMeterFlags burst = OFMeterFlags.BURST;
			Set<OFMeterFlags> flags = new HashSet<OFMeterFlags>();
			flags.add(burst);
			flags.add(kbps);

			// build band
			// rate(unit: kilobit per seconds) 100 kbps for test
			// burst size = rate * Tolerance time(1s for test) (unit: kilobits)
			OFMeterBand ofmb = factory.meterBands().buildDrop().setBurstSize(meterBurstSize).setRate(meterRate).build();
			List<OFMeterBand> meterBands = new ArrayList<OFMeterBand>();
			meterBands.add(ofmb);

			// build meter message
			return factory.buildMeterMod().setCommand(OFMeterModCommand.ADD).setMeterId(meterId).setMeters(meterBands)
					.setFlags(flags).build();
		}

		/*****************************************************
		 * * for build metera modify * caihe * *
		 *****************************************************/

		/**
		 * @author caihe
		 * @param meter
		 * @return modify meter
		 */
		public OFMeterMod meterBuildModify(Meter meter) {
			return meterBuild(OFMeterModCommand.MODIFY, meter);
		}

		public OFMeterMod meterBuildModify(int meterId, long rate, long burst, boolean useKBPS, boolean useStats) {
			return meterBuild(OFMeterModCommand.MODIFY, meterId, rate, burst, useKBPS, useStats);
		}

		/*****************************************************
		 * * for build meter delete caihe * *
		 *****************************************************/

		/**
		 * @author caihe
		 * @param meterID
		 * @return delete by meterID
		 */
		public OFMeterMod meterBuildDelete(int meterID) {
			// return meterBuild(OFMeterModCommand.DELETE, meterID, METER_RATE_DEFAULT,
			// METER_BURST_SIZE_DEFAULT, true, true);
			return factory.buildMeterMod().setCommand(OFMeterModCommand.DELETE).setMeterId(meterID).build();
		}

		/**
		 * delete by meter
		 * 
		 * @param meter
		 * @return
		 */
		public OFMeterMod meterBuildDelete(Meter meter) {
			return meterBuild(OFMeterModCommand.DELETE, meter);
		}

		/**
		 * delete all meter
		 * 
		 * @author caihe
		 * @return
		 */
		public OFMeterMod meterBuildDelete() {

			return factory.buildMeterMod().setCommand(OFMeterModCommand.DELETE)
					.setMeterId(OFMeterSerializerVer13.ALL_VAL).build();
		}

		/*****************************************************
		 * * basic function for build meter add & modify * caihe * *
		 *****************************************************/

		/**
		 * @author caihe
		 * 
		 * @param meterId
		 * @param rate
		 * @param burst
		 * @param useKBPS
		 * @param useStats
		 * @return for single band meter
		 */
		private OFMeterMod meterBuild(OFMeterModCommand command, int meterId, long rate, long burst, boolean useKBPS,
				boolean useStats) {
			Meter meter = new Meter();
			meter.setId(meterId);
			meter.getBands().add(new Band(rate, burst));
			meter.getFlags().add((useKBPS ? OFMeterFlags.KBPS : OFMeterFlags.PKTPS));
			if (useStats)
				meter.getFlags().add(OFMeterFlags.STATS);
			if (burst != 0)
				meter.getFlags().add(OFMeterFlags.BURST);

			return meterBuild(command, meter);
		}

		/**
		 * @author caihe
		 * @param meter
		 * @return OFMeterMod basic build meter add & modify
		 */
		private OFMeterMod meterBuild(OFMeterModCommand command, Meter meter) {

			if (meter == null)
				throw new RuntimeException("[meterBuild " + command + "]: can't be null");

			Set<OFMeterFlags> flags = meter.getFlags();
			if (flags.contains(OFMeterFlags.KBPS) && flags.contains(OFMeterFlags.PKTPS))
				throw new RuntimeException("[meterBuild " + command + "]: Can't contain the PKTPS and KBPS Flag");

			List<OFMeterBand> meterBands = new ArrayList<OFMeterBand>();

			for (Band band : meter.getBands()) {
				OFMeterBandDrop.Builder bandBuilder = factory.meterBands().buildDrop().setRate(band.getRate());

				if (!flags.contains(OFMeterFlags.BURST) && band.getBurstSize() > 0) {
					throw new RuntimeException("[meterBuild " + command + "]: didn't set Flag: Brust");
				} else {
					bandBuilder.setBurstSize(band.getBurstSize());
				}

				meterBands.add(bandBuilder.build());
			}

			return factory.buildMeterMod().setCommand(command).setMeterId(meter.getId()).setMeters(meterBands)
					.setFlags(flags).build();
		}

		/*****************************************************
		 * * query switch the meter features * caihe * *
		 *****************************************************/

		/**
		 * @author caihe
		 * @return query switch meter features stats
		 */
		public OFMeterFeaturesStatsRequest buildMeterFeaturesStatsRequest() {
			return factory.buildMeterFeaturesStatsRequest().build();
		}

		/*****************************************************
		 * * get switch the meter stats * caihe * *
		 *****************************************************/

		public OFMeterStatsRequest buildMeterStatsRequest() {
			return factory.buildMeterStatsRequest().setMeterId(OFMeterSerializerVer13.ALL_VAL).build();
		}

		public OFMeterStatsRequest buildMeterStatsRequest(int meterId) {
			return factory.buildMeterStatsRequest().setMeterId(meterId).build();
		}

		/*****************************************************
		 * * get switch the meter config * caihe * *
		 *****************************************************/
		public OFMeterConfigStatsRequest buildMeterConfigRquest() {
			return factory.buildMeterConfigStatsRequest().setMeterId(OFMeterSerializerVer13.ALL_VAL).build();
		}

		public OFMeterConfigStatsRequest buildMeterConfigRquest(int meterId) {
			return factory.buildMeterConfigStatsRequest().setMeterId(meterId).build();
		}
	}

	/** floodlight module implemention **/
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IMeterService.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		m.put(IMeterService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		log.info("MeterService start up!");
	}

	@Override
	public OFMeterStatsRequest buildMeterStatsRequest() {
		return mc.buildMeterStatsRequest();
	}

	@Override
	public OFMeterConfigStatsRequest buildMeterConfigStatsRequest() {
		return mc.buildMeterConfigRquest();
	}

	@Override
	public OFMeterFeaturesStatsRequest buildMeterFeaturesStatsRequest() {
		return mc.buildMeterFeaturesStatsRequest();
	}

}
