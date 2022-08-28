package agency.highlysuspect.packages.net;

import net.minecraft.network.FriendlyByteBuf;

public enum PackageAction {
	//Insert one item into the package.
	INSERT_ONE,
	
	//Insert everything in your hand into the package.
	INSERT_STACK,
	
	//Take one item out of the package.
	TAKE_ONE,
	
	//Take a stack of items out of the package.
	TAKE_STACK;
	
	public boolean isInsert() {
		return this == INSERT_ONE || this == INSERT_STACK;
	}
	
	void write(FriendlyByteBuf buf) {
		buf.writeByte(ordinal());
	}
	
	static PackageAction get(int netValue) {
		if(netValue < PackageAction.values().length) return PackageAction.values()[netValue];
		else return TAKE_ONE; //shrug
	}
}