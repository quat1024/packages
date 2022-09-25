package agency.highlysuspect.packages;

import agency.highlysuspect.packages.block.PBlockEntityTypes;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.config.ConfigShape2;
import agency.highlysuspect.packages.config.PackagesConfig;
import agency.highlysuspect.packages.container.PMenuTypes;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PDispenserBehaviors;
import agency.highlysuspect.packages.junk.PItemTags;
import agency.highlysuspect.packages.junk.PSoundEvents;
import agency.highlysuspect.packages.net.PNetCommon;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Packages {
	public static final String MODID = "packages";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static Packages instance;
	
	public final PlatformSupport plat;
	public final ConfigShape2 configShape;
	public PackagesConfig config;
	
	public Packages(PlatformSupport plat) {
		if(instance != null) throw new IllegalStateException("Initializing Packages twice!");
		instance = this;
		
		this.plat = plat;
		this.configShape = PackagesConfig.makeConfigShape();
	}
	
	public void earlySetup() {
		//TODO: Split client and server config
		loadConfig();
		plat.installResourceReloadListener(mgr -> loadConfig(), id("data-reload"), PackType.CLIENT_RESOURCES, PackType.SERVER_DATA);
		
		PBlocks.onInitialize(plat);
		PBlockEntityTypes.onInitialize(plat);
		PItems.onInitialize(plat);
		
		PDispenserBehaviors.onInitialize(plat);
		PItemTags.onInitialize();
		
		PMenuTypes.onInitialize(plat);
		PNetCommon.onInitialize(plat);
		
		PSoundEvents.onInitialize(plat);
	}
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	private void loadConfig() {
		try {
			config = configShape.readFromOrCreateFile(plat.getConfigFolder().resolve("packages.cfg"), new PackagesConfig());
			config.finish();
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}
}
