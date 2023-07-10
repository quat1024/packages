package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.platform.RegistryHandle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

public class PItems {
	public static RegistryHandle<BlockItem> PACKAGE_MAKER;
	public static RegistryHandle<PackageItem> PACKAGE;
	
	public static void onInitialize() {
		Packages.instance.register(BuiltInRegistries.CREATIVE_MODE_TAB, Packages.id("group"), () -> 
			Packages.instance.creativeModeTabBuilder()
				.title(Component.translatable("itemGroup.packages.group"))
				.icon(() -> {
					try {
						List<ItemStack> stacks = PBlocks.PACKAGE.get().lotsOfPackages();
						return stacks.get(new Random(System.currentTimeMillis()).nextInt(stacks.size()));
					} catch (Exception e) { //trust no one not even yourself
						return new ItemStack(PACKAGE_MAKER.get());
					}
				})
				.displayItems((params, out) -> {
					out.accept(PACKAGE_MAKER.get());
					PBlocks.PACKAGE.get().lotsOfPackages().forEach(out::accept);
				})
				.build()
		);
		
		PACKAGE_MAKER = Packages.instance.register(BuiltInRegistries.ITEM, PBlocks.PACKAGE_MAKER.getId(), () -> new BlockItem(PBlocks.PACKAGE_MAKER.get(), new Item.Properties()));
		PACKAGE = Packages.instance.register(BuiltInRegistries.ITEM, PBlocks.PACKAGE.getId(), () -> new PackageItem(PBlocks.PACKAGE.get(), new Item.Properties()));
	}
}
