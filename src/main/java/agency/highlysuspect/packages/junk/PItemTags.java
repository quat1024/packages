package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.Init;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PItemTags {
	public static TagKey<Item> BANNED_FROM_PACKAGE_MAKER;
	
	public static void onInitialize() {
		BANNED_FROM_PACKAGE_MAKER = TagKey.create(Registry.ITEM_REGISTRY, Init.id("banned_from_package_maker"));
	}
}
