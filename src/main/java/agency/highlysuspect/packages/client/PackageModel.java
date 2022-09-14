package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Init;
import agency.highlysuspect.packages.junk.PackageStyle;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class PackageModel implements UnbakedModel {
	public static final ResourceLocation PACKAGE_SPECIAL = Init.id("special/package");
	public static final ResourceLocation ITEM_SPECIAL = Init.id("item/package");
	private static final PackageModelBakery.Spec modelBakerySpec = new PackageModelBakery.Spec(Init.id("block/package"));
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return modelBakerySpec.modelDependencies();
	}
	
	@Override
	public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return modelBakerySpec.textureDependencies(unbakedModelGetter, unresolvedTextureReferences);
	}
	
	@Override
	public BakedModel bake(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId) {
		return new Baked(modelBakerySpec.make(loader, textureGetter, rotationContainer, modelId));
	}
	
	public static class Baked extends ForwardingBakedModel {
		public Baked(PackageModelBakery bakery) {
			this.wrapped = bakery.baseModel;
			this.bakery = bakery;
		}
		
		public final PackageModelBakery bakery;
		
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