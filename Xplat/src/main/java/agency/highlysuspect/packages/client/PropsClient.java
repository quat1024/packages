package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.config.ConfigProperty;
import agency.highlysuspect.packages.config.ConfigSchema;

public class PropsClient {
	//TODO: category comments
	
	//  "Specify at least 'use' (right click) or 'punch' (left click), and optionally add",
	//	"any combination of 'ctrl', 'alt', or 'sneak' to require some modifier keys.",
	//	"Separate multiple items with hyphens. Disable an action entirely by leaving it blank."
	
	public static final ConfigProperty<String> INSERT_ONE_BINDING_UNPARSED = ConfigProperty.stringOpt("insertOne", "use", "How do you insert one item into the package?");
	public static final ConfigProperty<String> INSERT_STACK_BINDING_UNPARSED = ConfigProperty.stringOpt("insertStack", "sneak-use", "How do you insert one stack of items into the package?");
	public static final ConfigProperty<String> INSERT_ALL_BINDING_UNPARSED = ConfigProperty.stringOpt("insertAll", "ctrl-use", "How do you insert everything in your inventory that fits into the package?");
	public static final ConfigProperty<String> TAKE_ONE_BINDING_UNPARSED = ConfigProperty.stringOpt("takeOne", "punch", "How do you take one item from the package?");
	public static final ConfigProperty<String> TAKE_STACK_BINDING_UNPARSED = ConfigProperty.stringOpt("takeStack", "sneak-punch", "How do you take one stack of items from the package?");
	public static final ConfigProperty<String> TAKE_ALL_BINDING_UNPARSED = ConfigProperty.stringOpt("takeAll", "ctrl-punch", "How do you clear all items from the package?");
	
	public static final ConfigProperty<Integer> PUNCH_REPEAT = ConfigProperty.intOpt(
		"punchRepeat", -1,
		-1, Integer.MAX_VALUE,
		"Older versions of Packages had a \"feature\" where holding left-click would slowly trickle items out of the Package.",
		"This was actually a bug, caused by code intended to differentiatiate between 'starting a left click' and 'continuing",
		"a left click' not working correctly; the delay was my band-aid fix.",
		"I've since actually fixed it, but I don't know if people had gotten used to the broken behavior :)",
		"If you did and want it back, set this to 4."
	);
	
	public static final ConfigProperty<Boolean> RED_BAR_WHEN_FULL = ConfigProperty.boolOpt(
		"redBarWhenFull", true,
		"Packages display a \"durability\" bar corresponding to their fill level. If 'false', the bar is always blue,",
		"and if 'true', the bar will turn red when the package is 100% filled. I think the red color looks nice,",
		"but if you don't care for the discrepancy with the vanilla Bundle item (which is always blue), feel free to turn it off."
	);
	
	public static final ConfigProperty<Boolean> CACHE_MESHES = ConfigProperty.boolOpt(
		"cacheMeshes", true,
		"If 'true', Package and Package Crafter 3d models will be cached in-memory, instead of rebaked from scratch every time.",
		"The model bakery is quite fast, and this probably helps chunk-bake performance less than it sounds like it would.",
		"It also consumes more memory. An F3+T will discard all caches.",
		"However, I'm pretty sure it slightly improves the efficiency of item rendering. All's tradeoffs in love and perf."
	);
	
	public static final ConfigProperty<Boolean> FORGE_SWAP_RED_AND_BLUE = ConfigProperty.boolOpt(
		"swapRedAndBlue", true,
		"Hi Forge players! For some reason, I need this to make the front face of Packages render with the right color",
		"on your modloader. If you need to reset this to 'false', I'd be interested in hearing what mods you're using."
	);
	
	public static final ConfigProperty<Boolean> FABRIC_FREX_SUPPORT = ConfigProperty.boolOpt(
		"frexSupport", true,
		"If 'true' and FREX is loaded, FREX materials will be forwarded through into Packages's custom block models.",
		"Use this if you have funky Canvas shaders. Requires a game restart to activate and deactivate."
	);
	
	public static ConfigSchema visit(ConfigSchema in) {
		in.section("Keys", INSERT_ONE_BINDING_UNPARSED, INSERT_STACK_BINDING_UNPARSED, INSERT_ALL_BINDING_UNPARSED,
			TAKE_ONE_BINDING_UNPARSED, TAKE_STACK_BINDING_UNPARSED, TAKE_ALL_BINDING_UNPARSED,
			PUNCH_REPEAT);
		
		in.section("Pedantry", RED_BAR_WHEN_FULL);
		
		in.section("Model", CACHE_MESHES);
		if(Packages.instance.isForge()) in.option(FORGE_SWAP_RED_AND_BLUE);
		if(Packages.instance.isFabric()) in.option(FABRIC_FREX_SUPPORT);
		
		return in;
	}
}
