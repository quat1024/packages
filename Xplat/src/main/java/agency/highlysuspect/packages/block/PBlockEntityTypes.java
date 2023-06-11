package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.platform.RegistryHandle;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PBlockEntityTypes {
	public static RegistryHandle<BlockEntityType<PackageBlockEntity>> PACKAGE;
	public static RegistryHandle<BlockEntityType<PackageMakerBlockEntity>> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE = Packages.instance.register(Registry.BLOCK_ENTITY_TYPE, Packages.id("package"), () -> Packages.instance.makeBlockEntityType(PackageBlockEntity::new, PBlocks.PACKAGE.get()));
		PACKAGE_MAKER = Packages.instance.register(Registry.BLOCK_ENTITY_TYPE, Packages.id("package_maker"), () -> Packages.instance.makeBlockEntityType(PackageMakerBlockEntity::new, PBlocks.PACKAGE_MAKER.get()));
	}
}
