package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.junk.BakedQuadExt;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class PackageUnbakedModel implements UnbakedModel {
	public PackageUnbakedModel(UnbakedModel basePackage) {
		this.basePackage = basePackage;
	}
	
	private final UnbakedModel basePackage;
	
	private static final Identifier SPECIAL_FRAME = new Identifier(PackagesInit.MODID, "special/frame");
	private static final Identifier SPECIAL_INNER = new Identifier(PackagesInit.MODID, "special/inner");
	
	private static final Direction[] DIRECTIONS_AND_NULL = new Direction[]{
		Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, null
	};
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		return basePackage.getModelDependencies();
	}
	
	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		//Remove references to the special textures, so the game doesn't try to load them
		return basePackage.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences)
			.stream()
			.filter(sid -> !sid.getTextureId().equals(SPECIAL_FRAME) && !sid.getTextureId().equals(SPECIAL_INNER))
			.collect(Collectors.toList());
	}
	
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		//I have a terrible plan, and it takes four steps.
		
		//Phase 1.
		//Load through vanilla JSON model loading machinery, but whenever a "placeholder" texture is requested intentionally
		//fuck up its texture atlas coordinates. I'm trusting that the basePackage unbaked model is from vanilla, but if some
		//other mod is wrapping it for some reason, should be alright? Just assuming my dumb fakesprite things don't break
		//mods too much.
		BakedModel fromJson = basePackage.bake(
			loader,
			//When you ask for a "special" texture, give it one that spans (0, 0) -> (1, 1).
			//The fake sprite appears to takes up the entire texture atlas, so when the JSON loader is choosing
			//the texture coordinates to apply to the BakedQuad, numerically, nothing happens.
			
			//I also make sure to note which quad this sprite came from.
			(id) -> {
				Identifier textureId = id.getTextureId();
				if (SPECIAL_FRAME.equals(textureId)) {
					return new FakeSprite(SPECIAL_FRAME);
				} else if (SPECIAL_INNER.equals(textureId)) {
					return new FakeSprite(SPECIAL_INNER);
				} else return textureGetter.apply(id);
			},
			rotationContainer, modelId
		);
		
		Preconditions.checkNotNull(fromJson, "null model when loading the barrel model?!");
		
		//Phase 2.
		//Sort the quads into four buckets:
		// * quads belonging to the frame
		// * quads belonging to the inner bit
		// * quads belonging to the recolored face on the front (anything with tintindex 1)
		// * everything else
		//For cases 1 and 2, disambiguate them from the FakeSprite's ID.
		//For case 3, I check the tint index.
		//Everything else goes in the 4th bucket.
		
		//cullFace -> quads. Note that cullFace can be null, so an EnumMap can't do.
		Map<Direction, List<BakedQuad>> frameQuads = new HashMap<>();
		Map<Direction, List<BakedQuad>> innerQuads = new HashMap<>();
		Map<Direction, List<BakedQuad>> faceQuads = new HashMap<>();
		Map<Direction, List<BakedQuad>> theRest = new HashMap<>();
		
		Random probablyShouldntPassNullHere = new Random();
		
		for (Direction d : DIRECTIONS_AND_NULL) {
			for (BakedQuad quad : fromJson.getQuads(PBlocks.PACKAGE.getDefaultState(), d, probablyShouldntPassNullHere)) {
				Sprite sprite = ((BakedQuadExt) quad).pkgs$getSprite();
				
				if (sprite instanceof FakeSprite) {
					Identifier type = sprite.getId();
					
					if (type.equals(SPECIAL_FRAME)) {
						frameQuads.computeIfAbsent(d, x -> new ArrayList<>()).add(quad);
					} else if (type.equals(SPECIAL_INNER)) {
						innerQuads.computeIfAbsent(d, x -> new ArrayList<>()).add(quad);
					} else {
						PackagesInit.LOGGER.error("Found a FakeSprite with ID " + type.toString() + " but I'm not sure what to do with that");
					}
					
					continue;
				}
				
				if (quad.getColorIndex() == 1) {
					faceQuads.computeIfAbsent(d, x -> new ArrayList<>()).add(quad);
					continue;
				}
				
				theRest.computeIfAbsent(d, x -> new ArrayList<>()).add(quad);
			}
		}
		
		//Phase 3: cook these these into meshes, since I don't need the raw quads anymore
		//This also, nicely, disposes of all references to the FakeSprites. Which is great since I don't want other mods seeing those ever lmao
		MeshBuilder mb = RendererAccess.INSTANCE.getRenderer().meshBuilder();
		Mesh frameMesh = meshFromMapThingie(mb, frameQuads);
		Mesh innerMesh = meshFromMapThingie(mb, innerQuads);
		Mesh faceMesh = meshFromMapThingie(mb, faceQuads);
		Mesh theRestMesh = meshFromMapThingie(mb, theRest);
		
		//Phase 4 happens in the baked model
		return new PackageBakedModel(fromJson, frameMesh, innerMesh, faceMesh, theRestMesh);
	}
	
	private static Mesh meshFromMapThingie(MeshBuilder mb, Map<Direction, List<BakedQuad>> quadMap) {
		QuadEmitter emitter = mb.getEmitter();
		quadMap.forEach((direction, quads) -> quads.forEach(q -> {
			emitter.fromVanilla(q.getVertexData(), 0, false);
			emitter.cullFace(direction); //It's ok to pass null here
			emitter.emit();
		}));
		return mb.build();
	}
}