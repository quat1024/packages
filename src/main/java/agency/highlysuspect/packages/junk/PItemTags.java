package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.PackagesInit;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class PItemTags {
	public static Tag<Item> BANNED_FROM_PACKAGE_MAKER;
	
	public static void onInitialize() {
		BANNED_FROM_PACKAGE_MAKER = TagRegistry.item(new Identifier(PackagesInit.MODID, "banned_from_package_maker"));
	}
}
