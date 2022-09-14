package agency.highlysuspect.packages.client.compat.frex;

import io.vram.frex.api.material.MaterialMap;
import io.vram.frex.fabric.compat.FabricMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;

public class YesFrex implements FrexProxy {
	@Override
	public void fancifyPackageQuad(QuadEmitter emitter, BlockState state, TextureAtlasSprite sprite) {
		emitter.material(FabricMaterial.of(MaterialMap.get(state).getMapped(sprite)));
	}
}
