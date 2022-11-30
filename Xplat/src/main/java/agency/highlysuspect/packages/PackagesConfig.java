package agency.highlysuspect.packages;

import agency.highlysuspect.packages.platform.CommonPlatformConfig;

public class PackagesConfig {
	public static PackagesConfig makeConfig(CommonPlatformConfig cfgSource) {
		PackagesConfig cfg = new PackagesConfig();
		cfg.inventoryInteractions = cfgSource.inventoryInteractions();
		cfg.interactionSounds = cfgSource.interactionSounds();
		cfg.packageMakerAllowlistMode = cfgSource.packageMakerAllowlistMode();
		return cfg;
	}
	
	//todo: none of these default values get used at all, that's something i should fix
	// default values are an implementation detail of PlatformConfig2
	// (the config situation in this mod is Very Bad)
	public boolean inventoryInteractions = true;
	public boolean interactionSounds = true;
	
	public boolean packageMakerAllowlistMode = false;
}
