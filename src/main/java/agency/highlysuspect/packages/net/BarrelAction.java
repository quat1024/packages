package agency.highlysuspect.packages.net;

public enum BarrelAction {
	//Insert or remove one item from the barrel.
	STACK((byte) 0),
	
	//Insert or remove a full stack of items from the barrel.
	ONE((byte) 1);
	
	public final byte netValue;
	
	BarrelAction(byte netValue) {
		this.netValue = netValue;
	}
	
	static BarrelAction get(byte netValue) {
		if(netValue < BarrelAction.values().length) return BarrelAction.values()[netValue];
		else return ONE;
	}
}