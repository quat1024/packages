package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.Random;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class PackageBakedModel extends ForwardingBakedModel {
	public PackageBakedModel(BakedModel other, Mesh frameMesh, Mesh innerMesh, Mesh faceMesh, Mesh theRestMesh) {
		this.wrapped = other;
		
		this.frameMesh = frameMesh;
		this.innerMesh = innerMesh;
		this.faceMesh = faceMesh;
		this.theRestMesh = theRestMesh;
	}
	
	private Mesh frameMesh;
	private Mesh innerMesh;
	private Mesh faceMesh;
	private Mesh theRestMesh;
	
	@Override
	public boolean isVanillaAdapter() {
		return false;
	}
	
	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		Object ext = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
		if(!(ext instanceof PackageBlockEntity.Style)) {
			//Shit's fucked! TODO handle this case better, if it even comes up at all
			return;
		}
		
		PackageBlockEntity.Style style = (PackageBlockEntity.Style) ext;
		
		//BlockState frameState = style.frameBlock.getDefaultState();
		//BlockState innerState = style.innerBlock.getDefaultState();
		Random boop = randomSupplier.get();
		Block[][] choices = new Block[][]{
			{Blocks.OAK_LOG, Blocks.OAK_PLANKS},
			{Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS},
			{Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS},
			{Blocks.ACACIA_LOG, Blocks.ACACIA_PLANKS},
			{Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS},
			{Blocks.JUNGLE_LOG, Blocks.JUNGLE_PLANKS},
		};
		Block[] bop = choices[boop.nextInt(choices.length)];
		
		BlockState frameState = bop[0].getDefaultState();
		BlockState innerState = bop[1].getDefaultState();
		
		BlockRenderManager mgr = MinecraftClient.getInstance().getBlockRenderManager();
		BakedModel frameModel = mgr.getModel(frameState);
		BakedModel innerModel = mgr.getModel(innerState);
		
		Sprite frameSprite = frameModel.getSprite();
		Sprite innerSprite = innerModel.getSprite();
		
		justFuckMyShitUp(context, frameSprite, innerSprite, DyeColor.values()[boop.nextInt(16)]);
	}
	
	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		//TODO read them from the nbt in the item stack instead of choosing literally randomly
		//TEMP TEMP TEMP TEMP TEMP TEMP i am literally just copy paste the block method for now lmao
		Random boop = randomSupplier.get();
		boop.setSeed(System.currentTimeMillis()); //LOL
		
		Block[][] choices = new Block[][]{
			{Blocks.OAK_LOG, Blocks.OAK_PLANKS},
			{Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS},
			{Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS},
			{Blocks.ACACIA_LOG, Blocks.ACACIA_PLANKS},
			{Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS},
			{Blocks.JUNGLE_LOG, Blocks.JUNGLE_PLANKS},
		};
		Block[] bop = choices[boop.nextInt(choices.length)];
		
		BlockState frameState = bop[0].getDefaultState();
		BlockState innerState = bop[1].getDefaultState();
		
		BlockRenderManager mgr = MinecraftClient.getInstance().getBlockRenderManager();
		BakedModel frameModel = mgr.getModel(frameState);
		BakedModel innerModel = mgr.getModel(innerState);
		
		Sprite frameSprite = frameModel.getSprite();
		Sprite innerSprite = innerModel.getSprite();
		
		justFuckMyShitUp(context, frameSprite, innerSprite, DyeColor.values()[boop.nextInt(16)]);
	}
	
	private void justFuckMyShitUp(RenderContext context, Sprite frameSprite, Sprite innerSprite, DyeColor faceColor) {
		//Phase 4: slap the textures on as late as possible.
		//Quad transformers are very helpful here.
		
		//Frame - need to texture it with the frame texture
		context.pushTransform(q -> {
			q.spriteBake(0, frameSprite, MutableQuadView.BAKE_NORMALIZED);
			return true;
		});
		context.meshConsumer().accept(frameMesh);
		context.popTransform();
		
		//Inner bit - need to texture it with the inner texture
		context.pushTransform(q -> {
			q.spriteBake(0, innerSprite, MutableQuadView.BAKE_NORMALIZED);
			return true;
		});
		context.meshConsumer().accept(innerMesh);
		context.popTransform();
		
		//Face - need to tint it the face's color
		int tint = 0xFF000000 | faceColor.getMaterialColor().color;
		context.pushTransform(q -> {
			q.spriteColor(0, tint, tint, tint, tint);
			return true;
		});
		context.meshConsumer().accept(faceMesh);
		context.popTransform();
		
		//And everything else.
		context.meshConsumer().accept(theRestMesh);
	}
}
