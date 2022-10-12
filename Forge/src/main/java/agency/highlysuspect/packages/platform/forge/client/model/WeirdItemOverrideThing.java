package agency.highlysuspect.packages.platform.forge.client.model;

import agency.highlysuspect.packages.client.PackageModelBakery;
import agency.highlysuspect.packages.client.PackagesClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
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
				//uhh it should be good to share the factory instance.... hm
				List<BakedQuad> rehreh = factory.bake(cacheKey, faceColor, frameBlock, innerBlock);
				return new BakedModelWrapper<>(getBaseModel()) {
					@Override
					public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
						return rehreh;
					}
					
					@Override
					public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
						return rehreh;
					}
					
					@Override
					public ItemOverrides getOverrides() {
						return ItemOverrides.EMPTY;
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
