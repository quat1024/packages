package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.junk.PackageMakerStyle;
import agency.highlysuspect.packages.junk.PackageStyle;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This class takes a face color, frame block, and inner block, and produces... "a model".
 * The return type of the model is platform-specific: it might be a fabric rendering api Mesh, or it might be a List<BakedQuad>.
 *  (Or it could even be a BakedModel directly, when using it as a general-purpose model caching mechanism.)
 * 
 * The class is also shared across the Package and Package Crafter models, because they use the same retexturing technology.
 * So this is kind of a general-purpose, hard-to-explain nexus of functionality. The best kind of class.
 */
public interface PackageModelBakery<MODEL> {
	BakedModel getBaseModel();
	MODEL bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock);
	
	default MODEL bake(@Nullable PackageStyle style) {
		if(style == null) return bake(null, null, null, null);
		else return bake(style, style.color(), style.frameBlock(), style.innerBlock());
	}
	
	default MODEL bake(@Nullable PackageMakerStyle style) {
		if(style == null) return bake(null, null, null, null);
		else return bake(style, style.color(), style.frameBlock(), style.innerBlock());
	}
	
	/**
	 * An in-memory cache of package models.
	 */
	class Caching<MODEL> implements PackageModelBakery<MODEL> {
		public Caching(PackageModelBakery<MODEL> uncached) {
			this.uncached = uncached;
		}
		
		private final PackageModelBakery<MODEL> uncached;
		
		//HashMap does support null keys.
		//No method of evicting meshes from the cache is required because the entire PackageModelBakery is thrown out on resource reload.
		//
		//Also, yeehaw, throwing thread safety to the wind!!!
		//ConcurrentHashMap turned out to have too much overhead - this is an append-only map, I want reads to be backed by simple code.
		//Because model baking is usually pretty fast anyways, I don't care about spurious cache misses caused by e.g. reading while rehashing.
		//
		//"Surely this will not come back to bite me in the tail later"-driven-development
		private final Map<Object, MODEL> cache = new HashMap<>();
		private final Object UPDATE_LOCK = new Object();
		
		@Override
		public MODEL bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
			MODEL result = cache.get(cacheKey);
			if(result != null) return result;
			
			result = uncached.bake(cacheKey, faceColor, frameBlock, innerBlock);
			synchronized(UPDATE_LOCK) { cache.put(cacheKey, result); }
			return result;
		}
		
		@Override
		public BakedModel getBaseModel() {
			return uncached.getBaseModel();
		}
	}
	
	/**
	 * Layering on let another layer of annoying abstraction: an instance of this class produces PackageModelBakeries.
	 * The idea here is that you can keep an instance of this class in the UnbakedModel, and the methods prove useful
	 * towards supplying the necessary model dependencies to the model baking process?
	 */
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
		
		public PackageModelBakery<T> make(ModelBaker loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId) {
			UnbakedModel unbaked = loader.getModel(blockModelId);
			//TODO: why is the parent field not already set? Has resolveParent not been called on it?
			unbaked.resolveParents(loader::getModel);
			
			BakedModel baseModel = unbaked.bake(loader, textureGetter, rotationContainer, modelId);
			TextureAtlasSprite specialFrameSprite = textureGetter.apply(SPECIAL_FRAME);
			TextureAtlasSprite specialInnerSprite = textureGetter.apply(SPECIAL_INNER);
			
			PackageModelBakery<T> bakery = make(baseModel, specialFrameSprite, specialInnerSprite);
			if(PackagesClient.instance.config.get(PropsClient.CACHE_MESHES)) bakery = new Caching<>(bakery);
			return bakery;
		}
		
		public abstract PackageModelBakery<T> make(BakedModel baseModel, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite);
	}
}
