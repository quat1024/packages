package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PBlockEntityTypes {
	public static PlatformSupport.RegistryHandle<BlockEntityType<PackageBlockEntity>> PACKAGE;
	public static PlatformSupport.RegistryHandle<BlockEntityType<PackageMakerBlockEntity>> PACKAGE_MAKER;
	
	public static void onInitialize(PlatformSupport plat) {
		PACKAGE = plat.register(Registry.BLOCK_ENTITY_TYPE, Packages.id("package"), () -> plat.makeBlockEntityType(PackageBlockEntity::new, PBlocks.PACKAGE.get()));
		PACKAGE_MAKER = plat.register(Registry.BLOCK_ENTITY_TYPE, Packages.id("package_maker"), () -> plat.makeBlockEntityType(PackageMakerBlockEntity::new, PBlocks.PACKAGE_MAKER.get()));
	}
}
