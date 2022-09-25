package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PBlockEntityTypes;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.client.compat.frex.FrexCompat;
import agency.highlysuspect.packages.container.PMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;

public class ClientInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FrexCompat.onInitializeClient();
		
		PModelStuff.onInitializeClient();
		
		MenuScreens.register(PMenuTypes.PACKAGE_MAKER.get(), PackageMakerScreen::new);
		PackageMakerScreen.initIcons();
		
		PClientBlockEventHandlers.onInitializeClient();
		
		//block entity renderers
		BlockEntityRendererRegistry.register(PBlockEntityTypes.PACKAGE.get(), PackageRenderer::new);
		
		//BlockRenderLayerMap entries
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutoutMipped(), PBlocks.PACKAGE_MAKER.get());
	}
}
