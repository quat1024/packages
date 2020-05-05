package agency.highlysuspect.packages.block.entity;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.PBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PBlockEntityTypes {
	public static BlockEntityType<PackageBlockEntity> PACKAGE;
	public static BlockEntityType<PackageMakerBlockEntity> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(PackagesInit.MODID, "package"), 
			BlockEntityType.Builder.create(PackageBlockEntity::new, PBlocks.PACKAGE).build(null)
		);
		
		PACKAGE_MAKER = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(PackagesInit.MODID, "package_maker"),
			BlockEntityType.Builder.create(PackageMakerBlockEntity::new, PBlocks.PACKAGE_MAKER).build(null)
		);
	}
}
