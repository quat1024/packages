package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.Packages;
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
	private final PackageModelBakery.Factory<T> MODEL_BAKERY_FACTORY = makeFactory();
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return MODEL_BAKERY_FACTORY.getDependencies();
	}
	
	@Override
	public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return MODEL_BAKERY_FACTORY.getMaterials(unbakedModelGetter, unresolvedTextureReferences);
	}
	
	@Override
	public BakedModel bake(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId) {
		PackageModelBakery<T> bakery = MODEL_BAKERY_FACTORY.make(loader, textureGetter, rotationContainer, modelId);
		if(Packages.instance.config.cacheMeshes) bakery = new PackageModelBakery.Caching<>(bakery);
		return bake(bakery);
	}
	
	protected abstract PackageModelBakery.Factory<T> makeFactory();
	protected abstract BakedModel bake(PackageModelBakery<T> factoryResult);
}
