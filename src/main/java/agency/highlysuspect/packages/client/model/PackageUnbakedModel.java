package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.junk.BakedQuadExt;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class PackageUnbakedModel implements UnbakedModel {
	public PackageUnbakedModel(UnbakedModel basePackage) {
		this.basePackage = basePackage;
	}
	
	private final UnbakedModel basePackage;
	
	private static final Identifier SPECIAL_FRAME = new Identifier(PackagesInit.MODID, "special/frame");
	private static final Identifier SPECIAL_INNER = new Identifier(PackagesInit.MODID, "special/inner");
	
	private static final Direction[] DIRECTIONS_AND_NULL = new Direction[]{
		Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, null
	};
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		return basePackage.getModelDependencies();
	}
	
	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return basePackage.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences);
	}
	
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		BakedModel fromJson = basePackage.bake(loader,	textureGetter, rotationContainer, modelId);
		Preconditions.checkNotNull(fromJson, "null model when loading the barrel model?!");
		
		//When canvas(JMX) is present, mixing a getSprite() onto BakedQuad and calling it on a quad from BakedModel#getQuads implemented by JMX,
		//you just get the particle texture.
		//My model depends on being able to tell textures apart, so, I compare texture coordinates instead.
		Sprite specialFrameSprite = textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, SPECIAL_FRAME));
		Sprite specialInnerSprite = textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, SPECIAL_INNER));
		
		Random probablyShouldntPassNullHere = new Random();
		
		Renderer renderer = RendererAccess.INSTANCE.getRenderer();
		assert renderer != null;
		MeshBuilder mb = renderer.meshBuilder();
		QuadEmitter emitter = mb.getEmitter();
		
		for (Direction cullFace : DIRECTIONS_AND_NULL) {
			for (BakedQuad quad : fromJson.getQuads(PBlocks.PACKAGE.getDefaultState(), cullFace, probablyShouldntPassNullHere)) {
				emitter.fromVanilla(quad, null, cullFace);
				
				if(remapToUnitSquare(emitter, specialFrameSprite)) {
					emitter.tag(1);
				} else if(remapToUnitSquare(emitter, specialInnerSprite)) {
					emitter.tag(2);
				} else if(quad.getColorIndex() == 1) {
					emitter.tag(3);
				}
				
				hackyJmxFix(emitter);
				
				emitter.emit();
			}
		}
		
		return new PackageBakedModel(fromJson, mb.build());
	}
	
	private static void hackyJmxFix(QuadEmitter a) {
		//shh
		Class<? extends QuadEmitter> classs = a.getClass();
		if(classs.getName().equals("grondag.canvas.apiimpl.mesh.MeshBuilderImpl$Maker")) {
			Class<?> qviclass = classs.getSuperclass().getSuperclass();
			if(qviclass.getName().equals("grondag.canvas.apiimpl.mesh.QuadViewImpl")) {
				//shh   it's ok      you don't have to call findSprite and NPE on something
				//(i actually don't know why the game explodes)
				
				Field flagField;
				try {
					//older versions of canvas
					flagField = qviclass.getDeclaredField("spriteMappedFlags");
					flagField.setAccessible(true);
					flagField.setInt(a, 0);
				} catch (ReflectiveOperationException e) {
					//swallow it, yummy yummy exception
				}
				
				try {
					//newer versions of canvas
					flagField = qviclass.getDeclaredField("spriteMappedFlag");
					flagField.setAccessible(true);
					flagField.setBoolean(a, false);
				} catch(ReflectiveOperationException e) {
					return;
				}
			}
		}
	}
	
	//Imagine a red box encompassing the Sprite on its texture atlas, and the QuadEmitter's UV coordinates on the atlas are a blue box.
	//If the blue box is not inside the red box, this method does nothing and returns false.
	//If they are, this method scales the red box up to fill the entire atlas (the unit square), while keeping the proportional sizes of the boxes the same.
	private static boolean remapToUnitSquare(QuadEmitter emitter, Sprite sprite) {
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