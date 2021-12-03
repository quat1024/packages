package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.junk.PUtil;
import agency.highlysuspect.packages.junk.PackageMakerRenderAttachment;
import com.google.common.collect.ImmutableList;
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

public class PackageMakerModel implements UnbakedModel {
	public static final ResourceLocation PACKAGE_MAKER_SPECIAL = new ResourceLocation(PackagesInit.MODID, "special/package_maker");
	public static final ResourceLocation ITEM_SPECIAL = new ResourceLocation(PackagesInit.MODID, "item/package_maker");
	private static final PackageModelBakery.Spec modelBakerySpec = new PackageModelBakery.Spec(new ResourceLocation(PackagesInit.MODID, "block/package_maker"));
	
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
			this.wrapped = bakery.baseModel();
			this.bakery = bakery;
		}
		
		public final PackageModelBakery bakery;
		
		@Override
		public boolean isVanillaAdapter() {
			return false;
		}
		
		@Override
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			if(((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos) instanceof PackageMakerRenderAttachment attachment) {
				context.meshConsumer().accept(bakery.bake(attachment.color(), attachment.frameBlock(), attachment.innerBlock()));
			}
		}
		
		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
			//filters out all the special quads
			context.meshConsumer().accept(bakery.bake(null, null, null));
		}
	}
}
