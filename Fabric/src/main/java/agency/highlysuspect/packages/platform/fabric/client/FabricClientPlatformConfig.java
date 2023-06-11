package agency.highlysuspect.packages.platform.fabric.client;

import agency.highlysuspect.packages.client.PackageActionBinding;
import agency.highlysuspect.packages.client.PackagesClient;
import agency.highlysuspect.packages.client.PackagesClientConfig;
import agency.highlysuspect.packages.net.PackageAction;
import agency.highlysuspect.packages.platform.client.ClientPlatformConfig;
import agency.highlysuspect.packages.platform.fabric.AbstractFabricPlatformConfig;
import net.minecraft.server.packs.PackType;

import java.util.List;

public class FabricClientPlatformConfig extends AbstractFabricPlatformConfig implements ClientPlatformConfig {
	@Override
	protected boolean parseKeyValuePair(String key, String value) {
		switch(key) {
			case "insertOne" -> insertOneBinding = PackageActionBinding.fromString(PackageAction.INSERT_ONE, value);
			case "insertStack" -> insertStackBinding = PackageActionBinding.fromString(PackageAction.INSERT_STACK, value);
			case "insertAll" -> insertAllBinding = PackageActionBinding.fromString(PackageAction.INSERT_ALL, value);
			case "takeOne" -> takeOneBinding = PackageActionBinding.fromString(PackageAction.TAKE_ONE, value);
			case "takeStack" -> takeStackBinding = PackageActionBinding.fromString(PackageAction.TAKE_STACK, value);
			case "takeAll" -> takeAllBinding = PackageActionBinding.fromString(PackageAction.TAKE_ALL, value);
			
			case "punchRepeat" -> punchRepeat = Integer.parseInt(value);
			case "cacheMeshes" -> cacheMeshes = Boolean.parseBoolean(value);
			case "frexSupport" -> frexSupport = Boolean.parseBoolean(value);
			
			default -> { return false; }
		}
		return true;
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
			"",
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
			"# If 'true', Package and Package Crafter 3d models will be cached in-memory, instead of rebaked from scratch every time.",
			"# The model bakery is quite fast; this probably helps chunk-bake performance less than it sounds like it would, and consumes memory.",
			"# However I'm pretty sure it slightly improves the efficiency of item rendering. All's tradeoffs.",
			"# F3+T will dump the cache.",
			"# Default: false",
			"cacheMeshes = " + cacheMeshes,
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
	
	private boolean cacheMeshes = false;
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
	public boolean cacheMeshes() {
		return cacheMeshes;
	}
	
	@Override
	public boolean swapRedAndBlue() {
		return false; //Red-blue swaps aren't needed on the FrapiMeshModelBakery implementation.
	}
	
	@Override
	public boolean frexSupport() {
		return frexSupport;
	}
}
