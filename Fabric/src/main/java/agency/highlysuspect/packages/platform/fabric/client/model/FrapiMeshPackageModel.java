package agency.highlysuspect.packages.platform.fabric.client.model;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.client.model.AbstractPackageModel;
import agency.highlysuspect.packages.client.model.PackageModelBakery;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;
import java.util.function.Supplier;

public class FrapiMeshPackageModel extends AbstractPackageModel<Mesh> {
	@Override
	protected PackageModelBakery.Factory<Mesh> makeFactory() {
		return new FrapiMeshModelBakeryFactory(Packages.id("block/package"));
	}
	
	@Override
	protected BakedModel toBakedModel(PackageModelBakery<Mesh> factoryResult) {
		return new Baked(factoryResult);
	}
	
	private static class Baked extends ForwardingBakedModel {
		public Baked(PackageModelBakery<Mesh> bakery) {
			this.wrapped = bakery.getBaseModel();
			this.bakery = bakery;
		}
		
		private final PackageModelBakery<Mesh> bakery;
		
		@Override
		public boolean isVanillaAdapter() {
			return false;
		}
		
		@Override
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			if(((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos) instanceof PackageStyle style) {
				context.meshConsumer().accept(bakery.bake(style, style.color(), style.frameBlock(), style.innerBlock()));
			}
		}
		
		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
			PackageStyle style = PackageStyle.fromItemStack(stack);
			context.meshConsumer().accept(bakery.bake(style, style.color(), style.frameBlock(), style.innerBlock()));
		}
	}
}