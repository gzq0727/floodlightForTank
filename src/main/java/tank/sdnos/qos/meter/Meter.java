package tank.sdnos.qos.meter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFMeterFlags;

import tank.sdnos.qos.meter.Bands.Band;

public class Meter {
	private int id; /* meter ID */
	private Set<OFMeterFlags> flags = new HashSet<OFMeterFlags>(); /*
																	 * meter flags: OFMeterFlags.KBPS OFMeterFlags.PKTPS
																	 * OFMeterFlags.STATS OFMeterFlags.BURST
																	 */
	private Bands bands = new Bands(); /* Bands list: rate & burst value */

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Set<OFMeterFlags> getFlags() {
		return flags;
	}

	public void addFlags(OFMeterFlags flag) {
		flags.add(flag);
	}

	public List<Band> getBands() {
		return bands.getBands();
	}

	@Override
	public String toString() {
		return "Meter: " + id + ", " + flags + ", " + bands;
	}
}
