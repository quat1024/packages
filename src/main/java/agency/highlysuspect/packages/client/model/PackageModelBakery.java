package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.client.compat.frex.FrexCompat;
import agency.highlysuspect.packages.junk.PUtil;
import agency.highlysuspect.packages.junk.PackageStyle;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public final class PackageModelBakery {
	private static final SpriteIdentifier SPECIAL_FRAME = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(PackagesInit.MODID, "special/frame"));
	private static final SpriteIdentifier SPECIAL_INNER = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(PackagesInit.MODID, "special/inner"));
	private final BakedModel baseModel;
	private final Sprite specialFrameSprite;
	private final Sprite specialInnerSprite;
	
	public PackageModelBakery(BakedModel baseModel, Sprite specialFrameSprite, Sprite specialInnerSprite) {
		this.baseModel = baseModel;
		this.specialFrameSprite = specialFrameSprite;
		this.specialInnerSprite = specialInnerSprite;
	}
	
	public static final class Spec {
		private final Identifier blockModelId;
		
		public Spec(Identifier blockModelId) {this.blockModelId = blockModelId;}
		
		public Collection<Identifier> modelDependencies() {
			return ImmutableList.of(blockModelId);
		}
		
		public Collection<SpriteIdentifier> textureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
			return PUtil.concat(
				unbakedModelGetter.apply(blockModelId).getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences),
				ImmutableList.of(SPECIAL_FRAME, SPECIAL_INNER)
			);
		}
		
		public PackageModelBakery make(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
			return new PackageModelBakery(
				loader.getOrLoadModel(blockModelId).bake(loader, textureGetter, rotationContainer, modelId),
				textureGetter.apply(SPECIAL_FRAME),
				textureGetter.apply(SPECIAL_INNER)
			);
		}
		
		public Identifier blockModelId() {return blockModelId;}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this) return true;
			if(obj == null || obj.getClass() != this.getClass()) return false;
			var that = (Spec) obj;
			return Objects.equals(this.blockModelId, that.blockModelId);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(blockModelId);
		}
		
		@Override
		public String toString() {
			return "Spec[" +
				"blockModelId=" + blockModelId + ']';
		}
		
	}
	
	public Mesh bake(PackageStyle style) {
		return bake(style.color(), style.frameBlock(), style.innerBlock());
	}
	
	public Mesh bake(@Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
		BlockRenderManager mgr = MinecraftClient.getInstance().getBlockRenderManager();
		Renderer renderer = RendererAccess.INSTANCE.getRenderer();
		assert renderer != null;
		
		MeshBuilder meshBuilder = renderer.meshBuilder();
		QuadEmitter emitter = meshBuilder.getEmitter();
		
		Random random = new Random(42);
		
		@Nullable BlockState frameState = frameBlock == null ? null : frameBlock.getDefaultState();
		@Nullable Sprite frameSprite = frameState == null ? null : mgr.getModel(frameState).getSprite();
		
		@Nullable BlockState innerState = innerBlock == null ? null : innerBlock.getDefaultState();
		@Nullable Sprite innerSprite = innerState == null ? null : mgr.getModel(innerState).getSprite();
		
		for(Direction cullFace : PUtil.DIRECTIONS_AND_NULL) {
			for(BakedQuad quad : baseModel.getQuads(PBlocks.PACKAGE.getDefaultState(), cullFace, random)) {
				emitter.fromVanilla(quad, null, cullFace);
				emitter.material(null);
				
				if(emitter.colorIndex() == 1) {
					if(faceColor != null) {
						int tint = 0xFF000000 | faceColor.getMapColor().color;
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
	
	public BakedModel baseModel() {return baseModel;}
	
	public Sprite specialFrameSprite() {return specialFrameSprite;}
	
	public Sprite specialInnerSprite() {return specialInnerSprite;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (PackageModelBakery) obj;
		return Objects.equals(this.baseModel, that.baseModel) &&
			Objects.equals(this.specialFrameSprite, that.specialFrameSprite) &&
			Objects.equals(this.specialInnerSprite, that.specialInnerSprite);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(baseModel, specialFrameSprite, specialInnerSprite);
	}
	
	@Override
	public String toString() {
		return "PackageModelBakery[" +
			"baseModel=" + baseModel + ", " +
			"specialFrameSprite=" + specialFrameSprite + ", " +
			"specialInnerSprite=" + specialInnerSprite + ']';
	}
	
	
	static final class SpriteUvBounds {
		private final float minU;
		private final float maxU;
		private final float minV;
		private final float maxV;
		
		SpriteUvBounds(float minU, float maxU, float minV, float maxV) {
			this.minU = minU;
			this.maxU = maxU;
			this.minV = minV;
			this.maxV = maxV;
		}
		
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
		
		boolean displaysSprite(Sprite sprite) {
			return sprite.getMinU() <= minU && sprite.getMaxU() >= maxU && sprite.getMinV() <= minV && sprite.getMaxV() >= maxV;
		}
		
		void normalizeEmitter(QuadEmitter emitter, Sprite sprite) {
			float remappedMinU = PUtil.rangeRemap(minU, sprite.getMinU(), sprite.getMaxU(), 0, 1);
			float remappedMaxU = PUtil.rangeRemap(maxU, sprite.getMinU(), sprite.getMaxU(), 0, 1);
			float remappedMinV = PUtil.rangeRemap(minV, sprite.getMinV(), sprite.getMaxV(), 0, 1);
			float remappedMaxV = PUtil.rangeRemap(maxV, sprite.getMinV(), sprite.getMaxV(), 0, 1);
			
			//This loop has to go in reverse order or else UV mapping totally falls apart under Canvas (last I checked). Not sure why, I should ask!
			//It's not float comparison issues, pretty sure (if i add an epsilon, it's still broken)
			for(int i = 3; i >= 0; i--) {
				float writeU = emitter.spriteU(i, 0) == minU ? remappedMinU : remappedMaxU;
				float writeV = emitter.spriteV(i, 0) == minV ? remappedMinV : remappedMaxV;
				emitter.sprite(i, 0, writeU, writeV);
			}
		}
		
		public float minU() {return minU;}
		
		public float maxU() {return maxU;}
		
		public float minV() {return minV;}
		
		public float maxV() {return maxV;}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this) return true;
			if(obj == null || obj.getClass() != this.getClass()) return false;
			var that = (SpriteUvBounds) obj;
			return Float.floatToIntBits(this.minU) == Float.floatToIntBits(that.minU) &&
				Float.floatToIntBits(this.maxU) == Float.floatToIntBits(that.maxU) &&
				Float.floatToIntBits(this.minV) == Float.floatToIntBits(that.minV) &&
				Float.floatToIntBits(this.maxV) == Float.floatToIntBits(that.maxV);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(minU, maxU, minV, maxV);
		}
		
		@Override
		public String toString() {
			return "SpriteUvBounds[" +
				"minU=" + minU + ", " +
				"maxU=" + maxU + ", " +
				"minV=" + minV + ", " +
				"maxV=" + maxV + ']';
		}
		
	}
}
