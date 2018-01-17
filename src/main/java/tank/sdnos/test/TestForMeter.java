package tank.sdnos.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMeterConfig;
import org.projectfloodlight.openflow.protocol.OFMeterFlags;
import org.projectfloodlight.openflow.protocol.OFMeterStats;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBandDrop;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.routing.IRoutingService;
import tank.sdnos.monitor.ISwitchStatisticsCollector;
import tank.sdnos.qos.meter.Bands;
import tank.sdnos.qos.meter.IMeterService;
import tank.sdnos.qos.meter.Meter;

public class TestForMeter {

	File file = new File("testLog.txt");
	FileWriter fw = null;
	BufferedWriter writer = null;
	IMeterService meterService;
	IOFSwitchService swService;
	Logger log;
	Set<IOFSwitch> sws = new HashSet<IOFSwitch>();
	IRoutingService routingEngineService;
	IFloodlightProviderService floodlightProvider;
	ISwitchStatisticsCollector statsService;
	private final OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);

	public TestForMeter(IMeterService meterService, IOFSwitchService swService, Logger log,
			IFloodlightProviderService floodlightProvider, ISwitchStatisticsCollector statsService) {
		this.meterService = meterService;
		this.log = log;
		this.swService = swService;
		this.statsService = statsService;
		try {
			file.createNewFile();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	// start test
	public void startTest() {

		log.error("Start to test...");
		log.error("log to " + file.getAbsolutePath());
		for (DatapathId sw : swService.getAllSwitchDpids()) {
			sws.add(swService.getSwitch(sw));
		}

		for (Method m : TestForMeter.class.getDeclaredMethods()) {
			// log.info(Arrays.asList(m.getAnnotations()).toString());
			if (m.getAnnotation(SDNTest.class) != null) {
				write("**********************************************");
				write("Test " + m.getName());
				log.error(m.getName());
				m.setAccessible(true);
				try {
					m.invoke(this, new Object[] {});
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error(e.getMessage());
				}
				write("");
				if (m.getAnnotation(SDNTest.class).needClear()) {
					clearAll();
				}
			}
		}
		log.error("Test Finish!");
	}

	// test insert ok?
	@SDNTest
	private void testMeterInsert() {
		for (IOFSwitch sw : sws) {
			meterService.meterAdd(sw, 0x01, true);
			write("Send " + sw.getId() + " meter: 0x01");
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}

		boolean isContain = false;
		for (IOFSwitch sw : sws) {
			List<OFMeterStats> meters = statsService.getMeterStats(sw);
			isContain = false;
			for (OFMeterStats m : meters) {
				if (m.getMeterId() == 0x01) {
					isContain = true;
				}
			}
			if (isContain == false) {
				write("Test Failed: " + sw.getId() + " not meter 0x01");
			} else {
				write("Test Success: " + sw.getId() + " meter 0x01");
			}
		}
	}

	// test modify meter
	@SDNTest
	public void testModifyMeter() {
		testMeterInsert();

		for (IOFSwitch sw : sws) {
			meterService.meterModify(sw, 0x01, 1000, 0, true, true);
			write("Modify meter: 0x01 from " + sw.getId());
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}

		boolean isOk = false;
		for (IOFSwitch sw : sws) {
			isOk = false;
			for (OFMeterConfig m : statsService.getMeter(sw)) {
				if (1000 == ((OFMeterBandDrop) m.getEntries().get(0)).getRate()) {
					isOk = true;
				}
			}
			if (!isOk) {
				write("Test Failed: " + sw.getId() + " didn't modify meter 0x01");
			} else {
				write("Test Success: " + sw.getId() + " modify meter 0x01");
			}
		}
	}

	// test delete meter
	@SDNTest
	public void testDeleteMeter() {
		testMeterInsert();

		for (IOFSwitch sw : sws) {
			meterService.meterDelete(sw, 0x01);
			write("Delete meter: 0x01 from " + sw.getId());
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}

		boolean isContain = false;
		for (IOFSwitch sw : sws) {
			isContain = false;
			for (OFMeterStats m : statsService.getMeterStats(sw)) {
				if (m.getMeterId() == 0x01) {
					isContain = true;
				}
			}
			if (isContain) {
				write("Test Failed: " + sw.getId() + " didn't delete meter 0x01");
			} else {
				write("Test Success: " + sw.getId() + " delete meter 0x01");
			}
		}
	}

	// test meter table capacity
	public void testCapacityOfMeterTable(Map<IOFSwitch, Meter> map) {
		log.error("test capcity of meter table");
		int cnt = 0;
		Map<DatapathId, Long> meterCnt = new HashMap<DatapathId, Long>();
		Map<DatapathId, Boolean> meterFail = new HashMap<DatapathId, Boolean>();

		for (IOFSwitch sw : sws) {
			int capacity = (int) statsService.getMeterFeaturesStats(sw).getMaxMeter() - 1;
			log.info("capacity: " + capacity);
			for (int i = 1; i <= capacity; ++i) {
				map.get(sw).setId(i);
				meterService.meterAdd(sw, map.get(sw));
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			}
			meterCnt.put(sw.getId(), (long) capacity);
			meterFail.put(sw.getId(), false);
		}
		log.info("Too many meters send finish!!");
		try {
			Thread.sleep(30000); // sleep for 2 seconds
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}

		// write("Add to all switch: " + meterId);
		do {
			for (IOFSwitch sw : sws) {
				log.info("meterService size: " + statsService.getMeter(sw).size());
				if (statsService.getMeter(sw).size() == meterCnt.get(sw.getId())) {
					meterFail.put(sw.getId(), true);
				} else {
					meterCnt.put(sw.getId(), (long) statsService.getMeter(sw).size());
					meterFail.put(sw.getId(), false);
				}
			}

			for (IOFSwitch s : sws) {
				if (!meterFail.get(s.getId()))
					cnt++;
			}
			if (cnt == sws.size())
				break;
			cnt = 0;

			// send
			for (IOFSwitch s : sws) {
				if (meterFail.get(s.getId())) {
					log.info(s.getId() + ": +1");
					meterService.meterAdd(s, (int) (meterCnt.get(s.getId()) + 1), true);
					meterCnt.put(s.getId(), (meterCnt.get(s.getId()) + 1));
				}
			}

			try {
				Thread.sleep(11000); // sleep for 2 seconds
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}

		} while (true);

		write("meterMax: " + meterCnt.toString());
		statsService.setUpdateInTime(true);
	}

	// test Flags == Packet
	@SDNTest(needClear = false)
	public void testPacket() {
		meterService.meterAdd(sws, 0x01, 100, false, true);
	}

	// test the capacity of meter bands
	@SDNTest
	public void testBandsCapacity() {
		int cnt = 0;
		Map<DatapathId, Long> bandCnt = new HashMap<DatapathId, Long>();
		Map<DatapathId, Boolean> bandFail = new HashMap<DatapathId, Boolean>();

		Map<IOFSwitch, Meter> bandMeter = new HashMap<IOFSwitch, Meter>();

		for (IOFSwitch sw : sws) {
			int capacity = (int) statsService.getMeterFeaturesStats(sw).getMaxBands();
			log.info("band capacity: " + capacity);
			Meter meter = new Meter();
			for (int i = 1; i <= capacity; ++i) {
				meter.setId(0x01);
				meter.addFlags(OFMeterFlags.KBPS);
				meter.addFlags(OFMeterFlags.BURST);
				meter.addFlags(OFMeterFlags.STATS);
				meter.getBands().add(new Bands.Band(100 * i, 100 * i));
				bandMeter.put(sw, meter);
			}
			meterService.meterAdd(sw, bandMeter.get(sw));
			bandCnt.put(sw.getId(), (long) capacity);
			bandFail.put(sw.getId(), false);
		}
		log.info("Too many bands send finish!!");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}

		// write("Add to all switch: " + meterId);
		do {
			for (IOFSwitch sw : sws) {
				log.info("meterService bandsize: " + statsService.getMeter(sw, 0x01).getEntries().size());
				if (statsService.getMeter(sw, 0x01).getEntries().size() == bandCnt.get(sw.getId())) {
					bandFail.put(sw.getId(), true);
				} else {
					bandMeter.get(sw).getBands().remove(bandMeter.get(sw).getBands().size() - 1);
					bandCnt.put(sw.getId(), (long) statsService.getMeter(sw, 0x01).getEntries().size());
					bandFail.put(sw.getId(), false);
				}
			}

			for (IOFSwitch s : sws) {
				if (!bandFail.get(s.getId()))
					cnt++;
			}
			if (cnt == sws.size())
				break;
			cnt = 0;

			// send
			for (IOFSwitch s : sws) {
				if (bandFail.get(s.getId())) {
					log.info(s.getId() + ": band +1");
					Meter meter = bandMeter.get(s);
					long lastBand = meter.getBands().get(meter.getBands().size() - 1).getRate();
					meter.getBands().add(new Bands.Band(lastBand + 100, lastBand + 100));
					meterService.meterAdd(s, meter);
					bandCnt.put(s.getId(), (bandCnt.get(s.getId()) + 1));
				}
			}

			try {
				Thread.sleep(500); // sleep for 2 seconds
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}

		} while (true);
		write("bands: " + bandCnt.toString());

		statsService.setUpdateInTime(false);
		testCapacityOfMeterTable(bandMeter);
	}

	// test insert and relation flow entry ok?
	private void testFlowEntryWithMeter() {
		testMeterInsert();
	}

	public void write(String s) {

		try {
			if (fw == null)
				fw = new FileWriter(file);
			if (writer == null)
				writer = new BufferedWriter(fw);
			writer.write(s);
			writer.newLine();// 换行
			writer.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
	}

	/**
	 * @author caihe clear all
	 */
	private void clearAll() {
		meterService.meterDelete(sws);
		write("clear all");
	}
}
