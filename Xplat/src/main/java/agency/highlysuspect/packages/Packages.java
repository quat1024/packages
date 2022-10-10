package agency.highlysuspect.packages;

import agency.highlysuspect.packages.block.PBlockEntityTypes;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.container.PMenuTypes;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PDispenserBehaviors;
import agency.highlysuspect.packages.junk.PTags;
import agency.highlysuspect.packages.junk.PSoundEvents;
import agency.highlysuspect.packages.junk.SidedProxy;
import agency.highlysuspect.packages.net.PNetCommon;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Packages {
	public static final String MODID = "packages";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static Packages instance;
	
	public final PlatformSupport plat;
	
	//This defaults to null on purpose. I initially had it set to a default instance to avoid errors, but I'd rather kaboom
	//so I know I'm reading data that doesn't correspond to the config file, because that'd be effectively garbage data.
	public PackagesConfig config = null;
	
	//Reset from PackagesClient
	public SidedProxy proxy = new SidedProxy();
	
	public Packages(PlatformSupport plat) {
		if(instance != null) throw new IllegalStateException("Initializing Packages twice!");
		instance = this;
		
		this.plat = plat;
		plat.makePlatformConfig().registerAndLoadAndAllThatJazz();
	}
	
	public void earlySetup() {
		PBlocks.onInitialize(plat);
		PBlockEntityTypes.onInitialize(plat);
		PItems.onInitialize(plat);
		
		PDispenserBehaviors.onInitialize(plat);
		PTags.onInitialize();
		
		PMenuTypes.onInitialize(plat);
		PNetCommon.onInitialize(plat);
		
		PSoundEvents.onInitialize(plat);
	}
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
}
