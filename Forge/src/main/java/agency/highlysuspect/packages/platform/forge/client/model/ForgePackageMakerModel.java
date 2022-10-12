package agency.highlysuspect.packages.platform.forge.client.model;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.client.PackageModelBakery;
import agency.highlysuspect.packages.junk.PackageMakerStyle;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ForgePackageMakerModel implements IUnbakedGeometry<ForgePackageMakerModel> {
	protected static final ModelProperty<PackageMakerStyle> STYLE_PROPERTY = new ModelProperty<>();
	
	protected final PackageModelBakery.Factory<List<BakedQuad>> modelBakeryFactory = new PackageModelBakery.Factory<>(Packages.id("block/package_maker")) {
		@Override
		public PackageModelBakery<List<BakedQuad>> make(BakedModel baseModel, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
			return new BakedQuadPackageModelBakery(baseModel, specialFrameSprite, specialInnerSprite);
		}
	};
	
	@Override
	public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return modelBakeryFactory.getMaterials(modelGetter, missingTextureErrors);
	}
	
	//Model dependencies are notably missing from this class - this is because Forge forgot to add them to IModelGeometry.
	//see ForgeClientPlatformSupport#setupCustomModelLoaders.
	
	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
		return new Baked(modelBakeryFactory.make(bakery, spriteGetter, modelState, modelLocation));
	}
	
	private static class Baked extends BakedModelWrapper<BakedModel> {
		public Baked(PackageModelBakery<List<BakedQuad>> factory) {
			super(factory.getBaseModel());
			this.factory = factory;
			this.itemOverrideThing = new WeirdItemOverrideThing(factory) {
				@Nullable
				@Override
				public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity player, int idk) {
					return itemModelMaker.bake((PackageMakerStyle) null);
				}
			};
		}
		
		private final PackageModelBakery<List<BakedQuad>> factory;
		private final WeirdItemOverrideThing itemOverrideThing;
		
		@NotNull
		@Override
		public ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
			if(level.getBlockEntity(pos) instanceof PackageMakerBlockEntity be) {
				return modelData.derive().with(STYLE_PROPERTY, be.getStyle()).build();
			}
			return modelData;
		}
		
		@Override
		public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
			PackageMakerStyle style = extraData.get(STYLE_PROPERTY);
			if(style == null) style = PackageMakerStyle.NIL;
			return factory.bake(style);
		}
		
		@Override
		public ItemOverrides getOverrides() {
			return itemOverrideThing;
		}
	}
	
	public static class Loader implements IGeometryLoader<ForgePackageMakerModel> {
		public static final ResourceLocation ID = Packages.id("forge_package_maker_model_loader");
		
		@Override
		public ForgePackageMakerModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
			return new ForgePackageMakerModel();
		}
	}
}
