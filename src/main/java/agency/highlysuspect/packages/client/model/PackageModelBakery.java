package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.client.compat.frex.FrexCompat;
import agency.highlysuspect.packages.junk.PUtil;
import agency.highlysuspect.packages.junk.PackageStyle;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
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
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PackageModelBakery {
	public PackageModelBakery(BakedModel baseModel, Sprite specialFrameSprite, Sprite specialInnerSprite) {
		this.baseModel = baseModel;
		this.specialFrameSprite = specialFrameSprite;
		this.specialInnerSprite = specialInnerSprite;
	}
	
	public PackageModelBakery(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		this(
			loader.getOrLoadModel(PACKAGE_BLOCKMODEL).bake(loader, textureGetter, rotationContainer, modelId),
			textureGetter.apply(SPECIAL_FRAME),
			textureGetter.apply(SPECIAL_INNER)
		);
	}
	
	public static final Identifier PACKAGE_BLOCKMODEL = new Identifier(PackagesInit.MODID, "block/package");
	public static final SpriteIdentifier SPECIAL_FRAME = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(PackagesInit.MODID, "special/frame"));
	public static final SpriteIdentifier SPECIAL_INNER = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(PackagesInit.MODID, "special/inner"));
	
	public static abstract class DependentUnbakedModel implements UnbakedModel {
		@Override
		public Collection<Identifier> getModelDependencies() {
			return PUtil.arrayListOf(PACKAGE_BLOCKMODEL);
		}
		
		@Override
		public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
			//Ensure these are added to the texture dependencies, just in case someone removes them from the model, i don't want NPEs or anything due to these not being loaded
			return PUtil.concat(unbakedModelGetter.apply(PACKAGE_BLOCKMODEL).getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences), SPECIAL_FRAME, SPECIAL_INNER);
		}
	}
	
	public final BakedModel baseModel;
	private final Sprite specialFrameSprite;
	private final Sprite specialInnerSprite;
	
	public BakedModel getBaseModel() {
		return baseModel;
	}
	
	private final Map<PackageStyle, Mesh> meshCache = new ConcurrentHashMap<>();
	
	private static final Direction[] DIRECTIONS_AND_NULL = new Direction[]{
		Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, null
	};
	
	public Mesh getOrBake(PackageStyle style) {
		return meshCache.computeIfAbsent(style, x -> {
			BlockRenderManager mgr = MinecraftClient.getInstance().getBlockRenderManager();
			Renderer renderer = RendererAccess.INSTANCE.getRenderer();
			assert renderer != null;
			MeshBuilder meshBuilder = renderer.meshBuilder();
			QuadEmitter emitter = meshBuilder.getEmitter();
			
			Random random = new Random(42);
			BlockState packageState = PBlocks.PACKAGE.getDefaultState();
			
			for(Direction cullFace : DIRECTIONS_AND_NULL) {
				for(BakedQuad quad : baseModel.getQuads(packageState, cullFace, random)) {
					emitter.fromVanilla(quad, null, cullFace);
					emitter.material(null);
					
					if(style.hasColor() && emitter.colorIndex() == 1) {
						DyeColor color = Objects.requireNonNull(style.getColor());
						int tint = 0xFF000000 | color.getMaterialColor().color;
						emitter.spriteColor(0, tint, tint, tint, tint);
						emitter.emit();
						continue;
					}
					
					if(style.hasFrame()) {
						BlockState frameBlockState = Objects.requireNonNull(style.getFrame()).getDefaultState();
						Sprite frameSprite = mgr.getModel(frameBlockState).getSprite();
						
						if(recropIfInside(emitter, specialFrameSprite)) {
							emitter.spriteBake(0, frameSprite, MutableQuadView.BAKE_NORMALIZED);
							FrexCompat.PROXY.fancifyPackageQuad(emitter, frameBlockState, frameSprite);
							emitter.emit();
							continue;
						}
					}
					
					if(style.hasInner()) {
						BlockState innerBlockState = Objects.requireNonNull(style.getInner()).getDefaultState();
						Sprite innerSprite = mgr.getModel(innerBlockState).getSprite();
						
						if(recropIfInside(emitter, specialInnerSprite)) {
							emitter.spriteBake(0, innerSprite, MutableQuadView.BAKE_NORMALIZED);
							FrexCompat.PROXY.fancifyPackageQuad(emitter, innerBlockState, innerSprite);
							emitter.emit();
							continue;
						}
					}
				}
			}
			
			return meshBuilder.build();
		});
	}
	
	private static boolean recropIfInside(QuadEmitter emitter, Sprite sprite) {
		float spriteMinU = sprite.getMinU();
		float spriteMaxU = sprite.getMaxU();
		float spriteMinV = sprite.getMinV();
		float spriteMaxV = sprite.getMaxV();
		
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
		
		if(spriteMinU <= minU && spriteMaxU >= maxU && spriteMinV <= minV && spriteMaxV >= maxV) {
			float remappedMinU = rangeRemap(minU, spriteMinU, spriteMaxU, 0, 1);
			float remappedMaxU = rangeRemap(maxU, spriteMinU, spriteMaxU, 0, 1);
			float remappedMinV = rangeRemap(minV, spriteMinV, spriteMaxV, 0, 1);
			float remappedMaxV = rangeRemap(maxV, spriteMinV, spriteMaxV, 0, 1);
			
			//This loop has to go in reverse order or else UV mapping totally falls apart under Canvas. Not sure why, I should ask!
			//It's not float comparison issues, pretty sure (if i add an epsilon, it's still broken)
			for(int i = 3; i >= 0; i--) {
				float writeU = emitter.spriteU(i, 0) == minU ? remappedMinU : remappedMaxU;
				float writeV = emitter.spriteV(i, 0) == minV ? remappedMinV : remappedMaxV;
				emitter.sprite(i, 0, writeU, writeV);
			}
			
			return true;
		}
		return false;
	}
	
	//my favorite method in the whole wide world
	public static float rangeRemap(float value, float low1, float high1, float low2, float high2) {
		float value2 = MathHelper.clamp(value, low1, high1);
		return low2 + (value2 - low1) * (high2 - low2) / (high1 - low1);
	}
}
