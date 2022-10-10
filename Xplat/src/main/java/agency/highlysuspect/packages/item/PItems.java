package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class PItems {
	public static CreativeModeTab TAB;
	
	public static PlatformSupport.RegistryHandle<BlockItem> PACKAGE_MAKER;
	public static PlatformSupport.RegistryHandle<PackageItem> PACKAGE;
	
	public static void onInitialize(PlatformSupport plat) {
		TAB = plat.makeCreativeModeTab(Packages.id("group"), () -> {
			try {
				NonNullList<ItemStack> stacks = NonNullList.create();
				PBlocks.PACKAGE.get().fillItemCategory(TAB, stacks);
				return stacks.get(new Random(System.currentTimeMillis()).nextInt(stacks.size()));
			} catch (Exception e) { //trust no one not even yourself
				return new ItemStack(PACKAGE_MAKER.get());
			}
		});
		
		PACKAGE_MAKER = plat.register(Registry.ITEM, PBlocks.PACKAGE_MAKER.getId(), () -> new BlockItem(PBlocks.PACKAGE_MAKER.get(), new Item.Properties().tab(TAB)));
		PACKAGE = plat.register(Registry.ITEM, PBlocks.PACKAGE.getId(), () -> new PackageItem(PBlocks.PACKAGE.get(), new Item.Properties().tab(TAB)));
	}
	
}
