package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
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
	
	private void justFuckMyShitUp(RenderContext context, Sprite frame, Sprite inner, DyeColor faceColor) {
		context.pushTransform(quad -> {
			int colorIndex = quad.colorIndex();
			if(colorIndex == 1) {
				//Fill in the actual tint index for the front of the package
				int color = 0xFF000000 | faceColor.getMaterialColor().color;
				quad.spriteColor(0, color, color, color, color);
				
				//And finally, phase 3 of my evil plan.
				//Read off the sentinel values and swap in the correct textures just in time.
			} else if(colorIndex == 100) {
				//Sentinel colorIndex for the frame
				quad.spriteBake(0, frame, MutableQuadView.BAKE_NORMALIZED);
			} else if(colorIndex == 101) {
				//Sentinel colorIndex for the inner bit
				quad.spriteBake(0, inner, MutableQuadView.BAKE_NORMALIZED);
			}
			return true;
		});
		
		context.fallbackConsumer().accept(wrapped);
		
		context.popTransform();
	}
}
