package agency.highlysuspect.packages.client.compat.frex;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;

public class NoFrex implements FrexProxy {
	@Override
	public boolean isFrex() {
		return false;
	}
	
	@Override
	public void fancifyPackageQuad(QuadEmitter emitter, BlockState state, Sprite sprite) {
		//No-op
	}
}
