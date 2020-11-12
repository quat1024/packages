package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.client.compat.frex.FrexCompat;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class PackageBakedModel extends ForwardingBakedModel {
	public PackageBakedModel(BakedModel other, Sprite specialFrameSprite, Sprite specialInnerSprite) {
		this.wrapped = other;
		this.specialFrameSprite = specialFrameSprite;
		this.specialInnerSprite = specialInnerSprite;
	}
	
	private final Sprite specialFrameSprite, specialInnerSprite;
	
	private static final Direction[] DIRECTIONS_AND_NULL = new Direction[]{
		Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, null
	};
	
	private static final Map<PackageStyle, Mesh> meshCache = new ConcurrentHashMap<>();
	
	public static void dumpCache() {
		meshCache.clear();
	}
	
	@Override
	public boolean isVanillaAdapter() {
		return false;
	}
	
	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		Object ext = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
		
		PackageStyle style;
		if(ext instanceof PackageStyle) {
			style = (PackageStyle) ext;
		} else {
			style = PackageStyle.FALLBACK;
		}
		
		emitFrame(context, style);
	}
	
	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		emitFrame(context, PackageStyle.fromItemStack(stack));
	}
	
	private void emitFrame(RenderContext context, PackageStyle style) {
		context.meshConsumer().accept(meshCache.computeIfAbsent(style, x -> {
			//Compute some resources
			BlockState frameBlockState = style.frameBlock.getDefaultState();
			BlockState innerBlockState = style.innerBlock.getDefaultState();
			
			BlockRenderManager mgr = MinecraftClient.getInstance().getBlockRenderManager();
			Sprite frameSprite = mgr.getModel(frameBlockState).getSprite();
			Sprite innerSprite = mgr.getModel(innerBlockState).getSprite();
			
			DyeColor color = style.color;
			int tint = 0xFF000000 | color.getMaterialColor().color;
			
			//Get ready to produce mesh
			Renderer renderer = RendererAccess.INSTANCE.getRenderer();
			assert renderer != null;
			MeshBuilder meshBuilder = renderer.meshBuilder();
			QuadEmitter emitter = meshBuilder.getEmitter();
			
			Random random = new Random(42);
			BlockState state = PBlocks.PACKAGE.getDefaultState();
			for(Direction cullFace : DIRECTIONS_AND_NULL) {
				for(BakedQuad quad : wrapped.getQuads(state, cullFace, random)) {
					//copy into the quad emitter
					emitter.fromVanilla(quad, null, cullFace);
					
					if(recropIfInside(emitter, specialFrameSprite)) { //if it's a frame quad
						emitter.spriteBake(0, frameSprite, MutableQuadView.BAKE_NORMALIZED);
						FrexCompat.PROXY.fancifyPackageQuad(emitter, frameBlockState, frameSprite);
					} else if(recropIfInside(emitter, specialInnerSprite)) { //if it's an inner quad
						emitter.spriteBake(0, innerSprite, MutableQuadView.BAKE_NORMALIZED);
						FrexCompat.PROXY.fancifyPackageQuad(emitter, innerBlockState, innerSprite);
					}
					
					if(emitter.colorIndex() == 1) { //the "front" quad, by default
						emitter.spriteColor(0, tint, tint, tint, tint);
					}
					
					//and let's go
					emitter.emit();
				}
			}
			
			return meshBuilder.build();
		}));
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
