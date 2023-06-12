package agency.highlysuspect.packages.platform.forge.client.model;

import agency.highlysuspect.packages.client.PackageModelBakery;
import agency.highlysuspect.packages.client.PackagesClient;
import agency.highlysuspect.packages.client.PropsClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class WeirdItemOverrideThing extends ItemOverrides {
	public WeirdItemOverrideThing(PackageModelBakery<List<BakedQuad>> factory) {
		PackageModelBakery<BakedModel> bakeybake = new PackageModelBakery<>() {
			@Override
			public BakedModel getBaseModel() {
				return factory.getBaseModel();
			}
			
			@Override
			public BakedModel bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
				return new EpicModel(getBaseModel(), factory.bake(cacheKey, faceColor, frameBlock, innerBlock));
			}
		};
		
		if(PackagesClient.instance.config.get(PropsClient.CACHE_MESHES)) bakeybake = new PackageModelBakery.Caching<>(bakeybake);
		this.itemModelMaker = bakeybake;
	}
	
	protected final PackageModelBakery<BakedModel> itemModelMaker;
	
	//This is the magic method from ItemOverrides. Implementations should draw models out of itemModelMaker.
	@Nullable
	@Override
	public abstract BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity player, int idk);
	
	private static class EpicModel extends BakedModelWrapper<BakedModel> {
		public EpicModel(BakedModel originalModel, List<BakedQuad> quads) {
			super(originalModel);
			this.quads = quads;
		}
		
		private final List<BakedQuad> quads;
		private final List<BakedModel> thisButList = List.of(this);
		
		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
			return quads;
		}
		
		@Override
		public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
			return quads;
		}
		
		@Override
		public ItemOverrides getOverrides() {
			return ItemOverrides.EMPTY;
		}
		
		@Override
		public BakedModel applyTransform(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack, boolean applyLeftHandTransform) {
			//Have to override this or Forge throws away all your work making a nontrivial BakedModelWrapper.
			super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
			return this;
		}
		
		@Override
		public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
			//This one too.
			return thisButList;
		}
	}
}
