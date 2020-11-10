package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.client.ClientInit;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.fabricmc.fabric.impl.renderer.SpriteFinderImpl;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
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
		
		//Add funky quad transformer that applies the relevant frex material
		if(ClientInit.FREX_PROXY.isFrex()) {
			SpriteFinder spriteFinder = SpriteFinder.get(MinecraftClient.getInstance().getBakedModelManager().method_24153(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)); //oof
			
			context.pushTransform(q -> {
				if(q.tag() == 1 || q.tag() == 2) {
					BlockState state = (q.tag() == 1 ? style.frameBlock : style.innerBlock).getDefaultState();
					Sprite sprite = spriteFinder.find(q, 0);
					RenderMaterial mat = ClientInit.FREX_PROXY.getMaterial(state, sprite);
					q.material(mat);
				}
				return true;
			});
		}
		
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
		if(ClientInit.FREX_PROXY.isFrex()) context.popTransform();
	}
}
