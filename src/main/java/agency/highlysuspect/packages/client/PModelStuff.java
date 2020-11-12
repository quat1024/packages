package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.client.model.PackageMakerModel;
import agency.highlysuspect.packages.client.model.PackageModel;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class PModelStuff {
	private static PackageModel packageModel;
	private static PackageMakerModel packageMakerModel;
	
	public static void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(res -> (id, ctx) -> {
			if(PackageModel.PACKAGE_SPECIAL.equals(id) || PackageModel.ITEM_SPECIAL.equals(id)) {
				if(packageModel == null) packageModel = new PackageModel();
				return packageModel;
			}
			
			if(PackageMakerModel.PACKAGE_MAKER_SPECIAL.equals(id)) {
				if(packageMakerModel == null) packageMakerModel = new PackageMakerModel();
				return packageMakerModel;
			}
			
			return null;
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
					packageModel = null;
					packageMakerModel = null;
				}
			}
		);
	}
}
