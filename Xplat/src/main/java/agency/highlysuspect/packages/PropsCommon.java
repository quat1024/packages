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
		"lang keys to describe the behavior you create. See this very mod's en_us.json for some documentation on this."
	);
	
	public static final ConfigProperty<Boolean> DROP_EMPTY_PACKAGES_IN_CREATIVE = ConfigProperty.boolOpt(
		"dropEmptyPackagesInCreative", true,
		"In Creative mode, when you break a Package, it will always drop as an item on the floor - even if it's empty.",
		"This is different from vanilla Shulker Boxes, which only drop when nonempty. I think it's nice to always",
		"get the item, but if you don't care for the discrepancy with vanilla Shulker Boxes, feel free to turn it off."
	);
	
	public static ConfigSchema visit(ConfigSchema in) {
		in.section("Features", INVENTORY_INTERACTIONS, INTERACTION_SOUNDS, PACKAGE_MAKER_ALLOW_LIST_MODE);
		in.section("Pedantry", DROP_EMPTY_PACKAGES_IN_CREATIVE);
		return in;
	}
}
