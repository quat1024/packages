package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.PBlockEntityTypes;
import agency.highlysuspect.packages.client.compat.frex.FrexCompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FrexCompat.onInitializeClient();
		
		PModelStuff.onInitializeClient();
		PackageMakerScreen.onInitializeClient();
		PScreens.onInitializeClient();
		PClientBlockEventHandlers.onInitializeClient();
		
		//block entity renderers
		BlockEntityRendererRegistry.register(PBlockEntityTypes.PACKAGE, PackageRenderer::new);
		
		//BlockRenderLayerMap entries
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutoutMipped(), PBlocks.PACKAGE_MAKER);
	}
}
