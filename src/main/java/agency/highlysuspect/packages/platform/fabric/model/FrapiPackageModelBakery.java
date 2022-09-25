package agency.highlysuspect.packages.platform.fabric.model;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.platform.fabric.compat.frex.FrexCompat;
import agency.highlysuspect.packages.client.PackageModelBakery;
import agency.highlysuspect.packages.junk.PUtil;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@SuppressWarnings("ClassCanBeRecord")
public class FrapiPackageModelBakery implements PackageModelBakery<Mesh> {
	public final BakedModel baseModel;
	public final TextureAtlasSprite specialFrameSprite;
	public final TextureAtlasSprite specialInnerSprite;
	
	public FrapiPackageModelBakery(BakedModel baseModel, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
		this.baseModel = baseModel;
		this.specialFrameSprite = specialFrameSprite;
		this.specialInnerSprite = specialInnerSprite;
	}
	
	@Override
	public BakedModel getBaseModel() {
		return baseModel;
	}
	
	public Mesh bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
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
			for(BakedQuad quad : baseModel.getQuads(PBlocks.PACKAGE.get().defaultBlockState(), cullFace, random)) {
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
}
