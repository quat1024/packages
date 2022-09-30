package agency.highlysuspect.packages.config;

import agency.highlysuspect.packages.net.PackageAction;
import agency.highlysuspect.packages.platform.PlatformSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class PackagesConfig {
	public static PlatformConfig makePlatformConfig(PlatformSupport plat) {
		return plat.makePlatformConfigBuilder()
			.addIntProperty("configVersion", 0)
			
			.setSection("Features")
			.addBooleanProperty("inventoryInteractions", true, "Allow interacting with Packages in the player inventory, kinda like a Bundle.")
			.addBooleanProperty("interactionSounds", true, "Play sounds when players interact with a Package.")
			
			.setSection("Client")
			.addStringProperty("insertOne", "use",
				"Specify at least 'use' (right click) or 'punch' (left click), and optionally add",
				"any combination of 'ctrl', 'alt', or 'sneak' to require some modifier keys.",
				"Separate multiple items with hyphens. Disable an action entirely by leaving it blank.",
				"",
				"How do you insert one item into the package?")
			.addStringProperty("insertStack", "sneak-use", "How do you insert one stack of items into the package?")
			.addStringProperty("insertAll", "ctrl-use", "How do you insert everything in your inventory that fits into the package?")
			.addStringProperty("takeOne", "punch", "How do you take one item from the package?")
			.addStringProperty("takeStack", "sneak-punch", "How do you take one stack of items from the package?")
			.addStringProperty("takeAll", "ctrl-punch", "How do you clear all items from the package?")
			
			.addIntProperty("punchRepeat", -1,
				"Older versions of Packages had a \"feature\" where holding left-click would slowly trickle items out of the Package.",
				"This was actually a bug, caused by code intended to differentiatiate between 'starting a left click' and 'continuing",
				"a left click' not working correctly; the delay was my band-aid fix.",
				"I've since actually fixed it, but I don't know if people had gotten used to the broken behavior :)",
				"If you did and want it back, set this to 4.")
			.addDoubleProperty("fontVerticalShift", 0,
				"Vertically shift the numeric display on Packages up by this many blocks.",
				"Nudge this to recenter fonts with a different baseline from vanilla.")
			
			.addEnumProperty("meshBackend", MeshBackend.FRAPI_MESH,
				"The method Packages uses to render its models. After changing the option, reload resources (F3+T) uhh, twice (models get loaded before the config file :cry:)",
				"Possible values:",
				"'frapi_mesh' uses many features from Fabric Renderer API.",
				"   It has received the most testing and performs surprisingly well. It has special compatibility features with FREX.",
				"   If you have Sodium, you need Indium to use frapi_mesh.",
				"'frapi_bakedquad' uses a small amount of the Fabric Renderer API.",
				"   It doesn't perform as well, but may be more compatible. It doesn't have compat with FREX.",
				"   If you have Sodium, you also need Indium to use frapi_bakedquad.",
				"'skip' bypasses the custom model pipeline entirely. Packages will render its blocks using placeholder textures.",
				"   This is intended for debugging, resource-packmaking, getting into the world if the other renderers have a crash bug, etc.")
			.addBooleanProperty("cacheMeshes", false,
				"If 'true', Package and Package Crafter 3d models will be cached in-memory, instead of rebaked from scratch every time.",
				"With the frapi_mesh backend, this probably helps performance less than it sounds like it would.",
				"Might make more of a difference on other backends.")
			.addBooleanProperty("frexSupport", true,
				"If 'true' and FREX is loaded, materials on block models will be forwarded into the various Packages block models.",
				"Use this if you have funky Canvas shaders that make blocks glow, or whatnot.",
				"Requires a game restart to activate and deactivate.")
			
			.build(PlatformConfigBuilder.ConfigType.COMMON); //TODO split the configs !
	}
	
	@SuppressWarnings("unused")
	private int configVersion = 0;
	
	public boolean inventoryInteractions = true;
	public boolean interactionSounds = true;
	
	@PackageActionBinding.For(PackageAction.INSERT_ONE) //Weird annotation needed so the deserializer knows which PackageAction it's for... isn't great, but it'll do
	public PackageActionBinding insertOne = new PackageActionBinding.Builder(PackageAction.INSERT_ONE).use().build();
	@PackageActionBinding.For(PackageAction.INSERT_STACK)
	public PackageActionBinding insertStack = new PackageActionBinding.Builder(PackageAction.INSERT_STACK).use().sneak().build();
	@PackageActionBinding.For(PackageAction.INSERT_ALL)
	public PackageActionBinding insertAll = new PackageActionBinding.Builder(PackageAction.INSERT_ALL).use().ctrl().build();
	@PackageActionBinding.For(PackageAction.TAKE_ONE)
	public PackageActionBinding takeOne = new PackageActionBinding.Builder(PackageAction.TAKE_ONE).punch().build();
	@PackageActionBinding.For(PackageAction.TAKE_STACK)
	public PackageActionBinding takeStack = new PackageActionBinding.Builder(PackageAction.TAKE_STACK).punch().sneak().build();
	@PackageActionBinding.For(PackageAction.TAKE_ALL)
	public PackageActionBinding takeAll = new PackageActionBinding.Builder(PackageAction.TAKE_ALL).punch().ctrl().build();
	
	public int punchRepeat = -1;
	public double fontVerticalShift = 0;
	public MeshBackend meshBackend = MeshBackend.FRAPI_MESH;
	public boolean cacheMeshes = false;
	public boolean frexSupport = true;
	
	//Bindings sorted such that the more specific ones are at the front of the list (check ctrl-shift-alt, before ctrl-alt, before alt)
	public transient List<PackageActionBinding> sortedBindings = new ArrayList<>();
	
	public void finish() {
		sortedBindings = new ArrayList<>();
		sortedBindings.addAll(Arrays.asList(insertOne, insertStack, insertAll, takeOne, takeStack, takeAll));
		sortedBindings.sort(Comparator.naturalOrder());
	}
}
