package agency.highlysuspect.packages.client.compat.frex;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;

public interface FrexProxy {
	default void fancifyPackageQuad(QuadEmitter emitter, BlockState state, TextureAtlasSprite sprite) {
		//No-op by default.
	}
	
	class Nil implements FrexProxy {
		public static final Nil INSTANCE = new Nil();
	}
}
