package tank.sdnos.qos.meter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFMeterConfig;
import org.projectfloodlight.openflow.protocol.OFMeterConfigStatsRequest;
import org.projectfloodlight.openflow.protocol.OFMeterFeatures;
import org.projectfloodlight.openflow.protocol.OFMeterFeaturesStatsRequest;
import org.projectfloodlight.openflow.protocol.OFMeterStats;
import org.projectfloodlight.openflow.protocol.OFMeterStatsRequest;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.IFloodlightService;

/***
 * 
 * @author root meter service
 *
 */
public interface IMeterService extends IFloodlightService {
	/** meterAddService **/
	public boolean meterAdd(IOFSwitch sw, Meter meter);

	public boolean meterAdd(IOFSwitch sw, int meterId, long rate, long burst, boolean useKBPS, boolean useStats);

	public boolean meterAdd(IOFSwitch sw, int meterId, long rate, boolean useKBPS, boolean useStats);

	public boolean meterAdd(IOFSwitch sw, int meterId, boolean useStats);

	public boolean meterAdd(IOFSwitch sw, long meterId, long meterRate, long meterBurstSize);

	public boolean meterAdd(Set<IOFSwitch> sws, int meterId, long rate, long burst, boolean useKBPS, boolean useStats);

	public boolean meterAdd(Set<IOFSwitch> sws, int meterId, long rate, boolean useKBPS, boolean useStats);

	public boolean meterAdd(Set<IOFSwitch> sws, int meterId, boolean useStats);

	public boolean meterAdd(Set<IOFSwitch> sws, long meterId, long meterRate, long meterBurstSize);

	public boolean meterAdd(Set<IOFSwitch> sws, Meter meter);

	/** meter modify service **/
	public boolean meterModify(IOFSwitch sw, Meter meter);

	public boolean meterModify(IOFSwitch sw, int meterId, long rate, long burst, boolean useKBPS, boolean useStats);

	public boolean meterModify(Set<IOFSwitch> sws, Meter meter);

	public boolean meterModify(Set<IOFSwitch> sws, int meterId, long rate, long burst, boolean useKBPS,
			boolean useStats);

	/** meter delete service **/
	public boolean meterDelete(IOFSwitch sw, int meterID);

	public boolean meterDelete(IOFSwitch sw, Meter meter);

	public boolean meterDelete(Set<IOFSwitch> sws, Meter meter);

	public boolean meterDelete(Set<IOFSwitch> sws, int meterID);

	public boolean meterDelete(IOFSwitch sw);

	public boolean meterDelete(Set<IOFSwitch> sws);

	public OFMeterStatsRequest buildMeterStatsRequest();

	public OFMeterConfigStatsRequest buildMeterConfigStatsRequest();

	public OFMeterFeaturesStatsRequest buildMeterFeaturesStatsRequest();

}
