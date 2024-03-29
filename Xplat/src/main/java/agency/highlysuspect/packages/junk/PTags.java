package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.Packages;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PTags {
	public static TagKey<Item> BANNED_FROM_PACKAGE_MAKER;
	public static TagKey<Item> BANNED_FROM_PACKAGE;
	public static TagKey<Item> THINGS_YOU_NEED_FOR_PACKAGE_CRAFTING;
	
	public static TagKey<Item> ALLOWLIST_PACKAGE_MAKER_FRAME;
	public static TagKey<Item> ALLOWLIST_PACKAGE_MAKER_INNER;
	
	public static TagKey<Block> STICKY;
	
	public static void onInitialize() {
		BANNED_FROM_PACKAGE_MAKER = TagKey.create(BuiltInRegistries.ITEM.key(), Packages.id("banned_from_package_maker"));
		BANNED_FROM_PACKAGE = TagKey.create(BuiltInRegistries.ITEM.key(), Packages.id("banned_from_package"));
		THINGS_YOU_NEED_FOR_PACKAGE_CRAFTING = TagKey.create(BuiltInRegistries.ITEM.key(), Packages.id("things_you_need_for_package_crafting"));
		
		ALLOWLIST_PACKAGE_MAKER_FRAME = TagKey.create(BuiltInRegistries.ITEM.key(), Packages.id("allowlist_package_maker_frame"));
		ALLOWLIST_PACKAGE_MAKER_INNER = TagKey.create(BuiltInRegistries.ITEM.key(), Packages.id("allowlist_package_maker_inner"));
		
		STICKY = TagKey.create(BuiltInRegistries.BLOCK.key(), Packages.id("sticky"));
	}
}
