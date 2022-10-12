package agency.highlysuspect.packages.platform.fabric;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.PackagesConfig;
import agency.highlysuspect.packages.platform.CommonPlatformConfig;
import net.minecraft.server.packs.PackType;

import java.util.List;

public class FabricCommonPlatformConfig extends AbstractFabricPlatformConfig implements CommonPlatformConfig {
	@Override
	protected boolean parseKeyValuePair(String key, String value) {
		switch(key) {
			case "inventoryInteractions" -> inventoryInteractions = Boolean.parseBoolean(value);
			case "interactionSounds" -> interactionSounds = Boolean.parseBoolean(value);
			default -> { return false; }
		}
		return true;
	}
	
	@Override
	protected List<String> write() {
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
