package agency.highlysuspect.packages.client.compat.frex;

import agency.highlysuspect.packages.Packages;
import net.fabricmc.loader.api.FabricLoader;

public class FrexCompat {
	public static final FrexProxy PROXY;
	
	static {
		FrexProxy p;
		
		if(Packages.config.frexSupport && FabricLoader.getInstance().isModLoaded("frex")) {
			Packages.LOGGER.info("Packages is loading FREX support !");
			try {
				p = (FrexProxy) Class.forName("agency.highlysuspect.packages.client.compat.frex.YesFrex").getDeclaredConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				Packages.LOGGER.error("Problem initializing FREX compat, special stuff will be disabled: ", e);
				p = FrexProxy.Nil.INSTANCE;
			}
		} else p = FrexProxy.Nil.INSTANCE;
		
		PROXY = p;
	}
	
	@SuppressWarnings("EmptyMethod")
	public static void onInitializeClient() {
		//Triggers static init
	}
}
