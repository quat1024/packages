package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
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
	public PackageBakedModel(BakedModel other, Mesh mesh) {
		this.wrapped = other;
		
		this.mesh = mesh;
	}
	
	private final Mesh mesh;
	
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
			//TODO handle this better?
			style = PackageStyle.FALLBACK;
		}
		
		emitFrame(context, style);
	}
	
	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		emitFrame(context, PackageStyle.fromItemStack(stack));
	}
	
	private void emitFrame(RenderContext context, PackageStyle style) {
		BlockRenderManager mgr = MinecraftClient.getInstance().getBlockRenderManager();
		
		Sprite frameSprite = mgr.getModel(style.frameBlock.getDefaultState()).getSprite();
		Sprite innerSprite = mgr.getModel(style.innerBlock.getDefaultState()).getSprite();
		DyeColor color = style.color;
		int tint = 0xFF000000 | color.getMaterialColor().color; //what a line of code
		
		context.pushTransform(q -> {
			switch(q.tag()) {
				case 1: q.spriteBake(0, frameSprite, MutableQuadView.BAKE_NORMALIZED); break;
				case 2: q.spriteBake(0, innerSprite, MutableQuadView.BAKE_NORMALIZED); break;
				case 3: q.spriteColor(0, tint, tint, tint, tint); break;
			}
			return true;
		});
		context.meshConsumer().accept(mesh);
		context.popTransform();
	}
}
