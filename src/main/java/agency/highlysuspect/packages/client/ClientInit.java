package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.entity.PBlockEntityTypes;
import agency.highlysuspect.packages.client.compat.canvas.CanvasCompat;
import agency.highlysuspect.packages.client.compat.frex.FrexCompat;
import agency.highlysuspect.packages.client.screen.PScreens;
import agency.highlysuspect.packages.client.screen.PackageMakerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FrexCompat.onInitializeClient();
		CanvasCompat.onInitializeClient();
		
		PModelStuff.onInitializeClient();
		PackageMakerScreen.onInitializeClient();
		PScreens.onInitializeClient();
		PClientBlockEventHandlers.onInitializeClient();
		
		//block entity renderers
		BlockEntityRendererRegistry.INSTANCE.register(PBlockEntityTypes.PACKAGE, PackageBlockEntityRenderer::new);
		
		//BlockRenderLayerMap entries
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(), PBlocks.PACKAGE_MAKER);
	}
}
