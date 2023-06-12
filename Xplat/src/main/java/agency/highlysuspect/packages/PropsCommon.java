package agency.highlysuspect.packages;

import agency.highlysuspect.packages.config.ConfigProperty;
import agency.highlysuspect.packages.config.ConfigSchema;

public class PropsCommon {
	public static final ConfigProperty<Boolean> INVENTORY_INTERACTIONS = ConfigProperty.boolOpt(
		"inventoryInteractions", true,
		"Allow interacting with Packages in the player inventory, kinda like a Bundle."
	);
	
	public static final ConfigProperty<Boolean> INTERACTION_SOUNDS = ConfigProperty.boolOpt(
		"interactionSounds", true,
		"Play sounds when players interact with a Package."
	);
	
	public static final ConfigProperty<Boolean> PACKAGE_MAKER_ALLOW_LIST_MODE = ConfigProperty.boolOpt(
		"packageMakerAllowlistMode", false,
		"If 'true', the item tags `packages:allowlist_package_maker_frame` and `packages:allowlist_package_maker_inner`",
		"will be read, and only the items specified in those tags will be allowed to enter the Frame and Core slots of",
		"the Package Maker. This is mainly for modpackers to mess with; you should also probably add something to the tooltip",
		"lang keys (`packages.package_maker.frame.X`, where X is a one-indexed line number) to describe the behavior you create."
	);
	
	public static ConfigSchema visit(ConfigSchema in) {
		in.section("Features", INVENTORY_INTERACTIONS, INTERACTION_SOUNDS, PACKAGE_MAKER_ALLOW_LIST_MODE);
		return in;
	}
}
