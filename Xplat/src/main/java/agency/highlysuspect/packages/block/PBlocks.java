package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.platform.RegistryHandle;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class PBlocks {
	public static RegistryHandle<PackageBlock> PACKAGE;
	public static RegistryHandle<PackageMakerBlock> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE = Packages.instance.register(Registry.BLOCK, Packages.id("package"), () -> new PackageBlock(
			BlockBehaviour.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(1f, 1f).noOcclusion()
		));
		
		PACKAGE_MAKER = Packages.instance.register(Registry.BLOCK, Packages.id("package_maker"), () -> new PackageMakerBlock(
			BlockBehaviour.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(1f, 1f)
		));
	}
}
