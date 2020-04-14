package agency.highlysuspect.packages.block.entity;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PBlockEntityTypes {
	public static BlockEntityType<PackageBlockEntity> PACKAGE;
	
	public static void onInitialize() {
		PACKAGE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Packages.MODID, "package"), 
			BlockEntityType.Builder.create(PackageBlockEntity::new, PBlocks.PACKAGE).build(null)
		);
	}
}
