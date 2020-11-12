package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class PackageModel extends PackageModelBakery.DependentUnbakedModel {
	public static final Identifier PACKAGE_SPECIAL = new Identifier(PackagesInit.MODID, "special/package");
	public static final Identifier ITEM_SPECIAL = new Identifier(PackagesInit.MODID, "item/package");
	
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		PackageModelBakery bakery = new PackageModelBakery(loader, textureGetter, rotationContainer, modelId);
		return new Baked(bakery);
	}
	
	public static class Baked extends ForwardingBakedModel {
		public Baked(PackageModelBakery bakery) {
			this.wrapped = bakery.getBaseModel();
			this.bakery = bakery;
		}
		
		private final PackageModelBakery bakery;
		
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
				style = PackageStyle.EMPTY;
			}
			
			emitFrame(context, style);
		}
		
		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
			emitFrame(context, PackageStyle.fromItemStack(stack));
		}
		
		private void emitFrame(RenderContext context, PackageStyle style) {
			context.meshConsumer().accept(bakery.getOrBake(style));
		}
	}
}