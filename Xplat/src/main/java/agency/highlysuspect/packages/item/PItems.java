package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.function.BiFunction;

public class PItems {
	public static CreativeModeTab TAB;
	
	public static PlatformSupport.RegistryHandle<BlockItem> PACKAGE_MAKER;
	public static PlatformSupport.RegistryHandle<PackageItem> PACKAGE;
	
	public static void onInitialize(PlatformSupport plat) {
		TAB = plat.makeCreativeModeTab(Packages.id("group"), () -> new ItemStack(PACKAGE_MAKER.get()));
		
		PACKAGE_MAKER = mkBlockItem(plat, PBlocks.PACKAGE_MAKER, BlockItem::new);
		PACKAGE = mkBlockItem(plat, PBlocks.PACKAGE, PackageItem::new);
	}
	
	private static <T extends BlockItem, B extends Block> PlatformSupport.RegistryHandle<T> mkBlockItem(PlatformSupport plat, PlatformSupport.RegistryHandle<B> b, BiFunction<B, Item.Properties, T> constructor) {
		return plat.register(Registry.ITEM, b.getId(), () -> constructor.apply(b.get(), new Item.Properties().tab(TAB)));
	}
	
	//TODO: port to Item#fillItemCategory instead of this
//			Object[][] sampleBarrels = new Object[][] {
//				{ Blocks.OAK_LOG                , Blocks.OAK_PLANKS                  , DyeColor.WHITE},
//				{ Blocks.BIRCH_LOG              , Blocks.BIRCH_PLANKS                , DyeColor.YELLOW},
//				{ Blocks.SPRUCE_LOG             , Blocks.SPRUCE_PLANKS               , DyeColor.RED},
//				{ Blocks.ACACIA_LOG             , Blocks.ACACIA_PLANKS               , DyeColor.PINK},
//				{ Blocks.DARK_OAK_LOG           , Blocks.DARK_OAK_PLANKS             , DyeColor.BROWN},
//				{ Blocks.JUNGLE_LOG             , Blocks.JUNGLE_PLANKS               , DyeColor.GREEN},
//				{ Blocks.CRIMSON_HYPHAE         , Blocks.CRIMSON_PLANKS              , DyeColor.RED},
//				{ Blocks.WARPED_HYPHAE          , Blocks.WARPED_PLANKS               , DyeColor.CYAN},
//				{ Blocks.POLISHED_ANDESITE      , Blocks.ANDESITE                    , DyeColor.LIGHT_GRAY},
//				{ Blocks.POLISHED_GRANITE       , Blocks.GRANITE                     , DyeColor.PINK},
//				{ Blocks.POLISHED_DIORITE       , Blocks.DIORITE                     , DyeColor.WHITE},
//				{ Blocks.POLISHED_DEEPSLATE     , Blocks.COBBLED_DEEPSLATE           , DyeColor.GRAY},
//				{ Blocks.WHITE_CONCRETE         , Blocks.WHITE_CONCRETE_POWDER       , DyeColor.WHITE},
//				{ Blocks.ORANGE_CONCRETE        , Blocks.ORANGE_CONCRETE_POWDER      , DyeColor.ORANGE},
//				{ Blocks.MAGENTA_CONCRETE       , Blocks.MAGENTA_CONCRETE_POWDER     , DyeColor.MAGENTA},
//				{ Blocks.LIGHT_BLUE_CONCRETE    , Blocks.LIGHT_BLUE_CONCRETE_POWDER  , DyeColor.LIGHT_BLUE},
//				{ Blocks.YELLOW_CONCRETE        , Blocks.YELLOW_CONCRETE_POWDER      , DyeColor.YELLOW},
//				{ Blocks.LIME_CONCRETE          , Blocks.LIME_CONCRETE_POWDER        , DyeColor.LIME},
//				{ Blocks.PINK_CONCRETE          , Blocks.PINK_CONCRETE_POWDER        , DyeColor.PINK},
//				{ Blocks.GRAY_CONCRETE          , Blocks.GRAY_CONCRETE_POWDER        , DyeColor.GRAY},
//				{ Blocks.LIGHT_GRAY_CONCRETE    , Blocks.LIGHT_GRAY_CONCRETE_POWDER  , DyeColor.LIGHT_GRAY},
//				{ Blocks.CYAN_CONCRETE          , Blocks.CYAN_CONCRETE_POWDER        , DyeColor.CYAN},
//				{ Blocks.PURPLE_CONCRETE        , Blocks.PURPLE_CONCRETE_POWDER      , DyeColor.PURPLE},
//				{ Blocks.BLUE_CONCRETE          , Blocks.BLUE_CONCRETE_POWDER        , DyeColor.BLUE},
//				{ Blocks.BROWN_CONCRETE         , Blocks.BROWN_CONCRETE_POWDER       , DyeColor.BROWN},
//				{ Blocks.GREEN_CONCRETE         , Blocks.GREEN_CONCRETE_POWDER       , DyeColor.GREEN},
//				{ Blocks.RED_CONCRETE           , Blocks.RED_CONCRETE_POWDER         , DyeColor.RED},
//				{ Blocks.WHITE_TERRACOTTA       , Blocks.WHITE_TERRACOTTA            , DyeColor.WHITE},
//				{ Blocks.ORANGE_TERRACOTTA      , Blocks.ORANGE_TERRACOTTA           , DyeColor.ORANGE},
//				{ Blocks.MAGENTA_TERRACOTTA     , Blocks.MAGENTA_TERRACOTTA          , DyeColor.MAGENTA},
//				{ Blocks.LIGHT_BLUE_TERRACOTTA  , Blocks.LIGHT_BLUE_TERRACOTTA       , DyeColor.LIGHT_BLUE},
//				{ Blocks.YELLOW_TERRACOTTA      , Blocks.YELLOW_TERRACOTTA           , DyeColor.YELLOW},
//				{ Blocks.LIME_TERRACOTTA        , Blocks.LIME_TERRACOTTA             , DyeColor.LIME},
//				{ Blocks.PINK_TERRACOTTA        , Blocks.PINK_TERRACOTTA             , DyeColor.PINK},
//				{ Blocks.GRAY_TERRACOTTA        , Blocks.GRAY_TERRACOTTA             , DyeColor.GRAY},
//				{ Blocks.LIGHT_GRAY_TERRACOTTA  , Blocks.LIGHT_GRAY_TERRACOTTA       , DyeColor.LIGHT_GRAY},
//				{ Blocks.CYAN_TERRACOTTA        , Blocks.CYAN_TERRACOTTA             , DyeColor.CYAN},
//				{ Blocks.PURPLE_TERRACOTTA      , Blocks.PURPLE_TERRACOTTA           , DyeColor.PURPLE},
//				{ Blocks.BLUE_TERRACOTTA        , Blocks.BLUE_TERRACOTTA             , DyeColor.BLUE},
//				{ Blocks.BROWN_TERRACOTTA       , Blocks.BROWN_TERRACOTTA            , DyeColor.BROWN},
//				{ Blocks.GREEN_TERRACOTTA       , Blocks.GREEN_TERRACOTTA            , DyeColor.GREEN},
//				{ Blocks.RED_TERRACOTTA         , Blocks.RED_TERRACOTTA              , DyeColor.RED},
//				{ Blocks.COPPER_BLOCK           , Blocks.CUT_COPPER                  , DyeColor.WHITE},
//				{ Blocks.EXPOSED_COPPER         , Blocks.EXPOSED_CUT_COPPER          , DyeColor.WHITE},
//				{ Blocks.WEATHERED_COPPER       , Blocks.WEATHERED_CUT_COPPER        , DyeColor.WHITE},
//				{ Blocks.OXIDIZED_COPPER        , Blocks.OXIDIZED_CUT_COPPER         , DyeColor.WHITE},
//			};
//			
//			for(Object[] triple : sampleBarrels) {
//				list.add(PACKAGE.createCustomizedStack((Block) triple[0], (Block) triple[1], (DyeColor) triple[2]));
//			}
}
