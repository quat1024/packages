package agency.highlysuspect.packages.platform.fabric.model;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.client.PackageModelBakery;
import agency.highlysuspect.packages.junk.PackageMakerRenderAttachment;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
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

public class FrapiPackageMakerModel implements UnbakedModel {
	private static final FrapiPackageModelBakeryFactory MODEL_BAKERY_FACTORY = new FrapiPackageModelBakeryFactory(Packages.id("block/package_maker"));
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return MODEL_BAKERY_FACTORY.getDependencies();
	}
	
	@Override
	public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return MODEL_BAKERY_FACTORY.getMaterials(unbakedModelGetter, unresolvedTextureReferences);
	}
	
	@Override
	public BakedModel bake(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId) {
		return new Baked(MODEL_BAKERY_FACTORY.make(loader, textureGetter, rotationContainer, modelId));
	}
	
	public static class Baked extends ForwardingBakedModel {
		public Baked(PackageModelBakery<Mesh> bakery) {
			this.wrapped = bakery.getBaseModel();
			this.bakery = bakery;
		}
		
		public final PackageModelBakery<Mesh> bakery;
		
		@Override
		public boolean isVanillaAdapter() {
			return false;
		}
		
		@Override
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			if(((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos) instanceof PackageMakerRenderAttachment attachment) {
				context.meshConsumer().accept(bakery.bake(attachment, attachment.color(), attachment.frameBlock(), attachment.innerBlock()));
			}
		}
		
		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
			//filters out all the special quads
			context.meshConsumer().accept(bakery.bake(null, null, null, null));
		}
	}
}
