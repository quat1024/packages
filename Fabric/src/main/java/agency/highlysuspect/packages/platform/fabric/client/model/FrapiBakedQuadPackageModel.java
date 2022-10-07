package agency.highlysuspect.packages.platform.fabric.client.model;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.client.PackagesClient;
import agency.highlysuspect.packages.client.model.AbstractPackageModel;
import agency.highlysuspect.packages.client.model.BakedQuadPackageModelBakeryFactory;
import agency.highlysuspect.packages.client.model.PackageModelBakery;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class FrapiBakedQuadPackageModel extends AbstractPackageModel<List<BakedQuad>> {
	@Override
	protected PackageModelBakery.Factory<List<BakedQuad>> makeFactory() {
		return new BakedQuadPackageModelBakeryFactory(Packages.id("block/package"));
	}
	
	@Override
	protected BakedModel toBakedModel(PackageModelBakery<List<BakedQuad>> factoryResult) {
		//performance is when you add more layers of indirection :TM:
		PackageModelBakery<BakedModel> b = new PackageModelBakery.BakedQuadsToBakedModel(factoryResult);
		if(PackagesClient.instance.config.cacheMeshes) b = new PackageModelBakery.Caching<>(b);
		return new Baked(b);
	}
	
	private static class Baked extends ForwardingBakedModel {
		public Baked(PackageModelBakery<BakedModel> bakery) {
			this.wrapped = bakery.getBaseModel();
			this.bakery = bakery;
		}
		
		private final PackageModelBakery<BakedModel> bakery;
		
		@Override
		public boolean isVanillaAdapter() {
			return false;
		}
		
		@Override
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			if(((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos) instanceof PackageStyle style) {
				context.fallbackConsumer().accept(bakery.bake(style, style.color(), style.frameBlock(), style.innerBlock()));
			}
		}
		
		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
			PackageStyle style = PackageStyle.fromItemStack(stack);
			context.fallbackConsumer().accept(bakery.bake(style, style.color(), style.frameBlock(), style.innerBlock()));
		}
	}
}
