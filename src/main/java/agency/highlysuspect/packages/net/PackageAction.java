package agency.highlysuspect.packages.net;

public enum PackageAction {
	//Insert or remove one item from the barrel.
	STACK((byte) 0),
	
	//Insert or remove a full stack of items from the barrel.
	ONE((byte) 1);
	
	public final byte netValue;
	
	PackageAction(byte netValue) {
		this.netValue = netValue;
	}
	
	static PackageAction get(byte netValue) {
		if(netValue < PackageAction.values().length) return PackageAction.values()[netValue];
		else return ONE;
	}
}