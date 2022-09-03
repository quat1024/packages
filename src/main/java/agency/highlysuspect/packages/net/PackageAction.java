package agency.highlysuspect.packages.net;

import net.minecraft.network.FriendlyByteBuf;

public enum PackageAction {
	//Insert one item into the package.
	INSERT_ONE,
	
	//Insert up to one stack of items into the package.
	INSERT_STACK,
	
	//Insert all matching items in the player's inventory into the package.
	INSERT_ALL,
	
	//Take one item out of the package.
	TAKE_ONE,
	
	//Take a stack of items out of the package.
	TAKE_STACK,
	
	//Take everything out of the package.
	TAKE_ALL,
	;
	
	public boolean isInsert() {
		return this == INSERT_ONE || this == INSERT_STACK || this == INSERT_ALL;
	}
	
	void write(FriendlyByteBuf buf) {
		buf.writeByte(ordinal());
	}
	
	static PackageAction get(int netValue) {
		if(netValue < PackageAction.values().length) return PackageAction.values()[netValue];
		else return TAKE_ONE; //shrug
	}
}