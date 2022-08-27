package agency.highlysuspect.packages.client.compat.frex;

import agency.highlysuspect.packages.Init;
import net.fabricmc.loader.api.FabricLoader;

public class FrexCompat {
	public static final FrexProxy PROXY;
	
	static {
		FrexProxy p;
		
		//TODO: Readd Frex compat in 1.18
		//noinspection PointlessBooleanExpression,ConstantConditions
		if(false && FabricLoader.getInstance().isModLoaded("frex")) {
			try {
				p = (FrexProxy) Class.forName("agency.highlysuspect.packages.client.compat.frex.YesFrex").getDeclaredConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				Init.LOGGER.error("Problem initializing FREX compat, special stuff will be disabled: ", e);
				p = new NoFrex();
			}
		} else {
			p = new NoFrex();
		}
		
		PROXY = p;
	}
	
	@SuppressWarnings("EmptyMethod")
	public static void onInitializeClient() {
		//Triggers static init
	}
}
