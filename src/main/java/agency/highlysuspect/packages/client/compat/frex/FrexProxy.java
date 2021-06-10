package agency.highlysuspect.packages.client.compat.frex;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;

public interface FrexProxy {
	void fancifyPackageQuad(QuadEmitter emitter, BlockState state, Sprite sprite);
}
