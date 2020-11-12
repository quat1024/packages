package agency.highlysuspect.packages.client.compat.frex;

import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public interface FrexProxy {
	boolean isFrex();
	void fancifyPackageQuad(QuadEmitter emitter, PackageStyle style);
}
