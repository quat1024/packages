package agency.highlysuspect.packages.platform.fabric.client.model;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.client.PackageModelBakery;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is basically identical to FrapiMeshPackageMakerModel.
 * It uses PackageStyle instead of PackageMakerStyle, and uses a different style of choosing the model from the item stack.
 */
public class FrapiMeshPackageModel implements UnbakedModel {
	public static final ResourceLocation BLOCK_SPECIAL = Packages.id("special/package");
	public static final ResourceLocation ITEM_SPECIAL = Packages.id("item/package");
	
	private final PackageModelBakery.Factory<Mesh> factory = new PackageModelBakery.Factory<>(Packages.id("block/package"), FrapiMeshModelBakery::new);
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return List.of(factory.blockModelId());
	}
	
	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
		function.apply(factory.blockModelId()).resolveParents(function);
	}
	
	@Nullable
	@Override
	public BakedModel bake(ModelBaker loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId) {
		return new Baked(factory.make(loader, textureGetter, rotationContainer, modelId));
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
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
			if(((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos) instanceof PackageStyle style) {
				context.meshConsumer().accept(bakery.bake(style));
			}
		}
		
		@Override
		public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
			context.meshConsumer().accept(bakery.bake(PackageStyle.fromItemStack(stack)));
		}
	}
}