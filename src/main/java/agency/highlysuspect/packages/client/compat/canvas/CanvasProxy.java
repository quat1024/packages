package agency.highlysuspect.packages.client.compat.canvas;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public interface CanvasProxy {
	boolean isCanvas();
	void fixQuadEmitter(QuadEmitter emitter);
}
