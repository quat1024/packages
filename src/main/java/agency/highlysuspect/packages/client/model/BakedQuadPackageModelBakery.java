package agency.highlysuspect.packages.client.model;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.junk.PUtil;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		ArrayList<BakedQuad> result = new ArrayList<>();
		
		BlockRenderDispatcher mgr = Minecraft.getInstance().getBlockRenderer();
		@Nullable BlockState frameState = frameBlock == null ? null : frameBlock.defaultBlockState();
		@Nullable TextureAtlasSprite frameSprite = frameState == null ? null : mgr.getBlockModel(frameState).getParticleIcon();
		
		@Nullable BlockState innerState = innerBlock == null ? null : innerBlock.defaultBlockState();
		@Nullable TextureAtlasSprite innerSprite = innerState == null ? null : mgr.getBlockModel(innerState).getParticleIcon();
		
		Random random = new Random(42);
		for(Direction cullFace : PUtil.DIRECTIONS_AND_NULL) {
			for(BakedQuad quad : baseModel.getQuads(PBlocks.PACKAGE.get().defaultBlockState(), cullFace, random)) {
				if(quad.getTintIndex() == 1) {
					if(faceColor != null) {
						int tint = 0xFF000000 | faceColor.getMaterialColor().col;
						
						BakedQuad copy = copyQuad(quad);
						setTintColor(copy, tint, tint, tint, tint);
						result.add(copy);
					}
					continue;
				}
				
				//TODO: the rest
				result.add(quad);
			}
		}
		
		return result;
	}
	
	private static BakedQuad copyQuad(BakedQuad in) {
		return new BakedQuad(in.getVertices(), in.getTintIndex(), in.getDirection(), in.getSprite(), in.isShade());
	}
	
	//Here's where things get fun!!!
	
	int vertexStride, vertexColorOffset;
	{
		vertexStride = DefaultVertexFormat.BLOCK.getIntegerSize();
		//I tried writing code to calculate this from the VertexFormat but couldn't figure out how to do it.
		vertexColorOffset = 3;
	}
	
	private void setTintColor(BakedQuad in, int color1, int color2, int color3, int color4) {
		in.getVertices()[                   vertexColorOffset] = color1;
		in.getVertices()[    vertexStride + vertexColorOffset] = color2;
		in.getVertices()[2 * vertexStride + vertexColorOffset] = color3;
		in.getVertices()[3 * vertexStride + vertexColorOffset] = color4;
	}
}
