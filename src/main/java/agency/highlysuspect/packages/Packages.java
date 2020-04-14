package agency.highlysuspect.packages;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.entity.PBlockEntityTypes;
import agency.highlysuspect.packages.item.PItems;
import net.fabricmc.api.ModInitializer;

public class Packages implements ModInitializer {
	public static final String MODID = "packages";
	
	@Override
	public void onInitialize() {
		PBlocks.onInitialize();
		PBlockEntityTypes.onInitialize();
		
		PItems.onInitialize();
	}
}
