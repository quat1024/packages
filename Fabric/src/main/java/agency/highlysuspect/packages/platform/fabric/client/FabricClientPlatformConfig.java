package agency.highlysuspect.packages.platform.fabric.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.client.MeshBackend;
import agency.highlysuspect.packages.client.PackageActionBinding;
import agency.highlysuspect.packages.client.PackagesClient;
import agency.highlysuspect.packages.client.PackagesClientConfig;
import agency.highlysuspect.packages.net.PackageAction;
import agency.highlysuspect.packages.platform.ClientPlatformConfig;
import agency.highlysuspect.packages.platform.fabric.AbstractFabricPlatformConfig;
import net.minecraft.server.packs.PackType;

import java.util.List;

public class FabricClientPlatformConfig extends AbstractFabricPlatformConfig implements ClientPlatformConfig {
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
				case "insertOne" -> insertOneBinding = PackageActionBinding.fromString(PackageAction.INSERT_ONE, value);
				case "insertStack" -> insertStackBinding = PackageActionBinding.fromString(PackageAction.INSERT_STACK, value);
				case "insertAll" -> insertAllBinding = PackageActionBinding.fromString(PackageAction.INSERT_ONE, value);
				case "takeOne" -> takeOneBinding = PackageActionBinding.fromString(PackageAction.TAKE_ONE, value);
				case "takeStack" -> takeStackBinding = PackageActionBinding.fromString(PackageAction.TAKE_STACK, value);
				case "takeAll" -> takeAllBinding = PackageActionBinding.fromString(PackageAction.TAKE_ONE, value);
				
				case "punchRepeat" -> punchRepeat = Integer.parseInt(value);
				case "fontVerticalShift" -> fontVerticalShift = Double.parseDouble(value);
				case "meshBackend" -> meshBackend = parseEnum(MeshBackend.class, value);
				case "cacheMeshes" -> cacheMeshes = Boolean.parseBoolean(value);
				case "swapRedAndBlue" -> swapRedAndBlue = Boolean.parseBoolean(value);
				case "frexSupport" -> frexSupport = Boolean.parseBoolean(value);
				
				default -> Packages.LOGGER.warn("unknown config key " + key);
			}
		}
	}
	
	@Override
	protected List<String> write() {
		return List.of(
			"# (This file will be reloaded when loading resource packs with F3+T.)",
			"##########",
			"## Keys ##",
			"##########",
			"",
			"# Specify at least 'use' (right click) or 'punch' (left click), and optionally add",
			"# any combination of 'ctrl', 'alt', or 'sneak' to require some modifier keys.",
			"# Separate multiple items with hyphens. Disable an action entirely by leaving it blank.",
			"# ",
			"# How do you insert one item into the package?",
			"# Default: use",
			"insertOne = " + insertOneBinding.asString(),
			"",
			"# How do you insert one stack of items into the package?",
			"# Default: sneak-use",
			"insertStack = " + insertStackBinding.asString(),
			"",
			"# How do you insert everything in your inventory that fits into the package?",
			"# Default: ctrl-use",
			"insertAll = " + insertAllBinding.asString(),
			"",
			"# How do you take one item from the package?",
			"# Default: punch",
			"takeOne = " + takeOneBinding.asString(),
			"",
			"# How do you take one stack of items from the package?",
			"# Default: sneak-punch",
			"takeStack = " + takeStackBinding.asString(),
			"",
			"# How do you clear all items from the package?",
			"# Default: ctrl-punch",
			"takeAll = " + takeAllBinding.asString(),
			"",
			"# Older versions of Packages had a \"feature\" where holding left-click would slowly trickle items out of the Package.",
			"# This was actually a bug, caused by code intended to differentiatiate between 'starting a left click' and 'continuing",
			"# a left click' not working correctly; the delay was my band-aid fix.",
			"# I've since actually fixed it, but I don't know if people had gotten used to the broken behavior :)",
			"# If you did and want it back, set this to 4.",
			"# Default: -1",
			"punchRepeat = " + punchRepeat,
			"",
			"###########",
			"## Model ##",
			"###########",
			"",
			"# Vertically shift the numeric display on Packages up by this many blocks.",
			"# Nudge this to recenter fonts with a different baseline from vanilla.",
			"# Default: 0",
			"fontVerticalShift = " + fontVerticalShift,
			"",
			"# The method Packages uses to render its models. After changing the option, reload resources (F3+T) uhh, twice (models get loaded before the config file :cry:)",
			"# Possible values:",
			"# 'frapi_mesh' uses many features from Fabric Renderer API.",
			"#    It has received the most testing and performs surprisingly well. It has special compatibility features with FREX.",
			"#    If you have Sodium, you need Indium to use frapi_mesh.",
			"# 'frapi_bakedquad' uses a small amount of the Fabric Renderer API.",
			"#    It doesn't perform as well, but may be more compatible. It doesn't have compat with FREX.",
			"#    If you have Sodium, you also need Indium to use frapi_bakedquad.",
			"# 'skip' bypasses the custom model pipeline entirely. Packages will render its blocks using placeholder textures.",
			"#    This is intended for debugging, resource-packmaking, getting into the world if the other renderers have a crash bug, etc.",
			"# Default: FRAPI_MESH",
			"meshBackend = " + writeEnum(meshBackend),
			"",
			"# If 'true', Package and Package Crafter 3d models will be cached in-memory, instead of rebaked from scratch every time.",
			"# With the frapi_mesh backend, this probably helps performance less than it sounds like it would.",
			"# Might make more of a difference on other backends.",
			"# Default: false",
			"cacheMeshes = " + cacheMeshes,
			"",
			"# This is needed to make the front face of Packages render with the correct color on Forge. It shouldn't be needed here,",
			"# but it doesn't hurt to add the option. If you need to change it, I'd be interested in hearing what mods you're using.",
			"# Also this only does anything on the bakedquad model backend.",
			"# Default: false",
			"swapRedAndBlue = " + swapRedAndBlue,
			"",
			"# If 'true' and FREX is loaded, materials on block models will be forwarded into the various Packages block models.",
			"# Use this if you have funky Canvas shaders that make blocks glow, or whatnot.",
			"# Requires a game restart to activate and deactivate.",
			"# Default: true",
			"frexSupport = " + frexSupport
		);
	}
	
	@Override
	protected void install() {
		PackagesClient.instance.config = PackagesClientConfig.makeConfig(this);
	}
	
	@Override
	public void registerAndLoadAndAllThatJazz() {
		setup(PackType.CLIENT_RESOURCES, "packages-client.cfg");
	}
	
	private PackageActionBinding insertOneBinding = new PackageActionBinding.Builder(PackageAction.INSERT_ONE).use().build();
	private PackageActionBinding insertStackBinding = new PackageActionBinding.Builder(PackageAction.INSERT_STACK).use().sneak().build();
	private PackageActionBinding insertAllBinding = new PackageActionBinding.Builder(PackageAction.INSERT_ALL).use().ctrl().build();
	private PackageActionBinding takeOneBinding = new PackageActionBinding.Builder(PackageAction.TAKE_ONE).punch().build();
	private PackageActionBinding takeStackBinding = new PackageActionBinding.Builder(PackageAction.TAKE_STACK).punch().sneak().build();
	private PackageActionBinding takeAllBinding = new PackageActionBinding.Builder(PackageAction.TAKE_ALL).punch().ctrl().build();
	
	private int punchRepeat = -1;
	private double fontVerticalShift = 0;
	private MeshBackend meshBackend = MeshBackend.FRAPI_MESH;
	private boolean cacheMeshes = false;
	private boolean swapRedAndBlue = false;
	private boolean frexSupport = true;
	
	@Override
	public PackageActionBinding insertOneBinding() {
		return insertOneBinding;
	}
	
	@Override
	public PackageActionBinding insertStackBinding() {
		return insertStackBinding;
	}
	
	@Override
	public PackageActionBinding insertAllBinding() {
		return insertAllBinding;
	}
	
	@Override
	public PackageActionBinding takeOneBinding() {
		return takeOneBinding;
	}
	
	@Override
	public PackageActionBinding takeStackBinding() {
		return takeStackBinding;
	}
	
	@Override
	public PackageActionBinding takeAllBinding() {
		return takeAllBinding;
	}
	
	@Override
	public int punchRepeat() {
		return punchRepeat;
	}
	
	@Override
	public double fontVerticalShift() {
		return fontVerticalShift;
	}
	
	@Override
	public MeshBackend meshBackend() {
		return meshBackend;
	}
	
	@Override
	public boolean cacheMeshes() {
		return cacheMeshes;
	}
	
	@Override
	public boolean swapRedAndBlue() {
		return swapRedAndBlue;
	}
	
	@Override
	public boolean frexSupport() {
		return frexSupport;
	}
}