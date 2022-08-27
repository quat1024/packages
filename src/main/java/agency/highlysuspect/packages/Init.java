package agency.highlysuspect.packages;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.PBlockEntityTypes;
import agency.highlysuspect.packages.container.PMenuTypes;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PDispenserBehaviors;
import agency.highlysuspect.packages.junk.PItemTags;
import agency.highlysuspect.packages.junk.PSoundEvents;
import agency.highlysuspect.packages.net.PNetCommon;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Init implements ModInitializer {
	public static final String MODID = "packages";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	@Override
	public void onInitialize() {
		PBlocks.onInitialize();
		PBlockEntityTypes.onInitialize();
		PItems.onInitialize();
		
		PDispenserBehaviors.onInitialize();
		PItemTags.onInitialize();
		
		PMenuTypes.onInitialize();
		PNetCommon.onInitialize();
		
		PSoundEvents.onInitialize();
	}
}