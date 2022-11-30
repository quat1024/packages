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
		
		packageMakerAllowlistMode = bob.comment(
			"If 'true', the item tags `packages:allowlist_package_maker_frame` and `packages:allowlist_package_maker_inner`",
			"will be read, and only the items specified in those tags will be allowed to enter the Frame and Core slots of",
			"the Package Maker. This is mainly for modpackers to mess with; you should also probably add something to the tooltip",
			"lang keys (`packages.package_maker.frame.X`, where X is a one-indexed line number) to describe the behavior you create."
			).define("packageMakerAllowlistMode", false);
		
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
	
	private ForgeConfigSpec.ConfigValue<Boolean> packageMakerAllowlistMode;
	
	@Override
	public boolean inventoryInteractions() {
		return inventoryInteractions.get();
	}
	
	@Override
	public boolean interactionSounds() {
		return interactionSounds.get();
	}
	
	@Override
	public boolean packageMakerAllowlistMode() {
		return packageMakerAllowlistMode.get();
	}
}
