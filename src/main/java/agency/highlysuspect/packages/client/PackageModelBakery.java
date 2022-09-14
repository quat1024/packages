package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Init;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.client.compat.frex.FrexCompat;
import agency.highlysuspect.packages.junk.PUtil;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class PackageModelBakery {
	@SuppressWarnings("deprecation") private static final Material SPECIAL_FRAME = new Material(TextureAtlas.LOCATION_BLOCKS, Init.id("special/frame"));
	@SuppressWarnings("deprecation") private static final Material SPECIAL_INNER = new Material(TextureAtlas.LOCATION_BLOCKS, Init.id("special/inner"));
	
	public final BakedModel baseModel;
	public final TextureAtlasSprite specialFrameSprite;
	public final TextureAtlasSprite specialInnerSprite;
	
	public PackageModelBakery(BakedModel baseModel, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
		this.baseModel = baseModel;
		this.specialFrameSprite = specialFrameSprite;
		this.specialInnerSprite = specialInnerSprite;
	}
	
	public record Spec(ResourceLocation blockModelId) {
		public Collection<ResourceLocation> modelDependencies() {
			return ImmutableList.of(blockModelId);
		}
		
		public Collection<Material> textureDependencies(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
			return PUtil.concat(
				unbakedModelGetter.apply(blockModelId).getMaterials(unbakedModelGetter, unresolvedTextureReferences),
				ImmutableList.of(SPECIAL_FRAME, SPECIAL_INNER)
			);
		}
		
		public PackageModelBakery make(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId) {
			BakedModel baseModel = loader.getModel(blockModelId).bake(loader, textureGetter, rotationContainer, modelId);
			TextureAtlasSprite specialFrameSprite = textureGetter.apply(SPECIAL_FRAME);
			TextureAtlasSprite specialInnerSprite = textureGetter.apply(SPECIAL_INNER);
			
			if(Init.config.cacheMeshes) return new Caching(baseModel, specialFrameSprite, specialInnerSprite);
			else return new PackageModelBakery(baseModel, specialFrameSprite, specialInnerSprite);
		}
	}
	
	protected Mesh bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
		BlockRenderDispatcher mgr = Minecraft.getInstance().getBlockRenderer();
		Renderer renderer = RendererAccess.INSTANCE.getRenderer();
		assert renderer != null;
		
		MeshBuilder meshBuilder = renderer.meshBuilder();
		QuadEmitter emitter = meshBuilder.getEmitter();
		
		@Nullable BlockState frameState = frameBlock == null ? null : frameBlock.defaultBlockState();
		@Nullable TextureAtlasSprite frameSprite = frameState == null ? null : mgr.getBlockModel(frameState).getParticleIcon();
		
		@Nullable BlockState innerState = innerBlock == null ? null : innerBlock.defaultBlockState();
		@Nullable TextureAtlasSprite innerSprite = innerState == null ? null : mgr.getBlockModel(innerState).getParticleIcon();
		
		Random random = new Random(42);
		for(Direction cullFace : PUtil.DIRECTIONS_AND_NULL) {
			for(BakedQuad quad : baseModel.getQuads(PBlocks.PACKAGE.defaultBlockState(), cullFace, random)) {
				emitter.fromVanilla(quad, null, cullFace);
				emitter.material(null);
				
				if(emitter.colorIndex() == 1) {
					if(faceColor != null) {
						int tint = 0xFF000000 | faceColor.getMaterialColor().col;
						emitter.spriteColor(0, tint, tint, tint, tint);
						emitter.emit();
					}
					continue;
				}
				
				SpriteUvBounds bounds = SpriteUvBounds.readOff(emitter);
				
				if(bounds.displaysSprite(specialFrameSprite)) {
					if(frameBlock != null) {
						bounds.normalizeEmitter(emitter, specialFrameSprite);
						emitter.spriteBake(0, frameSprite, MutableQuadView.BAKE_NORMALIZED);
						FrexCompat.PROXY.fancifyPackageQuad(emitter, frameState, frameSprite);
						emitter.emit();
					}
					continue;
				}
				
				if(bounds.displaysSprite(specialInnerSprite)) {
					if(innerBlock != null) {
						bounds.normalizeEmitter(emitter, specialInnerSprite);
						emitter.spriteBake(0, innerSprite, MutableQuadView.BAKE_NORMALIZED);
						FrexCompat.PROXY.fancifyPackageQuad(emitter, innerState, innerSprite);
						emitter.emit();
					}
					continue;
				}
				
				//It's not a special quad, so leave it as-is.
				emitter.emit();
			}
		}
		
		return meshBuilder.build();
	}
	
	record SpriteUvBounds(float minU, float maxU, float minV, float maxV) {
		static SpriteUvBounds readOff(QuadEmitter emitter) {
			float minU = Float.POSITIVE_INFINITY;
			float maxU = Float.NEGATIVE_INFINITY;
			float minV = Float.POSITIVE_INFINITY;
			float maxV = Float.NEGATIVE_INFINITY;
			
			for(int i = 0; i < 4; i++) {
				float u = emitter.spriteU(i, 0);
				if(minU > u) minU = u;
				if(maxU < u) maxU = u;
				
				float v = emitter.spriteV(i, 0);
				if(minV > v) minV = v;
				if(maxV < v) maxV = v;
			}
			
			return new SpriteUvBounds(minU, maxU, minV, maxV);
		}
		
		boolean displaysSprite(TextureAtlasSprite sprite) {
			return sprite.getU0() <= minU && sprite.getU1() >= maxU && sprite.getV0() <= minV && sprite.getV1() >= maxV;
		}
		
		void normalizeEmitter(QuadEmitter emitter, TextureAtlasSprite sprite) {
			float remappedMinU = PUtil.rangeRemap(minU, sprite.getU0(), sprite.getU1(), 0, 1);
			float remappedMaxU = PUtil.rangeRemap(maxU, sprite.getU0(), sprite.getU1(), 0, 1);
			float remappedMinV = PUtil.rangeRemap(minV, sprite.getV0(), sprite.getV1(), 0, 1);
			float remappedMaxV = PUtil.rangeRemap(maxV, sprite.getV0(), sprite.getV1(), 0, 1);
			
			for(int i = 0; i < 4; i++) {
				float writeU = Mth.equal(emitter.spriteU(i, 0), minU) ? remappedMinU : remappedMaxU;
				float writeV = Mth.equal(emitter.spriteV(i, 0), minV) ? remappedMinV : remappedMaxV;
				emitter.sprite(i, 0, writeU, writeV);
			}
		}
	}
	
	public static class Caching extends PackageModelBakery {
		public Caching(BakedModel baseModel, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
			super(baseModel, specialFrameSprite, specialInnerSprite);
		}
		
		//HashMap does support null keys.
		//No method of evicting meshes from the cache is required because the entire PackageModelBakery is thrown out on resource reload.
		//Also, yeehaw, throwing thread safety to the wind!!!
		//ConcurrentHashMap turned out to have too much overhead - this is an append-only map, I want reads to be backed by simple code.
		//Also because model baking is usually pretty fast anyways, I don't care about spurious cache misses caused by e.g. reading while rehashing
		private final Map<Object, Mesh> bakedModelCache = new HashMap<>();
		private static final Object UPDATE_LOCK = new Object();
		
		@Override
		protected Mesh bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
			Mesh mesh = bakedModelCache.get(cacheKey);
			if(mesh != null) return mesh;
			
			mesh = super.bake(cacheKey, faceColor, frameBlock, innerBlock);
			synchronized(UPDATE_LOCK) { bakedModelCache.put(cacheKey, mesh); }
			return mesh;
		}
	}
}
