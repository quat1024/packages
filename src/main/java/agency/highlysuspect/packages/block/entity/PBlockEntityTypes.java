package agency.highlysuspect.packages.block.entity;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.PBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PBlockEntityTypes {
	public static BlockEntityType<PackageBlockEntity> PACKAGE;
	public static BlockEntityType<PackageMakerBlockEntity> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(PackagesInit.MODID, "package"), 
			FabricBlockEntityTypeBuilder.create(PackageBlockEntity::new, PBlocks.PACKAGE).build(null)
		);
		
		PACKAGE_MAKER = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(PackagesInit.MODID, "package_maker"),
			FabricBlockEntityTypeBuilder.create(PackageMakerBlockEntity::new, PBlocks.PACKAGE_MAKER).build(null)
		);
	}
}
