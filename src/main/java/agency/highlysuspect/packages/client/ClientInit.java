package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.entity.PBlockEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
@SuppressWarnings("unused")
public class ClientInit implements ClientModInitializer {
	//Things to redirect
	private static final Identifier PACKAGE_SPECIAL = new Identifier(Packages.MODID, "special/package");
	private static final Identifier PACKAGE_ITEM = new Identifier(Packages.MODID, "item/package");
	
	//where to redirect them to
	private static final Identifier PACKAGE_BLOCK_MODEL_BASE = new Identifier(Packages.MODID, "block/package");
	
	//cached unbaked model!
	private static PackageUnbakedModel packageUnbakedModel;
	
	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(res -> (id, ctx) -> {
			if(PACKAGE_SPECIAL.equals(id) || PACKAGE_ITEM.equals(id)) {
				//TODO: dump this cache on resource reload
				if(packageUnbakedModel == null) packageUnbakedModel = new PackageUnbakedModel(ctx.loadModel(PACKAGE_BLOCK_MODEL_BASE));
				return packageUnbakedModel;
			} else return null;
		});
		
		BlockEntityRendererRegistry.INSTANCE.register(PBlockEntityTypes.PACKAGE, PackageBlockEntityRenderer::new);
	}
}
