package agency.highlysuspect.packages.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Random;

/**
 * Copy paste of fabric-rendering-api ForwardingBakedModel but frapi stuff stripped.
 */
public class SimpleForwardingBakedModel implements BakedModel {
	public SimpleForwardingBakedModel() {}
	
	public SimpleForwardingBakedModel(BakedModel wrapped) {
		this.wrapped = wrapped;
	}
	
	public BakedModel wrapped;
	
	@Override
	public List<BakedQuad> getQuads(BlockState blockState, Direction face, Random rand) {
		return wrapped.getQuads(blockState, face, rand);
	}
	
	@Override
	public boolean useAmbientOcclusion() {
		return wrapped.useAmbientOcclusion();
	}
	
	@Override
	public boolean isGui3d() {
		return wrapped.isGui3d();
	}
	
	@Override
	public boolean isCustomRenderer() {
		return wrapped.isCustomRenderer();
	}
	
	@Override
	public TextureAtlasSprite getParticleIcon() {
		return wrapped.getParticleIcon();
	}
	
	@Override
	public boolean usesBlockLight() {
		return wrapped.usesBlockLight();
	}
	
	@Override
	public ItemTransforms getTransforms() {
		return wrapped.getTransforms();
	}
	
	@Override
	public ItemOverrides getOverrides() {
		return wrapped.getOverrides();
	}
}
