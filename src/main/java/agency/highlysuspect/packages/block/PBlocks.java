package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.Init;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class PBlocks {
	public static PackageMakerBlock PACKAGE_MAKER;
	
	public static PackageBlock PACKAGE;
	
	public static void onInitialize() {
		PACKAGE_MAKER = Registry.register(Registry.BLOCK, Init.id("package_maker"), new PackageMakerBlock(
			FabricBlockSettings.of(Material.WOOD)
				.sound(SoundType.WOOD)
				.strength(1f, 1f)
		));
		
		PACKAGE = Registry.register(Registry.BLOCK, Init.id("package"), new PackageBlock(
			FabricBlockSettings.of(Material.WOOD)
				.sound(SoundType.WOOD)
				.strength(1f, 1f)
				.noOcclusion()
		));
	}
}
