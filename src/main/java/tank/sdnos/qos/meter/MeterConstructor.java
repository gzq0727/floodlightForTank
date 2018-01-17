package tank.sdnos.qos.meter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowStatsRequest;
import org.projectfloodlight.openflow.protocol.OFMeterConfigStatsRequest;
import org.projectfloodlight.openflow.protocol.OFMeterFeaturesStatsRequest;
import org.projectfloodlight.openflow.protocol.OFMeterFlags;
import org.projectfloodlight.openflow.protocol.OFMeterMod;
import org.projectfloodlight.openflow.protocol.OFMeterModCommand;
import org.projectfloodlight.openflow.protocol.OFMeterStatsRequest;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBand;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBandDrop;
import org.projectfloodlight.openflow.protocol.ver13.OFMeterSerializerVer13;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;

import tank.sdnos.qos.meter.Bands.Band;
public class MeterConstructor {

	private final static int METER_RATE = 100; // meter rate (kbps)
	private final static int METER_BURST_SIZE = METER_RATE * 1; // meter burst size (kilobits)
	private final static OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);

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
	public static OFMeterMod meterBuildAdd(int meterId, long rate, long burst, boolean useKBPS, boolean useStats) {
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
	public static OFMeterMod meterBuildAdd(int meterId, long rate, boolean useKBPS, boolean useStats) {
		return meterBuild(OFMeterModCommand.ADD, meterId, rate, 0, useKBPS, useStats);
	}

	/**
	 * @author caihe
	 * @return OFMeterMod use default meter rate k00kbps, burst_size 100kb
	 * 
	 */
	public static OFMeterMod meterBuildAdd(int meterId, boolean useStats) {
		return meterBuild(OFMeterModCommand.ADD, meterId, METER_RATE, METER_BURST_SIZE, true, useStats);
	}

	/**
	 * 
	 * @param meterRate
	 * @param meterBurstSize
	 * @return OFMeterMod
	 */
	public static OFMeterMod meterBuildAdd(long meterId, long meterRate, long meterBurstSize) {
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
	public static OFMeterMod meterBuildModify(Meter meter) {
		return meterBuild(OFMeterModCommand.MODIFY, meter);
	}

	public static OFMeterMod meterBuildModify(int meterId, long rate, long burst, boolean useKBPS, boolean useStats) {
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
	public static OFMeterMod meterBuildDelete(int meterID) {

		return factory.buildMeterMod().setCommand(OFMeterModCommand.DELETE).setMeterId(meterID).build();
	}

	/**
	 * @author caihe
	 * @param meter
	 * @return delete meter
	 */
	public static OFMeterMod meterBuildDelete(Meter meter) {

		if (meter == null)
			throw new RuntimeException("[meterBuild DELETE]: meter can not be null");
		return factory.buildMeterMod().setCommand(OFMeterModCommand.DELETE).setMeterId(meter.getId()).build();
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
	private static OFMeterMod meterBuild(OFMeterModCommand command, int meterId, long rate, long burst, boolean useKBPS,
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
	private static OFMeterMod meterBuild(OFMeterModCommand command, Meter meter) {

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
	public static OFMeterFeaturesStatsRequest buildMeterFeaturesStatsRequest() {
		return factory.buildMeterFeaturesStatsRequest().build();
	}

	/*****************************************************
	 * * get switch the meter stats * caihe * *
	 *****************************************************/

	public static OFMeterStatsRequest buildMeterStatsRequest() {
		return factory.buildMeterStatsRequest().setMeterId(OFMeterSerializerVer13.ALL_VAL).build();
	}

	public static OFMeterStatsRequest buildMeterStatsRequest(int meterId) {

		return factory.buildMeterStatsRequest().setMeterId(meterId).build();
	}

	public static OFFlowStatsRequest build() {
		Match match = factory.buildMatch().build();
		return factory.buildFlowStatsRequest()
        .setMatch(match)
        .setOutPort(OFPort.ANY)
        .setTableId(TableId.ALL)
        .setOutGroup(OFGroup.ANY)
        .build();
	}

	public static OFFlowMod buildFlow() {
		List<OFAction> acs = new ArrayList<OFAction>();
		acs.add(factory.actions().buildOutput().setPort(OFPort.CONTROLLER).build());
		OFInstructionApplyActions output = factory.instructions().buildApplyActions().setActions(acs).build();
		List<OFInstruction> instructions = new ArrayList<OFInstruction>();
		instructions.add((OFInstruction) Collections.singletonList((OFAction) output));
		OFFlowAdd flowAdd = factory.buildFlowAdd()
			    /* set anything else you need, e.g. match */
			    .setInstructions(instructions)
			    .build();
		return flowAdd;
	}
	//
	public static OFMeterConfigStatsRequest buildMeterConfigRequest() {
		return factory.buildMeterConfigStatsRequest().setMeterId(OFMeterSerializerVer13.ALL_VAL).build();
	}
}
