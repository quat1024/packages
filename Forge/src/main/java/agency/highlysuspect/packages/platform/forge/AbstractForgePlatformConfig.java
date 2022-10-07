package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Consumer;

public abstract class AbstractForgePlatformConfig {
	protected abstract void configure(ForgeConfigSpec.Builder bob);
	protected abstract void install();
	
	protected void setup(ModConfig.Type type) {
		ForgeConfigSpec.Builder bob = new ForgeConfigSpec.Builder();
		configure(bob);
		
		//Register the mod config with Forge.
		ModLoadingContext.get().registerConfig(type, bob.build());
		
		//Update the config global to reflect my current state.
		Consumer<ModConfig> blah = modcfg -> {
			if(Packages.MODID.equals(modcfg.getModId()) && type == modcfg.getType()) install();
		};
		FMLJavaModLoadingContext.get().getModEventBus().addListener((ModConfigEvent.Loading load) -> blah.accept(load.getConfig()));
		FMLJavaModLoadingContext.get().getModEventBus().addListener((ModConfigEvent.Reloading reload) -> blah.accept(reload.getConfig()));
	}
}
