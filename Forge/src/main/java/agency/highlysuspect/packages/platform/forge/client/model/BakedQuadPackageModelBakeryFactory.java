package agency.highlysuspect.packages.platform.forge.client.model;

import agency.highlysuspect.packages.client.PackageModelBakery;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BakedQuadPackageModelBakeryFactory extends PackageModelBakery.Factory<List<BakedQuad>> {
	public BakedQuadPackageModelBakeryFactory(ResourceLocation blockModelId) {
		super(blockModelId);
	}
	
	@Override
	public PackageModelBakery<List<BakedQuad>> make(BakedModel baseModel, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
		return new BakedQuadPackageModelBakery(baseModel, specialFrameSprite, specialInnerSprite);
	}
}
