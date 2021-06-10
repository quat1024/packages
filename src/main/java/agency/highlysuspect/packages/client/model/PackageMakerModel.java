package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.junk.PUtil;
import agency.highlysuspect.packages.junk.PackageMakerRenderAttachment;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class PackageMakerModel implements UnbakedModel {
	public static final Identifier PACKAGE_MAKER_SPECIAL = new Identifier(PackagesInit.MODID, "special/package_maker");
	public static final Identifier ITEM_SPECIAL = new Identifier(PackagesInit.MODID, "item/package_maker");
	private static final PackageModelBakery.Spec modelBakerySpec = new PackageModelBakery.Spec(new Identifier(PackagesInit.MODID, "block/package_maker"));
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		return modelBakerySpec.modelDependencies();
	}
	
	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return modelBakerySpec.textureDependencies(unbakedModelGetter, unresolvedTextureReferences);
	}
	
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		return new Baked(modelBakerySpec.make(loader, textureGetter, rotationContainer, modelId));
	}
	
	public static class Baked extends ForwardingBakedModel {
		public Baked(PackageModelBakery bakery) {
			this.wrapped = bakery.baseModel();
			this.bakery = bakery;
		}
		
		private final PackageModelBakery bakery;
		
		@Override
		public boolean isVanillaAdapter() {
			return false;
		}
		
		@Override
		public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
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
