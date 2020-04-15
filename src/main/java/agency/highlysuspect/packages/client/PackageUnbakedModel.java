package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.resource.metadata.AnimationFrameResourceMetadata;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class PackageUnbakedModel implements UnbakedModel {
	public PackageUnbakedModel(UnbakedModel basePackage) {
		this.basePackage = basePackage;
	}
	
	private final UnbakedModel basePackage;
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		return basePackage.getModelDependencies();
	}
	
	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return basePackage.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences);
	}
	
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		return new PackageBakedModel(
			basePackage.bake(
				loader,
				//HORRENDOUS, AWFUL hack:
				//Make the JSON loading machinery only see one texture that spans (0, 0) -> (16, 16).
				//This means my U/V coordinates come through untransformed, and not cropped to a tiny portion of the block render map
				(id) -> FakeSprite.INSTANCE,
				rotationContainer,
				modelId
			)
		);
	}
	
	public static class FakeSprite extends Sprite {
		public static final FakeSprite INSTANCE = new FakeSprite();
		
		public FakeSprite() {
			super(null, new Sprite.Info(
				new Identifier(Packages.MODID, "fake_sprite"),
				16,
				16,
				new AnimationResourceMetadata(
					Collections.singletonList(new AnimationFrameResourceMetadata(0, -1)),
					16,
					16,
					1,
					false
				)
			), 0, 16, 16, 0, 0, IMAGE.get());
		}
		
		//This method, for some reason, returns 0.25 in MissingSprite.
		//That fucks up the UV generation in json quad emitter machinery, seems to zoom in by 25% or something, really weird.
		//Overriding this is the whole reason I make my own fake sprite and don't use MissingSprite.
		@Override
		public float getAnimationFrameDelta() {
			return 0;
		}
	}
	
	//copy paste from mojang missingno code so I don't have to accessor it
	//this is cursed
	private static final Lazy<NativeImage> IMAGE = new Lazy<>(() -> {
		NativeImage nativeImage = new NativeImage(16, 16, false);
		for(int k = 0; k < 16; ++k) {
			for(int l = 0; l < 16; ++l) {
				if (k < 4 ^ l < 4) { //wow a different pattern
					nativeImage.setPixelRgba(l, k, 0x00FF00); //wow a different color
				} else {
					nativeImage.setPixelRgba(l, k, 0x0088FF); //amazing
				}
			}
		}
		
		nativeImage.untrack();
		return nativeImage;
	});
}
