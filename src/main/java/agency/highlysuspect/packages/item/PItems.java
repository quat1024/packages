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
import net.minecraft.util.DyeColor;
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
			
			Object[][] sampleBarrels = new Object[][] {
				{ Blocks.OAK_LOG                , Blocks.OAK_PLANKS                  , DyeColor.WHITE},
				{ Blocks.BIRCH_LOG              , Blocks.BIRCH_PLANKS                , DyeColor.YELLOW},
				{ Blocks.SPRUCE_LOG             , Blocks.SPRUCE_PLANKS               , DyeColor.RED},
				{ Blocks.ACACIA_LOG             , Blocks.ACACIA_PLANKS               , DyeColor.PINK},
				{ Blocks.DARK_OAK_LOG           , Blocks.DARK_OAK_PLANKS             , DyeColor.BROWN},
				{ Blocks.JUNGLE_LOG             , Blocks.JUNGLE_PLANKS               , DyeColor.GREEN},
				{ Blocks.WHITE_CONCRETE         , Blocks.WHITE_CONCRETE_POWDER       , DyeColor.WHITE},
				{ Blocks.ORANGE_CONCRETE        , Blocks.ORANGE_CONCRETE_POWDER      , DyeColor.ORANGE},
				{ Blocks.MAGENTA_CONCRETE       , Blocks.MAGENTA_CONCRETE_POWDER     , DyeColor.MAGENTA},
				{ Blocks.LIGHT_BLUE_CONCRETE    , Blocks.LIGHT_BLUE_CONCRETE_POWDER  , DyeColor.LIGHT_BLUE},
				{ Blocks.YELLOW_CONCRETE        , Blocks.YELLOW_CONCRETE_POWDER      , DyeColor.YELLOW},
				{ Blocks.LIME_CONCRETE          , Blocks.LIME_CONCRETE_POWDER        , DyeColor.LIME},
				{ Blocks.PINK_CONCRETE          , Blocks.PINK_CONCRETE_POWDER        , DyeColor.PINK},
				{ Blocks.GRAY_CONCRETE          , Blocks.GRAY_CONCRETE_POWDER        , DyeColor.GRAY},
				{ Blocks.LIGHT_GRAY_CONCRETE    , Blocks.LIGHT_GRAY_CONCRETE_POWDER  , DyeColor.LIGHT_GRAY},
				{ Blocks.CYAN_CONCRETE          , Blocks.CYAN_CONCRETE_POWDER        , DyeColor.CYAN},
				{ Blocks.PURPLE_CONCRETE        , Blocks.PURPLE_CONCRETE_POWDER      , DyeColor.PURPLE},
				{ Blocks.BLUE_CONCRETE          , Blocks.BLUE_CONCRETE_POWDER        , DyeColor.BLUE},
				{ Blocks.BROWN_CONCRETE         , Blocks.BROWN_CONCRETE_POWDER       , DyeColor.BROWN},
				{ Blocks.GREEN_CONCRETE         , Blocks.GREEN_CONCRETE_POWDER       , DyeColor.GREEN},
				{ Blocks.RED_CONCRETE           , Blocks.RED_CONCRETE_POWDER         , DyeColor.RED},
				{ Blocks.WHITE_TERRACOTTA       , Blocks.WHITE_TERRACOTTA            , DyeColor.WHITE},
				{ Blocks.ORANGE_TERRACOTTA      , Blocks.ORANGE_TERRACOTTA           , DyeColor.ORANGE},
				{ Blocks.MAGENTA_TERRACOTTA     , Blocks.MAGENTA_TERRACOTTA          , DyeColor.MAGENTA},
				{ Blocks.LIGHT_BLUE_TERRACOTTA  , Blocks.LIGHT_BLUE_TERRACOTTA       , DyeColor.LIGHT_BLUE},
				{ Blocks.YELLOW_TERRACOTTA      , Blocks.YELLOW_TERRACOTTA           , DyeColor.YELLOW},
				{ Blocks.LIME_TERRACOTTA        , Blocks.LIME_TERRACOTTA             , DyeColor.LIME},
				{ Blocks.PINK_TERRACOTTA        , Blocks.PINK_TERRACOTTA             , DyeColor.PINK},
				{ Blocks.GRAY_TERRACOTTA        , Blocks.GRAY_TERRACOTTA             , DyeColor.GRAY},
				{ Blocks.LIGHT_GRAY_TERRACOTTA  , Blocks.LIGHT_GRAY_TERRACOTTA       , DyeColor.LIGHT_GRAY},
				{ Blocks.CYAN_TERRACOTTA        , Blocks.CYAN_TERRACOTTA             , DyeColor.CYAN},
				{ Blocks.PURPLE_TERRACOTTA      , Blocks.PURPLE_TERRACOTTA           , DyeColor.PURPLE},
				{ Blocks.BLUE_TERRACOTTA        , Blocks.BLUE_TERRACOTTA             , DyeColor.BLUE},
				{ Blocks.BROWN_TERRACOTTA       , Blocks.BROWN_TERRACOTTA            , DyeColor.BROWN},
				{ Blocks.GREEN_TERRACOTTA       , Blocks.GREEN_TERRACOTTA            , DyeColor.GREEN},
				{ Blocks.RED_TERRACOTTA         , Blocks.RED_TERRACOTTA              , DyeColor.RED},
			};
			
			for(Object[] triple : sampleBarrels) {
				list.add(PACKAGE.createCustomizedStack((Block) triple[0], (Block) triple[1], (DyeColor) triple[2]));
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
