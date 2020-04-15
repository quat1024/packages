package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.Random;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class PackageBakedModel extends ForwardingBakedModel {
	public PackageBakedModel(BakedModel other) {
		this.wrapped = other;
	}
	
	@Override
	public boolean isVanillaAdapter() {
		return false;
	}
	
	//TODO override getSprite since it gets chewed up by my cursed json hack
	
	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		Object ext = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
		if(!(ext instanceof PackageBlockEntity.Style)) {
			//Shit's fucked! TODO handle this better, if it even comes up at all
			return;
		}
		
		PackageBlockEntity.Style style = (PackageBlockEntity.Style) ext;
		BlockRenderManager mgr = MinecraftClient.getInstance().getBlockRenderManager();
		
		//BlockState frameState = style.frameBlock.getDefaultState();
		//BlockState innerState = style.innerBlock.getDefaultState();
		BlockState frameState = Blocks.OAK_LOG.getDefaultState();
		BlockState innerState = Blocks.OAK_PLANKS.getDefaultState();
		
		BakedModel frameModel = mgr.getModel(frameState);
		BakedModel innerModel = mgr.getModel(innerState);
		
		Sprite frameSprite = frameModel.getSprite();
		Sprite innerSprite = innerModel.getSprite();
		
		context.pushTransform(quad -> {
			if(quad.colorIndex() == 1) {
				quad.spriteBake(0, frameSprite, MutableQuadView.BAKE_NORMALIZED);
			} else if(quad.colorIndex() == 2) {
				quad.spriteBake(0, innerSprite, MutableQuadView.BAKE_NORMALIZED);
			}
			return true;
		});
		
		context.fallbackConsumer().accept(wrapped);
		
		context.popTransform();
		
		/*
		Object ext = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
		if(ext instanceof PackageBlockEntity.Style) {
			BlockState frame = ((PackageBlockEntity.Style) ext).frameBlock.getDefaultState();
			BlockState inner = ((PackageBlockEntity.Style) ext).innerBlock.getDefaultState();
			
			BlockRenderManager mgr = MinecraftClient.getInstance().getBlockRenderManager();
			
			BakedModel frameModel = mgr.getModel(frame);
			BakedModel innerModel = mgr.getModel(inner);
			
			Random random = randomSupplier.get();
			
			//TODO different textures per face, like boundary's Templates mod (this will probably get jazzy)
			Sprite frameSprite = frameModel.getSprite();
			Sprite innerSprite = innerModel.getSprite();
			
			List<BakedQuad> untexturedBarrelQuads = wrapped.getQuads(state, null, randomSupplier.get());
			QuadEmitter emitter = context.getEmitter();
			
			for(BakedQuad q : untexturedBarrelQuads) {
				//idk what im doing
				if(!q.hasColor()) context.getEmitter().fromVanilla(q.getVertexData(), 0, false).emit();
				else {
					if(q.getColorIndex() == 1) {
						context.getEmitter().fromVanilla(haxQuadTextureCoords(q.getVertexData(), frameSprite), 0, false).emit();
					} else {
						context.getEmitter().fromVanilla(haxQuadTextureCoords(q.getVertexData(), innerSprite), 0, false).emit();
					}
				}
			}
		} else {
			//Will probably fuck up royally, but what can you do
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		}*/
	}
	
	private static int[] haxQuadTextureCoords(int[] data, Sprite newSprite) {
		data[4] = Float.floatToRawIntBits(newSprite.getFrameU(0));
		data[5] = Float.floatToRawIntBits(newSprite.getFrameV(0));
		data[12] = Float.floatToRawIntBits(newSprite.getFrameU(1));
		data[13] = Float.floatToRawIntBits(newSprite.getFrameV(0));
		data[20] = Float.floatToRawIntBits(newSprite.getFrameU(1));
		data[21] = Float.floatToRawIntBits(newSprite.getFrameV(1));
		data[28] = Float.floatToRawIntBits(newSprite.getFrameU(0));
		data[29] = Float.floatToRawIntBits(newSprite.getFrameV(1));
		return data;
	}
}
