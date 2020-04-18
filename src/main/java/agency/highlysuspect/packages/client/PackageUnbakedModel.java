package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.junk.BakedQuadExt;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class PackageUnbakedModel implements UnbakedModel {
	public PackageUnbakedModel(UnbakedModel basePackage) {
		this.basePackage = basePackage;
	}
	
	private final UnbakedModel basePackage;
	
	private static final Identifier SPECIAL_FRAME = new Identifier(Packages.MODID, "special/frame");
	private static final Identifier SPECIAL_INNER = new Identifier(Packages.MODID, "special/inner");
	
	private static final Direction[] DIRECTIONS_AND_NULL = new Direction[]{
		Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, null
	};
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		return basePackage.getModelDependencies();
	}
	
	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return basePackage.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences)
			.stream()
			.filter(sid -> !sid.getTextureId().equals(SPECIAL_FRAME) && !sid.getTextureId().equals(SPECIAL_INNER))
			.collect(Collectors.toList());
	}
	
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		//I have a terrible plan, and it takes three steps.
		
		//Phase 1.
		//Load through vanilla JSON model loading machinery, but
		//whenever a "placeholder" texture is requested intentionally
		//fuck up its texture atlas coordinates. I'm trusting that
		//the basePackage unbaked model is from vanilla but if some
		//other mod is wrapping it for some reason it should be fine.
		BakedModel fromJson = basePackage.bake(
			loader,
			//When you ask for a "special" texture, give it one that spans (0, 0) -> (1, 1).
			//The fake sprite appears to takes up the entire texture atlas, so when the JSON loader is choosing
			//the texture coordinates to apply to the BakedQuad, numerically, nothing happens.
			
			//I also make sure to note which quad this sprite came from.
			(id) -> {
				Identifier textureId = id.getTextureId();
				if(SPECIAL_FRAME.equals(textureId)) {
					return new FakeSprite(SPECIAL_FRAME);
				} else if(SPECIAL_INNER.equals(textureId)) {
					return new FakeSprite(SPECIAL_INNER);
				} else return textureGetter.apply(id);
			},
			rotationContainer,
			modelId
		);
		
		assert fromJson != null;
		
		//Phase 2.
		//Now that I'm done tricking the JSON model loader, get the fake sprites out ASAP.
		//They're super cursed and brittle and will probably break other mods that do things with models.
		//Instead, replace them with a valid sprite, but with a sentinel colorIndex value.
		Random probablyShouldntPassNullHere = new Random();
		for(Direction d : DIRECTIONS_AND_NULL) {
			for(BakedQuad quad : fromJson.getQuads(PBlocks.PACKAGE.getDefaultState(), d, probablyShouldntPassNullHere)) {
				Sprite sprite = ((BakedQuadExt) quad).pkgs$getSprite();
				if (sprite instanceof FakeSprite) {
					//Get rid of the FakeSprite.
					Sprite dummySprite = textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, MissingSprite.getMissingSpriteId()));
					((BakedQuadExt) quad).pkgs$setSprite(dummySprite);
					
					//And choose a sentinel tintIndex.
					Identifier type = sprite.getId();
					
					if (type.equals(SPECIAL_FRAME)) {
						((BakedQuadExt) quad).pkgs$setColorIndex(100);
					} else if (type.equals(SPECIAL_INNER)) {
						((BakedQuadExt) quad).pkgs$setColorIndex(101);
					}
				}
			}
		}
		
		//Phase 3 happens in the baked model.
		return new PackageBakedModel(fromJson);
	}
}

/* ************************************************************
 
 
 TODO TODO TODO big comment so i dont forget (This isnt crusty shut up)
 
 - delete this whole "sentinel value" nonsense with the color indices, what was I thinking lmao
 - probably delete the whole color index usage altogether...
 - INSTEAD split into 4 sets of quads...
   - frame quads
   - inner part quads
   - the recolored quad on the front?
     - can I even use the actual tintIndex system for that
   - "the rest" (anything people add themself as part of the model)
 - render those in four passes in the baked model (pushTransform, render a layer, pop)
 
 */