package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.entity.PBlockEntityTypes;
import agency.highlysuspect.packages.client.model.PackageUnbakedModel;
import agency.highlysuspect.packages.client.screen.PContainerScreens;
import agency.highlysuspect.packages.container.PackageMakerContainer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@SuppressWarnings("unused")
public class ClientInit implements ClientModInitializer {
	//Things to redirect
	private static final Identifier PACKAGE_SPECIAL = new Identifier(PackagesInit.MODID, "special/package");
	private static final Identifier PACKAGE_ITEM = new Identifier(PackagesInit.MODID, "item/package");
	
	//where to redirect them to
	private static final Identifier PACKAGE_BLOCK_MODEL_BASE = new Identifier(PackagesInit.MODID, "block/package");
	
	//cached unbaked model!
	private static PackageUnbakedModel packageUnbakedModel;
	
	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(res -> (id, ctx) -> {
			if(PACKAGE_SPECIAL.equals(id) || PACKAGE_ITEM.equals(id)) {
				if(packageUnbakedModel == null) packageUnbakedModel = new PackageUnbakedModel(ctx.loadModel(PACKAGE_BLOCK_MODEL_BASE));
				return packageUnbakedModel;
			} else return null;
		});
		regSimpleReloadListener("dump_package_model", (mgr) -> packageUnbakedModel = null);
		
		ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register((tex, reg) -> {
			reg.register(PackageMakerContainer.FRAME_BG);
			reg.register(PackageMakerContainer.INNER_BG);
			reg.register(PackageMakerContainer.DYE_BG);
		});
		
		BlockEntityRendererRegistry.INSTANCE.register(PBlockEntityTypes.PACKAGE, PackageBlockEntityRenderer::new);
		
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(), PBlocks.PACKAGE_MAKER);
		
		PClientBlockEventHandlers.onInitialize();
		PContainerScreens.onInitialize();
	}
	
	@SuppressWarnings("SameParameterValue") //i know i'm overengineering intellij. shut
	private static void regSimpleReloadListener(String idd, Consumer<ResourceManager> thing) {
		Identifier id = new Identifier(PackagesInit.MODID, idd);
		
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new SimpleSynchronousResourceReloadListener() {
				@Override
				public Identifier getFabricId() {
					return id;
				}
				
				@Override
				public void apply(ResourceManager manager) {
					thing.accept(manager);
				}
			}
		);
	}
}
