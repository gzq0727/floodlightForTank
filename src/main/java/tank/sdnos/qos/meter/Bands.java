package tank.sdnos.qos.meter;

import java.util.ArrayList;
import java.util.List;

public class Bands {
	private List<Band> bands = new ArrayList<Band>();
	
	public void addBand(long rate,long burstSize) {
		bands.add(new Band(rate,burstSize));
	}
	
	public List<Band> getBands(){
		return bands;
	}

	public String toString() {
		return bands+"";
	}
	public static class Band {
		private long rate;
		private long burstSize;
		public long getRate() {
			return rate;
		}
		public long getBurstSize() {
			return burstSize;
		}
		
		public Band(long rate , long burstSize){
			this.rate = rate;
			this.burstSize = burstSize;
		}
		
		public String toString() {
			return "Band: "+rate+", "+burstSize;
		}
		
	}
}
