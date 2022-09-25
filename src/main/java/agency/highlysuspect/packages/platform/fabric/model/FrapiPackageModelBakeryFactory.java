package agency.highlysuspect.packages.platform.fabric.model;

import agency.highlysuspect.packages.client.PackageModelBakery;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public class FrapiPackageModelBakeryFactory extends PackageModelBakery.Factory<Mesh> {
	public FrapiPackageModelBakeryFactory(ResourceLocation blockModelId) {
		super(blockModelId);
	}
	
	@Override
	public PackageModelBakery<Mesh> make(BakedModel baseModel, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
		return new FrapiPackageModelBakery(baseModel, specialFrameSprite, specialInnerSprite);
	}
}
