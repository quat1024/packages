package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.PackagesConfig;
import agency.highlysuspect.packages.platform.CommonPlatformConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeCommonPlatformConfig extends AbstractForgePlatformConfig implements CommonPlatformConfig {
	@Override
	protected void configure(ForgeConfigSpec.Builder bob) {
		bob.push("Features");
		
		inventoryInteractions = bob.comment("Allow interacting with Packages in the player inventory, kinda like a Bundle.").define("inventoryInteractions", true);
		interactionSounds = bob.comment("Play sounds when players interact with a Package.").define("interactionSounds", true);
		
		bob.pop();
	}
	
	@Override
	protected void install() {
		Packages.instance.config = PackagesConfig.makeConfig(this);
	}
	
	@Override
	public void registerAndLoadAndAllThatJazz() {
		setup(ModConfig.Type.COMMON);
	}
	
	private ForgeConfigSpec.ConfigValue<Boolean> inventoryInteractions;
	private ForgeConfigSpec.ConfigValue<Boolean> interactionSounds;
	
	@Override
	public boolean inventoryInteractions() {
		return inventoryInteractions.get();
	}
	
	@Override
	public boolean interactionSounds() {
		return interactionSounds.get();
	}
}
