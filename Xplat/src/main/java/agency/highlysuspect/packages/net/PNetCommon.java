package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.platform.PlatformSupport;

public class PNetCommon {
	public static void onInitialize(PlatformSupport plat) {
		plat.registerActionPacketHandler();
	}
}
