package agency.highlysuspect.packages.platform;

import agency.highlysuspect.packages.net.ActionPacket;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface ClientPlatformSupport {
	//Rendering
	void bakeSpritesOnto(ResourceLocation atlasTexture, ResourceLocation... sprites);
	<T extends BlockEntity> void setBlockEntityRenderer(PlatformSupport.RegistryHandle<? extends BlockEntityType<T>> type, BlockEntityRendererProvider<? super T> renderer);
	void setRenderType(PlatformSupport.RegistryHandle<? extends Block> block, RenderType type);
	
	//Weird bakedmodel stuff
	UnbakedModel createPackageModel();
	UnbakedModel createPackageMakerModel();
	
	//Networking
	void sendActionPacket(ActionPacket packet);
}
