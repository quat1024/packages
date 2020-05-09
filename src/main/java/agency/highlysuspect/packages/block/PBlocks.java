package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.PackagesInit;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PBlocks {
	public static PackageMakerBlock PACKAGE_MAKER;
	
	public static PackageBlock PACKAGE;
	
	public static void onInitialize() {
		PACKAGE_MAKER = Registry.register(Registry.BLOCK, new Identifier(PackagesInit.MODID, "package_maker"), new PackageMakerBlock(
			FabricBlockSettings.of(Material.WOOD)
				.breakByTool(FabricToolTags.AXES)
				.sounds(BlockSoundGroup.WOOD)
				.strength(1f, 1f)
				.build()
		));
		
		PACKAGE = Registry.register(Registry.BLOCK, new Identifier(PackagesInit.MODID, "package"), new PackageBlock(
			FabricBlockSettings.of(Material.WOOD)
				.breakByTool(FabricToolTags.AXES)
				.sounds(BlockSoundGroup.WOOD)
				.strength(1f, 1f)
				.nonOpaque()
				.build()
		));
	}
}
