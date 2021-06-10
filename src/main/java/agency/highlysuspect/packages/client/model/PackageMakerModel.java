package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.junk.PUtil;
import agency.highlysuspect.packages.junk.PackageStyle;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.BlockRenderView;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class PackageMakerModel extends PackageModelBakery.DependentUnbakedModel {
	public static final Identifier PACKAGE_MAKER_SPECIAL = new Identifier(PackagesInit.MODID, "special/package_maker");
	
	private static final Identifier PACKAGE_MAKER_BLOCKMODEL = new Identifier(PackagesInit.MODID, "block/package_maker");
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		return PUtil.concat(super.getModelDependencies(), PACKAGE_MAKER_BLOCKMODEL);
	}
	
	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return PUtil.concatCollections(
			super.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences),
			unbakedModelGetter.apply(PACKAGE_MAKER_BLOCKMODEL).getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences)
		);
	}
	
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		PackageModelBakery bakery = new PackageModelBakery(loader, textureGetter, rotationContainer, modelId);
		return new Baked(loader.getOrLoadModel(PACKAGE_MAKER_BLOCKMODEL).bake(loader, textureGetter, rotationContainer, modelId), bakery);
	}
	
	public static class Baked extends ForwardingBakedModel {
		public Baked(BakedModel wrapped, PackageModelBakery bakery) {
			this.wrapped = wrapped;
			this.bakery = bakery;
		}
		
		private final PackageModelBakery bakery;
		
		@Override
		public boolean isVanillaAdapter() {
			return false;
		}
		
		@Override
		public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			context.fallbackConsumer().accept(wrapped);
			
			Object ext = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
			
			PackageStyle style;
			if(ext instanceof PackageStyle) {
				style = (PackageStyle) ext;
			} else {
				return;
			}
			
			Vec3f scratch = new Vec3f(0, 0, 0);
			float size = 10/16f;
			
			context.pushTransform(q -> {
				for(int i = 0; i < 4; i++) {
					q.copyPos(i, scratch);
					scratch.scale(size);
					scratch.add((1 - size) / 2f, 9/16f, (1 - size) / 2f);
					q.pos(i, scratch);
					q.cullFace(null);
				}
				return true;
			});
			
			context.meshConsumer().accept(bakery.getOrBake(style));
			
			context.popTransform();
		}
	}
}
