package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.client.PackagesClient;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractPackageModel<T> implements UnbakedModel {
	protected final PackageModelBakery.Factory<T> modelBakeryFactory = makeFactory();
	public static final ResourceLocation PACKAGE_BLOCK_SPECIAL = Packages.id("special/package");
	public static final ResourceLocation PACKAGE_ITEM_SPECIAL = Packages.id("item/package");
	public static final ResourceLocation PACKAGE_MAKER_BLOCK_SPECIAL = Packages.id("special/package_maker");
	public static final ResourceLocation PACKAGE_MAKER_ITEM_SPECIAL = Packages.id("item/package_maker");
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return modelBakeryFactory.getDependencies();
	}
	
	@Override
	public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return modelBakeryFactory.getMaterials(unbakedModelGetter, unresolvedTextureReferences);
	}
	
	@Override
	public BakedModel bake(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId) {
		PackageModelBakery<T> bakery = modelBakeryFactory.make(loader, textureGetter, rotationContainer, modelId);
		if(PackagesClient.instance.config.cacheMeshes) bakery = new PackageModelBakery.Caching<>(bakery);
		return toBakedModel(bakery);
	}
	
	protected abstract PackageModelBakery.Factory<T> makeFactory();
	protected abstract BakedModel toBakedModel(PackageModelBakery<T> factoryResult);
}
