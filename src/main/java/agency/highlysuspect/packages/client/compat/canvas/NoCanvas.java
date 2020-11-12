package agency.highlysuspect.packages.client.compat.canvas;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public class NoCanvas implements CanvasProxy {
	@Override
	public boolean isCanvas() {
		return false;
	}
	
	@Override
	public void fixQuadEmitter(QuadEmitter emitter) {
		//No-op
	}
}
