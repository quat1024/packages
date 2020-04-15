package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
@SuppressWarnings("unused")
public class ClientInit implements ClientModInitializer {
	private static final Identifier PACKAGE_SPECIAL = new Identifier(Packages.MODID, "special/package");
	private static final Identifier PACKAGE_BLOCK_MODEL_BASE = new Identifier(Packages.MODID, "block/package");
	
	@Override
	public void onInitializeClient() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(res -> (id, ctx) -> {
			if(PACKAGE_SPECIAL.equals(id)) {
				//TODO cache this and remember to dump the cache on resource reload
				return new PackageUnbakedModel(ctx.loadModel(PACKAGE_BLOCK_MODEL_BASE));
			} else return null;
		});
	}
}
