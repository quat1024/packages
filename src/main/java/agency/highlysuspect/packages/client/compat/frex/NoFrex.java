package agency.highlysuspect.packages.client.compat.frex;

import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public class NoFrex implements FrexProxy {
	@Override
	public boolean isFrex() {
		return false;
	}
	
	@Override
	public void fancifyPackageQuad(QuadEmitter emitter, PackageStyle style) {
		//No-op
	}
}
