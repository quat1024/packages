package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.junk.PUtil;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface PackageModelBakery<T> {
	BakedModel getBaseModel();
	T bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock);
	
	class Caching<T> implements PackageModelBakery<T> {
		public Caching(PackageModelBakery<T> uncached) { this.uncached = uncached; }
		private final PackageModelBakery<T> uncached;
		
		//HashMap does support null keys.
		//No method of evicting meshes from the cache is required because the entire PackageModelBakery is thrown out on resource reload.
		//Also, yeehaw, throwing thread safety to the wind!!!
		//ConcurrentHashMap turned out to have too much overhead - this is an append-only map, I want reads to be backed by simple code.
		//Also because model baking is usually pretty fast anyways, I don't care about spurious cache misses caused by e.g. reading while rehashing
		private final Map<Object, T> bakedModelCache = new HashMap<>();
		private final Object UPDATE_LOCK = new Object();
		
		@Override
		public T bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
			T result = bakedModelCache.get(cacheKey);
			if(result != null) return result;
			
			result = uncached.bake(cacheKey, faceColor, frameBlock, innerBlock);
			synchronized(UPDATE_LOCK) { bakedModelCache.put(cacheKey, result); }
			return result;
		}
		
		@Override
		public BakedModel getBaseModel() {
			return uncached.getBaseModel();
		}
	}
	
	abstract class Factory<T> {
		@SuppressWarnings("deprecation") private static final Material SPECIAL_FRAME = new Material(TextureAtlas.LOCATION_BLOCKS, Packages.id("special/frame"));
		@SuppressWarnings("deprecation") private static final Material SPECIAL_INNER = new Material(TextureAtlas.LOCATION_BLOCKS, Packages.id("special/inner"));
		
		private final ResourceLocation blockModelId;
		
		public Factory(ResourceLocation blockModelId) {
			this.blockModelId = blockModelId;
		}
		
		public Collection<ResourceLocation> getDependencies() {
			return ImmutableList.of(blockModelId);
		}
		
		public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
			return PUtil.concat(
				unbakedModelGetter.apply(blockModelId).getMaterials(unbakedModelGetter, unresolvedTextureReferences),
				ImmutableList.of(SPECIAL_FRAME, SPECIAL_INNER)
			);
		}
		
		public PackageModelBakery<T> make(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId) {
			BakedModel baseModel = loader.getModel(blockModelId).bake(loader, textureGetter, rotationContainer, modelId);
			TextureAtlasSprite specialFrameSprite = textureGetter.apply(SPECIAL_FRAME);
			TextureAtlasSprite specialInnerSprite = textureGetter.apply(SPECIAL_INNER);
			
			PackageModelBakery<T> bakery = make(baseModel, specialFrameSprite, specialInnerSprite);
			if(Packages.instance.config.cacheMeshes) bakery = new Caching<>(bakery);
			return bakery;
		}
		
		public abstract PackageModelBakery<T> make(BakedModel baseModel, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite);
	}
}
