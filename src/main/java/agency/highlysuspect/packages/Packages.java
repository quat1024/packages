package agency.highlysuspect.packages;

import agency.highlysuspect.packages.block.PBlockEntityTypes;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.config.ConfigShape2;
import agency.highlysuspect.packages.config.PackageActionBinding;
import agency.highlysuspect.packages.config.PackagesConfig;
import agency.highlysuspect.packages.container.PMenuTypes;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PDispenserBehaviors;
import agency.highlysuspect.packages.junk.PItemTags;
import agency.highlysuspect.packages.junk.PSoundEvents;
import agency.highlysuspect.packages.net.PNetCommon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Packages implements ModInitializer {
	public static final String MODID = "packages";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static ConfigShape2 CONFIG_SHAPE;
	public static PackagesConfig config;
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	@Override
	public void onInitialize() {
		CONFIG_SHAPE = new ConfigShape2()
			.installSerializer(PackageActionBinding.class, new PackageActionBinding.SerializerDeserializer())
			.readPropsFromPojo(new PackagesConfig());
		
		//TODO: Split client and server config
		for(PackType type : List.of(PackType.CLIENT_RESOURCES, PackType.SERVER_DATA)) {
			ResourceManagerHelper.get(type).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
				@Override public ResourceLocation getFabricId() { return new ResourceLocation(MODID, "data-reload"); }
				@Override public void onResourceManagerReload(ResourceManager resourceManager) { loadConfig(); }
			});
		}
		
		loadConfig();
		
		PBlocks.onInitialize();
		PBlockEntityTypes.onInitialize();
		PItems.onInitialize();
		
		PDispenserBehaviors.onInitialize();
		PItemTags.onInitialize();
		
		PMenuTypes.onInitialize();
		PNetCommon.onInitialize();
		
		PSoundEvents.onInitialize();
	}
	
	private void loadConfig() {
		try {
			config = CONFIG_SHAPE.readFromOrCreateFile(FabricLoader.getInstance().getConfigDir().resolve("packages.cfg"), new PackagesConfig());
			config.finish();
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}
}
