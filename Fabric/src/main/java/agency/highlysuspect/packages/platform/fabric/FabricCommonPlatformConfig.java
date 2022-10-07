package agency.highlysuspect.packages.platform.fabric;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.PackagesConfig;
import agency.highlysuspect.packages.platform.CommonPlatformConfig;
import net.minecraft.server.packs.PackType;

import java.util.List;

public class FabricCommonPlatformConfig extends AbstractFabricPlatformConfig implements CommonPlatformConfig {
	@Override
	protected void parse(List<String> lines) {
		for(String line : lines) {
			line = line.trim();
			if(line.startsWith("#")) continue; //comments
			
			//Split on key-value pairs
			int eqIndex = line.indexOf('=');
			if(eqIndex == -1) continue;
			String key = line.substring(0, eqIndex).trim();
			String value = line.substring(eqIndex + 1).trim();
			
			//dispatch to the correct field
			//Todo this needs Way better error handling/recovery
			switch(key) {
				case "inventoryInteractions" -> inventoryInteractions = Boolean.parseBoolean(value);
				case "interactionSounds" -> interactionSounds = Boolean.parseBoolean(value);
				
				default -> Packages.LOGGER.warn("unknown config key " + key);
			}
		}
	}
	
	@Override
	protected List<String> write() {
		//todo this sucks SHIT !
		return List.of(
			"# (This file will be reloaded when loading data packs with /reload.)",
			"##############",
			"## Features ##",
			"##############",
			"",
			"# Allow interacting with Packages in the player inventory, kinda like a bundle.",
			"# Default: true",
			"inventoryInteractions = " + inventoryInteractions,
			"",
			"# Play sounds when players interact with a Package.",
			"# Default: true",
			"interactionSounds = " + interactionSounds,
			"",
			"# (Other values have moved to packages-client.cfg! Sorry for the trouble)"
		);
	}
	
	@Override
	protected void install() {
		Packages.instance.config = PackagesConfig.makeConfig(this);
	}
	
	@Override
	public void registerAndLoadAndAllThatJazz() {
		setup(PackType.SERVER_DATA, "packages-common.cfg");
	}
	
	private boolean inventoryInteractions = true;
	private boolean interactionSounds = true;
	
	@Override
	public boolean inventoryInteractions() {
		return inventoryInteractions;
	}
	
	@Override
	public boolean interactionSounds() {
		return interactionSounds;
	}
}
