package agency.highlysuspect.packages.client.compat.canvas;

import agency.highlysuspect.packages.PackagesInit;
import grondag.canvas.apiimpl.mesh.QuadViewImpl;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

import java.lang.reflect.Field;

public class YesCanvas implements CanvasProxy {
	@Override
	public boolean isCanvas() {
		return true;
	}
	
	@Override
	public void fixQuadEmitter(QuadEmitter emitter) {
		if(emitter instanceof QuadViewImpl) {
			//It's protected lol
			//Note that this is only called at model bake time, not chunk bake/item rendering, so performance is not an issue
			try {
				if(OLD_CANVAS_INT_FIELD != null) OLD_CANVAS_INT_FIELD.set(emitter, 0);
				if(NEW_CANVAS_BOOL_FIELD != null) NEW_CANVAS_BOOL_FIELD.set(emitter, false);
			} catch (ReflectiveOperationException e) {
				PackagesInit.LOGGER.error(e);
			}
		}
	}
	
	private static final Field OLD_CANVAS_INT_FIELD;
	private static final Field NEW_CANVAS_BOOL_FIELD;
	
	static {
		Field smfs, smf;
		
		try {
			//noinspection JavaReflectionMemberAccess
			smfs = QuadViewImpl.class.getDeclaredField("spriteMappedFlags");
			smfs.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			smfs = null;
			//Swallow it, normal on newer Canvases
		}
		
		OLD_CANVAS_INT_FIELD = smfs;
		
		try {
			smf = QuadViewImpl.class.getDeclaredField("spriteMappedFlag");
			smf.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			smf = null;
			//Swallow it, normal on older Canvases
		}
		
		NEW_CANVAS_BOOL_FIELD = smf;
	}
}
