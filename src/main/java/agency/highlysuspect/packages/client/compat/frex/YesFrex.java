package agency.highlysuspect.packages.client.compat.frex;

import grondag.frex.api.material.MaterialMap;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;

public class YesFrex implements FrexProxy {
	
	@Override
	public void fancifyPackageQuad(QuadEmitter emitter, BlockState state, Sprite sprite) {
		emitter.material(MaterialMap.get(state).getMapped(sprite));
	}
}
