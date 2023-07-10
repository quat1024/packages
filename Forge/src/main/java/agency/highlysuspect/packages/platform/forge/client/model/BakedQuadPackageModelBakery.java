package agency.highlysuspect.packages.platform.forge.client.model;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.client.PackageModelBakery;
import agency.highlysuspect.packages.client.PackagesClient;
import agency.highlysuspect.packages.client.PropsClient;
import agency.highlysuspect.packages.junk.PUtil;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class BakedQuadPackageModelBakery implements PackageModelBakery<List<BakedQuad>> {
	public final BakedModel baseModel;
	public final TextureAtlasSprite specialFrameSprite;
	public final TextureAtlasSprite specialInnerSprite;
	
	public BakedQuadPackageModelBakery(BakedModel baseModel, TextureAtlasSprite specialFrameSprite, TextureAtlasSprite specialInnerSprite) {
		this.baseModel = baseModel;
		this.specialFrameSprite = specialFrameSprite;
		this.specialInnerSprite = specialInnerSprite;
	}
	
	@Override
	public BakedModel getBaseModel() {
		return baseModel;
	}
	
	@Override
	public List<BakedQuad> bake(@Nullable Object cacheKey, @Nullable DyeColor faceColor, @Nullable Block frameBlock, @Nullable Block innerBlock) {
		boolean swapRedAndBlue = PackagesClient.instance.config.get(PropsClient.FORGE_SWAP_RED_AND_BLUE);
		
		ArrayList<BakedQuad> result = new ArrayList<>();
		
		BlockRenderDispatcher mgr = Minecraft.getInstance().getBlockRenderer();
		@Nullable BlockState frameState = frameBlock == null ? null : frameBlock.defaultBlockState();
		@Nullable TextureAtlasSprite frameSprite = frameState == null ? null : mgr.getBlockModel(frameState).getParticleIcon(ModelData.EMPTY);
		
		@Nullable BlockState innerState = innerBlock == null ? null : innerBlock.defaultBlockState();
		@Nullable TextureAtlasSprite innerSprite = innerState == null ? null : mgr.getBlockModel(innerState).getParticleIcon(ModelData.EMPTY);
		
		RandomSource random = new LegacyRandomSource(42);
		for(Direction cullFace : PUtil.DIRECTIONS_AND_NULL) {
			for(BakedQuad quad : baseModel.getQuads(PBlocks.PACKAGE.get().defaultBlockState(), cullFace, random, ModelData.EMPTY, null)) {
				if(quad.getTintIndex() == 1) {
					if(faceColor != null) {
						int tint = 0xFF000000 | faceColor.getMapColor().col;
						if(swapRedAndBlue) tint = swapRedAndBlue(tint);
						
						BakedQuad copy = copyQuad(quad);
						setTintColor(copy, tint, tint, tint, tint);
						result.add(copy);
					}
					continue;
				}
				
				QuadUvBounds bounds = QuadUvBounds.readOff(quad);
				
				if(bounds.displaysSprite(specialFrameSprite)) {
					if(frameSprite != null) {
						BakedQuad copy = copyQuad(quad);
						bounds.remapQuad(copy, specialFrameSprite, frameSprite);
						result.add(copy);
					}
					continue;
				}
				
				if(bounds.displaysSprite(specialInnerSprite)) {
					if(innerSprite != null) {
						BakedQuad copy = copyQuad(quad);
						bounds.remapQuad(copy, specialInnerSprite, innerSprite);
						result.add(copy);
					}
					continue;
				}
				
				//Not a special quad, leave it as-is (without copying).
				result.add(quad);
			}
		}
		
		return result;
	}
	
	record QuadUvBounds(float minU, float maxU, float minV, float maxV) {
		static QuadUvBounds readOff(BakedQuad in) {
			float minU = Float.POSITIVE_INFINITY;
			float maxU = Float.NEGATIVE_INFINITY;
			float minV = Float.POSITIVE_INFINITY;
			float maxV = Float.NEGATIVE_INFINITY;
			
			for(int i = 0; i < 4; i++) {
				float u = getU(in, i);
				if(minU > u) minU = u;
				if(maxU < u) maxU = u;
				
				float v = getV(in, i);
				if(minV > v) minV = v;
				if(maxV < v) maxV = v;
			}
			
			return new QuadUvBounds(minU, maxU, minV, maxV);
		}
		
		boolean displaysSprite(TextureAtlasSprite sprite) {
			return sprite.getU0() <= minU && sprite.getU1() >= maxU && sprite.getV0() <= minV && sprite.getV1() >= maxV;
		}
		
		void remapQuad(BakedQuad in, TextureAtlasSprite specialSprite, TextureAtlasSprite newSprite) {
			float remappedMinU = PUtil.rangeRemap(minU, specialSprite.getU0(), specialSprite.getU1(), newSprite.getU0(), newSprite.getU1());
			float remappedMaxU = PUtil.rangeRemap(maxU, specialSprite.getU0(), specialSprite.getU1(), newSprite.getU0(), newSprite.getU1());
			float remappedMinV = PUtil.rangeRemap(minV, specialSprite.getV0(), specialSprite.getV1(), newSprite.getV0(), newSprite.getV1());
			float remappedMaxV = PUtil.rangeRemap(maxV, specialSprite.getV0(), specialSprite.getV1(), newSprite.getV0(), newSprite.getV1());
			
			for(int i = 0; i < 4; i++) {
				setU(in, i, Mth.equal(getU(in, i), minU) ? remappedMinU : remappedMaxU);
				setV(in, i, Mth.equal(getV(in, i), minV) ? remappedMinV : remappedMaxV);
			}
		}
	}
	
	private static BakedQuad copyQuad(BakedQuad in) {
		int[] vertsCopy = new int[in.getVertices().length];
		System.arraycopy(in.getVertices(), 0, vertsCopy, 0, vertsCopy.length);
		
		return new BakedQuad(vertsCopy, in.getTintIndex(), in.getDirection(), in.getSprite(), in.isShade());
	}
	
	//Here's where things get fun!!! Forge has a BakedQuadBuilder but it is turbo useless for this btw
	private static final int vertexStride, vertexColorOffset, vertexUOffset, vertexVOffset; //Int offsets not byte offsets btw
	static {
		vertexStride = DefaultVertexFormat.BLOCK.getIntegerSize();
		
		//I tried writing code to calculate this from the VertexFormat, but couldn't figure out how to do it.
		//Just hardcode them. If FREX can do it I can do it too
		vertexColorOffset = 3;
		vertexUOffset = 4;
		vertexVOffset = 5;
	}
	
	private static void setTintColor(BakedQuad in, int color1, int color2, int color3, int color4) {
		in.getVertices()[                   vertexColorOffset] = color1;
		in.getVertices()[    vertexStride + vertexColorOffset] = color2;
		in.getVertices()[2 * vertexStride + vertexColorOffset] = color3;
		in.getVertices()[3 * vertexStride + vertexColorOffset] = color4;
	}
	
	//Converts a color from AARRGGBB format to AABBGGRR. Vanilla takes ARGB, Forge takes ABGR. No idea why.
	private static int swapRedAndBlue(int color) {
		return ((color & 0x00FF0000) >> 16) | ((color & 0x000000FF) << 16) | (color & 0xFF00FF00);
	}
	
	private static float getU(BakedQuad in, int vertex) {
		return Float.intBitsToFloat(in.getVertices()[vertex * vertexStride + vertexUOffset]);
	}
	
	private static void setU(BakedQuad in, int vertex, float u) {
		in.getVertices()[vertex * vertexStride + vertexUOffset] = Float.floatToRawIntBits(u);
	}
	
	private static float getV(BakedQuad in, int vertex) {
		return Float.intBitsToFloat(in.getVertices()[vertex * vertexStride + vertexVOffset]);
	}
	
	private static void setV(BakedQuad in, int vertex, float v) {
		in.getVertices()[vertex * vertexStride + vertexVOffset] = Float.floatToRawIntBits(v);
	}
}
