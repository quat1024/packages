package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.client.compat.frex.FrexCompat;
import agency.highlysuspect.packages.client.compat.frex.FrexProxy;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
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

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class PackageBakedModel extends ForwardingBakedModel {
	public PackageBakedModel(BakedModel other, Mesh baseMesh) {
		this.wrapped = other;
		this.baseMesh = baseMesh;
	}
	
	private final Mesh baseMesh;
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
			BlockRenderManager mgr = MinecraftClient.getInstance().getBlockRenderManager();
			Sprite frameSprite = mgr.getModel(style.frameBlock.getDefaultState()).getSprite();
			Sprite innerSprite = mgr.getModel(style.innerBlock.getDefaultState()).getSprite();
			DyeColor color = style.color;
			int tint = 0xFF000000 | color.getMaterialColor().color; //what a line of code
			
			Renderer renderer = RendererAccess.INSTANCE.getRenderer();
			assert renderer != null;
			MeshBuilder meshBuilder = renderer.meshBuilder();
			QuadEmitter emitter = meshBuilder.getEmitter();
			
			baseMesh.forEach(quad -> {
				quad.copyTo(emitter);
				
				switch(emitter.tag()) {
					case 1: emitter.spriteBake(0, frameSprite, MutableQuadView.BAKE_NORMALIZED); break;
					case 2: emitter.spriteBake(0, innerSprite, MutableQuadView.BAKE_NORMALIZED); break;
					case 3: emitter.spriteColor(0, tint, tint, tint, tint); break;
				}
				
				//TODO this isn't the best way to do this, mainly b/c it does things like re-lookup the spritefinder every time
				// It's not that big a deal though, especially because it gets cached
				FrexCompat.PROXY.fancifyPackageQuad(emitter, style);
				
				emitter.emit();
			});
			
			return meshBuilder.build();
		}));
	}
}
