package agency.highlysuspect.packages.client;

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
		
		PackageStyle style;
		if(ext instanceof PackageStyle) {
			style = (PackageStyle) ext;
		} else {
			//TODO handle this better?
			style = PackageStyle.FALLBACK;
		}
		
		boink(context, style);
	}
	
	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		boink(context, PackageStyle.fromItemStack(stack));
	}
	
	private void boink(RenderContext context, PackageStyle style) {
		BlockRenderManager mgr = MinecraftClient.getInstance().getBlockRenderManager();
		
		Sprite frameSprite = mgr.getModel(style.frameBlock.getDefaultState()).getSprite();
		Sprite innerSprite = mgr.getModel(style.innerBlock.getDefaultState()).getSprite();
		DyeColor color = style.color;
		
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
		int tint = 0xFF000000 | color.getMaterialColor().color; //what a line of code
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
