package agency.highlysuspect.packages.platform.forge.client.model;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.client.model.BakedQuadPackageModelBakeryFactory;
import agency.highlysuspect.packages.client.model.PackageModelBakery;
import agency.highlysuspect.packages.junk.PackageStyle;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
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

public class ForgePackageModel implements IModelGeometry<ForgePackageModel> {
	protected static final ModelProperty<PackageStyle> STYLE_PROPERTY = new ModelProperty<>();
	
	protected final BakedQuadPackageModelBakeryFactory modelBakeryFactory = new BakedQuadPackageModelBakeryFactory(Packages.id("block/package"));
	
	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
		return new Baked(modelBakeryFactory.make(bakery, spriteGetter, modelTransform, modelLocation));
	}
	
	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return modelBakeryFactory.getMaterials(modelGetter, missingTextureErrors);
	}
	
	private static class Baked extends BakedModelWrapper<BakedModel> {
		public Baked(PackageModelBakery<List<BakedQuad>> factory) {
			super(factory.getBaseModel());
			this.factory = factory;
		}
		
		private final PackageModelBakery<List<BakedQuad>> factory;
		
		@Override
		public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
			return super.handlePerspective(cameraTransformType, poseStack);
		}
		
		@NotNull
		@Override
		public IModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull IModelData modelData) {
			if(level.getBlockEntity(pos) instanceof PackageBlockEntity be) {
				//Do not fall for the forbidden fruit of IModelData#setModel. It does nothing
				return new ModelDataMap.Builder().withInitial(STYLE_PROPERTY, be.getStyle()).build();
			}
			return modelData;
		}
		
		@NotNull
		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
			PackageStyle style = extraData.hasProperty(STYLE_PROPERTY) ? extraData.getData(STYLE_PROPERTY) : PackageStyle.ERROR_LOL;
			if(style == null) style = PackageStyle.ERROR_LOL; //shouldn't happen, hasProperty checked, u never know though
			
			return factory.bake(style, style.color(), style.frameBlock(), style.innerBlock());
		}
	}
	
	public static class Loader implements IModelLoader<ForgePackageModel> {
		public static final ResourceLocation ID = Packages.id("forge_package_model_loader");
		
		@Override
		public ForgePackageModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			return new ForgePackageModel();
		}
		
		@Override
		public void onResourceManagerReload(ResourceManager p_10758_) {
			//Don't care
		}
	}
}
