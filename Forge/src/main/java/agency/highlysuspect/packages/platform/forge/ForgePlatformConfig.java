package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.config.MeshBackend;
import agency.highlysuspect.packages.config.PackageActionBinding;
import agency.highlysuspect.packages.config.PackagesConfig;
import agency.highlysuspect.packages.config.PlatformConfig;
import agency.highlysuspect.packages.net.PackageAction;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Consumer;

public class ForgePlatformConfig implements PlatformConfig {
	private final ForgeConfigSpec.Builder bob = new ForgeConfigSpec.Builder();
	{
		bob.push("Features");
		
		inventoryInteractions = bob.comment("Allow interacting with Packages in the player inventory, kinda like a Bundle.").define("inventoryInteractions", true);
		interactionSounds = bob.comment("Play sounds when players interact with a Package.").define("interactionSounds", true);
		
		bob.pop();
		
		bob.push("Client"); //TODO actual split into client config
		insertOneBindingUnparsed = bob.comment("Specify at least 'use' (right click) or 'punch' (left click), and optionally add",
			"any combination of 'ctrl', 'alt', or 'sneak' to require some modifier keys.",
			"Separate multiple items with hyphens. Disable an action entirely by leaving it blank.",
			"",
			"How do you insert one item into the package?").define("insertOne", "sneak-use");
		insertStackBindingUnparsed = bob.comment("How do you insert one stack of items into the package?").define("insertStack", "sneak-use");
		insertAllBindingUnparsed = bob.comment("How do you insert everything in your inventory that fits into the package?").define("insertAll", "ctrl-use");
		takeOneBindingUnparsed = bob.comment("How do you take one item from the package?").define("takeOne", "punch");
		takeStackBindingUnparsed = bob.comment("How do you take one stack of items from the package?").define("takeStack", "sneak-punch");
		takeAllBindingUnparsed = bob.comment("How do you clear all items from the package?").define("takeAll", "ctrl-punch");
		
		punchRepeat = bob.comment("Older versions of Packages had a \"feature\" where holding left-click would slowly trickle items out of the Package.",
			"This was actually a bug, caused by code intended to differentiatiate between 'starting a left click' and 'continuing",
			"a left click' not working correctly; the delay was my band-aid fix.",
			"I've since actually fixed it, but I don't know if people had gotten used to the broken behavior :)",
			"If you did and want it back, set this to 4.").define("punchRepeat", -1);
		fontVerticalShift = bob.comment("Vertically shift the numeric display on Packages up by this many blocks.",
			"Nudge this to recenter fonts with a different baseline from vanilla.").define("fontVerticalShift", 0d);
		
		meshBackend = bob.comment("The method Packages uses to render its models. After changing the option, reload resources (F3+T) uhh, twice (models get loaded before the config file :cry:)",
			"Possible values:",
			"'frapi_mesh' uses many features from Fabric Renderer API.",
			"   It has received the most testing and performs surprisingly well. It has special compatibility features with FREX.",
			"   If you have Sodium, you need Indium to use frapi_mesh.",
			"'frapi_bakedquad' uses a small amount of the Fabric Renderer API.",
			"   It doesn't perform as well, but may be more compatible. It doesn't have compat with FREX.",
			"   If you have Sodium, you also need Indium to use frapi_bakedquad.",
			"'skip' bypasses the custom model pipeline entirely. Packages will render its blocks using placeholder textures.",
			"   This is intended for debugging, resource-packmaking, getting into the world if the other renderers have a crash bug, etc.").defineEnum("meshBackend", MeshBackend.FRAPI_MESH); //TODO forg
		cacheMeshes = bob.comment("If 'true', Package and Package Crafter 3d models will be cached in-memory, instead of rebaked from scratch every time.",
			"With the frapi_mesh backend, this probably helps performance less than it sounds like it would.",
			"Might make more of a difference on other backends.").define("cacheMeshes", false);
		bob.pop();
	}
	
	private final ForgeConfigSpec.ConfigValue<Boolean> inventoryInteractions;
	private final ForgeConfigSpec.ConfigValue<Boolean> interactionSounds;
	
	private final ForgeConfigSpec.ConfigValue<String> insertOneBindingUnparsed;
	private final ForgeConfigSpec.ConfigValue<String> insertStackBindingUnparsed;
	private final ForgeConfigSpec.ConfigValue<String> insertAllBindingUnparsed;
	private final ForgeConfigSpec.ConfigValue<String> takeOneBindingUnparsed;
	private final ForgeConfigSpec.ConfigValue<String> takeStackBindingUnparsed;
	private final ForgeConfigSpec.ConfigValue<String> takeAllBindingUnparsed;
	
	private final ForgeConfigSpec.ConfigValue<Integer> punchRepeat;
	private final ForgeConfigSpec.ConfigValue<Double> fontVerticalShift;
	
	private final ForgeConfigSpec.EnumValue<MeshBackend> meshBackend;
	private final ForgeConfigSpec.ConfigValue<Boolean> cacheMeshes;
	
	@Override
	public boolean inventoryInteractions() {
		return inventoryInteractions.get();
	}
	
	@Override
	public boolean interactionSounds() {
		return interactionSounds.get();
	}
	
	@Override
	public PackageActionBinding insertOneBinding() {
		return PackageActionBinding.fromString(PackageAction.INSERT_ONE, insertOneBindingUnparsed.get());
	}
	
	@Override
	public PackageActionBinding insertStackBinding() {
		return PackageActionBinding.fromString(PackageAction.INSERT_STACK, insertStackBindingUnparsed.get());
	}
	
	@Override
	public PackageActionBinding insertAllBinding() {
		return PackageActionBinding.fromString(PackageAction.INSERT_ALL, insertAllBindingUnparsed.get());
	}
	
	@Override
	public PackageActionBinding takeOneBinding() {
		return PackageActionBinding.fromString(PackageAction.TAKE_ONE, takeOneBindingUnparsed.get());
	}
	
	@Override
	public PackageActionBinding takeStackBinding() {
		return PackageActionBinding.fromString(PackageAction.TAKE_STACK, takeStackBindingUnparsed.get());
	}
	
	@Override
	public PackageActionBinding takeAllBinding() {
		return PackageActionBinding.fromString(PackageAction.TAKE_ALL, takeAllBindingUnparsed.get());
	}
	
	@Override
	public int punchRepeat() {
		return punchRepeat.get();
	}
	
	@Override
	public double fontVerticalShift() {
		return fontVerticalShift.get();
	}
	
	@Override
	public MeshBackend meshBackend() {
		return meshBackend.get();
	}
	
	@Override
	public boolean cacheMeshes() {
		return cacheMeshes.get();
	}
	
	@Override
	public boolean frexSupport() {
		//Frex doesn't exist on forge.
		return false;
	}
	
	@Override
	public void registerAndLoadAndAllThatJazz() {
		//Register the mod config with Forge.
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, bob.build());
		
		//Update the config global in Packages.instance to reflect my current state.
		Consumer<ModConfig> blah = modcfg -> {
			if(Packages.MODID.equals(modcfg.getModId())) Packages.instance.config = PackagesConfig.makeConfig(this);
		};
		FMLJavaModLoadingContext.get().getModEventBus().addListener((ModConfigEvent.Loading load) -> blah.accept(load.getConfig()));
		FMLJavaModLoadingContext.get().getModEventBus().addListener((ModConfigEvent.Reloading reload) -> blah.accept(reload.getConfig()));
	}
}
