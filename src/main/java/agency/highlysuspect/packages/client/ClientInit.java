package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.entity.PBlockEntityTypes;
import agency.highlysuspect.packages.client.model.PackageUnbakedModel;
import agency.highlysuspect.packages.client.screen.PScreens;
import agency.highlysuspect.packages.container.PackageMakerScreenHandler;
import agency.highlysuspect.packages.net.PNetClient;
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

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {
	//Hey just FYI, this class is kind of a nexus of a bunch of different, unrelated, client-y subsystems.
	//None of them are big enough to justify splitting into a whole separate class, but I need them all somewhere.
	//So if you're making a bigger mod, maybe don't structure it like this.
	
	/////Models
	//Things to redirect
	private static final Identifier PACKAGE_SPECIAL = new Identifier(PackagesInit.MODID, "special/package");
	private static final Identifier PACKAGE_ITEM = new Identifier(PackagesInit.MODID, "item/package");
	
	//where to redirect them to
	private static final Identifier PACKAGE_BLOCK_MODEL_BASE = new Identifier(PackagesInit.MODID, "block/package");
	
	//cached unbaked model!
	private static PackageUnbakedModel packageUnbakedModel;
	
	@Override
	public void onInitializeClient() {
		/////Funky hacky package model
		//Loading it
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(res -> (id, ctx) -> {
			if(PACKAGE_SPECIAL.equals(id) || PACKAGE_ITEM.equals(id)) {
				if(packageUnbakedModel == null) packageUnbakedModel = new PackageUnbakedModel(ctx.loadModel(PACKAGE_BLOCK_MODEL_BASE));
				return packageUnbakedModel;
			} else return null;
		});
		Identifier id = new Identifier(PackagesInit.MODID, "dump_package_model");
		
		//Unloading it when you reload resources
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new SimpleSynchronousResourceReloadListener() {
				@Override
				public Identifier getFabricId() {
					return id;
				}
				
				@Override
				public void apply(ResourceManager manager) {
					packageUnbakedModel = null;
				}
			}
		);
		
		/////Sprites for the package maker GUI
		
		//SpriteAtlasTexture.BLOCK_ATLAS_TEX is deprecated but as of 1.16.2 is used all throughout vanilla code
		//So I am not bothered.
		//noinspection deprecation
		ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register((tex, reg) -> {
			reg.register(PackageMakerScreenHandler.FRAME_BG);
			reg.register(PackageMakerScreenHandler.INNER_BG);
			reg.register(PackageMakerScreenHandler.DYE_BG);
		});
		
		/////Block entity renderers
		BlockEntityRendererRegistry.INSTANCE.register(PBlockEntityTypes.PACKAGE, PackageBlockEntityRenderer::new);
		
		/////BlockRenderLayerMap entries
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(), PBlocks.PACKAGE_MAKER);
		
		/////We have EventBusSubscriber at home.
		PClientBlockEventHandlers.onInitialize();
		
		/////Screen handling
		PScreens.onInitialize();
		
		/////Misc c->s networking (actually this is empty right now)
		PNetClient.onInitialize();
	}
	
}
