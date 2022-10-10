package agency.highlysuspect.packages.platform.forge.client.model;

import agency.highlysuspect.packages.client.PackageModelBakery;
import agency.highlysuspect.packages.client.PackagesClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public abstract class WeirdItemOverrideThing extends ItemOverrides {
	public WeirdItemOverrideThing(PackageModelBakery<List<BakedQuad>> factory) {
		PackageModelBakery<BakedModel> bakeybake = new PackageModelBakery<>() {
			@Override
			public BakedModel getBaseModel() {
				return factory.getBaseModel();
			}
			
			@Override
			public BakedModel bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
				//uhh it should be good to share the factory instance.... hm
				List<BakedQuad> rehreh = factory.bake(cacheKey, faceColor, frameBlock, innerBlock);
				return new BakedModelWrapper<>(getBaseModel()) {
					@Override
					public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
						return rehreh;
					}
					
					@NotNull
					@Override
					public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
						return rehreh;
					}
					
					@Override
					public ItemOverrides getOverrides() {
						return ItemOverrides.EMPTY;
					}
					
					@Override
					public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
						super.handlePerspective(cameraTransformType, poseStack);
						return this; //Or else Forge just throws away all the work I put in to making a nontrivial baked model. Thanks!
					}
				};
			}
		};
		
		if(PackagesClient.instance.config.cacheMeshes) bakeybake = new PackageModelBakery.Caching<>(bakeybake);
		this.itemCache = bakeybake;
	}
	
	protected final PackageModelBakery<BakedModel> itemCache;
	
	@Nullable
	@Override
	public abstract BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity player, int idk);
}
