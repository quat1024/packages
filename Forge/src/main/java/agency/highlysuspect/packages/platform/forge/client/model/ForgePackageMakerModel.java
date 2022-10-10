package agency.highlysuspect.packages.platform.forge.client.model;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.client.PackageModelBakery;
import agency.highlysuspect.packages.junk.PackageMakerStyle;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class ForgePackageMakerModel implements IModelGeometry<ForgePackageMakerModel> {
	protected static final ModelProperty<PackageMakerStyle> MAKER_STYLE_PROPERTY = new ModelProperty<>();
	
	protected final PackageModelBakery.Factory<List<BakedQuad>> modelBakeryFactory = new PackageModelBakery.Factory<>(Packages.id("block/package_maker")) {
		@Override
		public PackageModelBakery<List<BakedQuad>> make(BakedModel baseModel, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
			return new BakedQuadPackageModelBakery(baseModel, specialFrameSprite, specialInnerSprite);
		}
	};
	
	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return modelBakeryFactory.getMaterials(modelGetter, missingTextureErrors);
	}
	
	//Model dependencies are notably missing from this class - this is because Forge forgot to add them to IModelGeometry.
	//see ForgeClientPlatformSupport#setupCustomModelLoaders.
	
	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
		return new Baked(modelBakeryFactory.make(bakery, spriteGetter, modelTransform, modelLocation));
	}
	
	private static class Baked extends BakedModelWrapper<BakedModel> {
		public Baked(PackageModelBakery<List<BakedQuad>> factory) {
			super(factory.getBaseModel());
			this.factory = factory;
			this.itemOverrideThing = new WeirdItemOverrideThing(factory) {
				@Nullable
				@Override
				public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity player, int idk) {
					return itemCache.bake((PackageMakerStyle) null);
				}
			};
		}
		
		private final PackageModelBakery<List<BakedQuad>> factory;
		private final WeirdItemOverrideThing itemOverrideThing;
		
		@Override
		public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
			return super.handlePerspective(cameraTransformType, poseStack);
		}
		
		@NotNull
		@Override
		public IModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull IModelData modelData) {
			if(level.getBlockEntity(pos) instanceof PackageMakerBlockEntity be) {
				//Do not fall for the forbidden fruit of IModelData#setModel. It does nothing
				return new ModelDataMap.Builder().withInitial(MAKER_STYLE_PROPERTY, be.getStyle()).build();
			}
			return modelData;
		}
		
		@NotNull
		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
			PackageMakerStyle style = extraData.hasProperty(MAKER_STYLE_PROPERTY) ? extraData.getData(MAKER_STYLE_PROPERTY) : PackageMakerStyle.NIL;
			
			//This shoudn't happen because I checked hasProperty, but forge javadocs specifically note that
			//hasProperty does not imply getData will return nonnull. Ok, sure.
			if(style == null) style = PackageMakerStyle.NIL;
			
			return factory.bake(style);
		}
		
		@Override
		public ItemOverrides getOverrides() {
			return itemOverrideThing;
		}
	}
	
	public static class Loader implements IModelLoader<ForgePackageMakerModel> {
		public static final ResourceLocation ID = Packages.id("forge_package_maker_model_loader");
		
		@Override
		public ForgePackageMakerModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			return new ForgePackageMakerModel();
		}
		
		@Override
		public void onResourceManagerReload(ResourceManager mgr) {
			//The old ForgePackageMakerModel and all caches derived from it should safely get garbage collected.
		}
	}
}