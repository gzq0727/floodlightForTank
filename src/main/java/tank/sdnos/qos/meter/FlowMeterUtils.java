package tank.sdnos.qos.meter;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModify;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionMeter;

public class FlowMeterUtils {

	private final static OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);

	public static OFFlowMod bindMeter(OFFlowMod flowMod, int meterId) {
		OFInstructionMeter meter = factory.instructions().buildMeter().setMeterId(meterId).build();
		flowMod.getInstructions().add(meter);
		return flowMod;
	}
	
	
	public static OFFlowAdd bindMeter(OFFlowAdd flowAdd, int meterId) {
		return (OFFlowAdd)bindMeter((OFFlowMod)flowAdd,meterId);
	}
	
	
		
	public static OFFlowModify bindMeter(OFFlowModify flowModify, int meterId) {
		return (OFFlowModify)bindMeter((OFFlowMod)flowModify,meterId);
	}
}
