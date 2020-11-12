package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import com.google.common.base.Preconditions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class PackageUnbakedModel extends DelegatingUnbakedModel {
	public PackageUnbakedModel(UnbakedModel basePackage) {
		super(basePackage);
	}
	
	public PackageUnbakedModel(ModelProviderContext ctx) {
		this(ctx.loadModel(BLOCKMODEL_PATH));
	}
	
	public static final Identifier PACKAGE_SPECIAL = new Identifier(PackagesInit.MODID, "special/package");
	//TODO, find out why making this a regular item model and setting "parent": "pacakges:special/package" seems to break all resource loading
	public static final Identifier ITEM_SPECIAL = new Identifier(PackagesInit.MODID, "item/package");
	
	public static final Identifier BLOCKMODEL_PATH = new Identifier(PackagesInit.MODID, "block/package");
	
	private static final Identifier SPECIAL_FRAME = new Identifier(PackagesInit.MODID, "special/frame");
	private static final Identifier SPECIAL_INNER = new Identifier(PackagesInit.MODID, "special/inner");
	
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		BakedModel fromJson = super.bake(loader,	textureGetter, rotationContainer, modelId);
		Preconditions.checkNotNull(fromJson, "null model when loading the barrel model?!");
		
		//When canvas(JMX) is present, mixing a getSprite() onto BakedQuad and calling it on a quad from BakedModel#getQuads implemented by JMX,
		//you just get the particle texture.
		//My model depends on being able to tell textures apart, so, I compare texture coordinates instead.
		Sprite specialFrameSprite = textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, SPECIAL_FRAME));
		Sprite specialInnerSprite = textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, SPECIAL_INNER));
		
		return new PackageBakedModel(fromJson, specialFrameSprite, specialInnerSprite);
	}
}