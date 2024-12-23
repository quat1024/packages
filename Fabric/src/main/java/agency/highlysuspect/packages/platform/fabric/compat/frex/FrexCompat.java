package agency.highlysuspect.packages.platform.fabric.compat.frex;

public class FrexCompat {
	public static final FrexProxy PROXY;
	
	static {
		FrexProxy p = new NoFrex();
//		if(PackagesClient.instance.config.get(PropsClient.FABRIC_FREX_SUPPORT) && FabricLoader.getInstance().isModLoaded("frex")) {
//			Packages.LOGGER.info("Packages is loading FREX support !");
//			try {
//				p = (FrexProxy) Class.forName("agency.highlysuspect.packages.platform.fabric.compat.frex.YesFrex").getDeclaredConstructor().newInstance();
//			} catch (Exception e) {
//				Packages.LOGGER.error("Problem initializing FREX compat, special stuff will be disabled: ", e);
//			}
//		}
		
		PROXY = p;
	}
	
	public static class NoFrex implements FrexProxy {}
	
	@SuppressWarnings("EmptyMethod")
	public static void onInitializeClient() {
		//Triggers static init
	}
}
