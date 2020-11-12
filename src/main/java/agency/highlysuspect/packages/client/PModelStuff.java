package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.client.model.PackageBakedModel;
import agency.highlysuspect.packages.client.model.PackageUnbakedModel;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class PModelStuff {
	private static PackageUnbakedModel packageUnbakedModel;
	
	public static void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(res -> (id, ctx) -> {
			if(PackageUnbakedModel.PACKAGE_SPECIAL.equals(id) || PackageUnbakedModel.ITEM_SPECIAL.equals(id)) {
				if(packageUnbakedModel == null) packageUnbakedModel = new PackageUnbakedModel(ctx);
				return packageUnbakedModel;
			} else return null;
		});
		
		Identifier id = new Identifier(PackagesInit.MODID, "dump_caches");
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new SimpleSynchronousResourceReloadListener() {
				@Override
				public Identifier getFabricId() {
					return id;
				}
				
				@Override
				public void apply(ResourceManager manager) {
					packageUnbakedModel = null;
					PackageBakedModel.dumpCache();
				}
			}
		);
	}
}
