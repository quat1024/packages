package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PBlocks;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.BiFunction;

public class PItems {
	public static BlockItem PACKAGE_MAKER;
	public static PackageItem PACKAGE;
	
	public static final ItemGroup GROUP = FabricItemGroupBuilder.create(new Identifier(Packages.MODID, "group"))
		.icon(() -> new ItemStack(PACKAGE_MAKER))
		.appendItems(list -> {
			list.add(new ItemStack(PACKAGE_MAKER));
			
			Block[][] sampleBarrels = new Block[][] {
				{ Blocks.OAK_LOG, Blocks.OAK_PLANKS },
				{ Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS },
				{ Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS } //TODO add the rest
			};
			
			for(Block[] pair : sampleBarrels) {
				list.add(PACKAGE.createCustomizedStack(pair[0], pair[1]));
			}
		})
		//todo appenditems for a bunch of sample package types? maybe?
		.build();
	
	public static void onInitialize() {
		PACKAGE_MAKER = blockItemYeet(PBlocks.PACKAGE_MAKER, BlockItem::new);
		PACKAGE = blockItemYeet(PBlocks.PACKAGE, PackageItem::new);
	}
	
	private static <T extends BlockItem> T blockItemYeet(Block b, BiFunction<Block, Item.Settings, T> constructor) {
		return Registry.register(Registry.ITEM, Registry.BLOCK.getId(b), constructor.apply(b,
			new Item.Settings().group(GROUP)
		));
	}
}
