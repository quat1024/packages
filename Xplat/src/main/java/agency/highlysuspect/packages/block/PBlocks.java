package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.platform.RegistryHandle;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public class PBlocks {
	public static RegistryHandle<PackageBlock> PACKAGE;
	public static RegistryHandle<PackageMakerBlock> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE = Packages.instance.register(BuiltInRegistries.BLOCK, Packages.id("package"), () -> new PackageBlock(
			BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion().pushReaction(PushReaction.DESTROY)
		));
		
		PACKAGE_MAKER = Packages.instance.register(BuiltInRegistries.BLOCK, Packages.id("package_maker"), () -> new PackageMakerBlock(
			BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1f, 1f).noOcclusion()
		));
	}
}
