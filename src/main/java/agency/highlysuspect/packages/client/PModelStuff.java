package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class PModelStuff {
	public static final ResourceLocation PACKAGE_BLOCK_SPECIAL = Packages.id("special/package");
	public static final ResourceLocation PACKAGE_ITEM_SPECIAL = Packages.id("item/package");
	private static UnbakedModel packageModel;
	
	public static final ResourceLocation PACKAGE_MAKER_BLOCK_SPECIAL = Packages.id("special/package_maker");
	public static final ResourceLocation PACKAGE_MAKER_ITEM_SPECIAL = Packages.id("item/package_maker");
	private static UnbakedModel packageMakerModel;
	
	public static void onInitializeClient() {
		//TODO: don't touch fabric here (not sure how it works on forge though)
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(res -> (id, ctx) -> {
			if(PACKAGE_BLOCK_SPECIAL.equals(id) || PACKAGE_ITEM_SPECIAL.equals(id)) {
				if(packageModel == null) packageModel = PackagesClient.instance.plat.createPackageModel();
				return packageModel;
			}
			
			if(PACKAGE_MAKER_BLOCK_SPECIAL.equals(id) || PACKAGE_MAKER_ITEM_SPECIAL.equals(id)) {
				if(packageMakerModel == null) packageMakerModel = PackagesClient.instance.plat.createPackageMakerModel();
				return packageMakerModel;
			}
			
			return null;
		});
		
		Packages.instance.plat.installResourceReloadListener(mgr -> {
			packageModel = null;
			packageMakerModel = null;
		}, Packages.id("dump_caches"), PackType.CLIENT_RESOURCES);
	}
}
