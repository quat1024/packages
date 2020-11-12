package agency.highlysuspect.packages.client.compat.canvas;

import agency.highlysuspect.packages.PackagesInit;
import net.fabricmc.loader.api.FabricLoader;

public class CanvasCompat {
	public static final CanvasProxy PROXY;
	
	static {
		CanvasProxy p;
		
		if(FabricLoader.getInstance().isModLoaded("canvas")) {
			try {
				p = (CanvasProxy) Class.forName("agency.highlysuspect.packages.client.compat.canvas.YesCanvas").newInstance();
			} catch (ReflectiveOperationException e) {
				PackagesInit.LOGGER.error("Problem initializing Canvas compat, special stuff will be disabled: ", e);
				p = new NoCanvas();
			}
		} else {
			p = new NoCanvas();
		}
		
		PROXY = p;
	}
	
	public static void onInitializeClient() {
		//Triggers static init
	}
}
