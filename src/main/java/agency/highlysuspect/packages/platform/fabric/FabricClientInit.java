package agency.highlysuspect.packages.platform.fabric;

import agency.highlysuspect.packages.client.PackagesClient;
import agency.highlysuspect.packages.platform.fabric.compat.frex.FrexCompat;
import net.fabricmc.api.ClientModInitializer;

public class FabricClientInit extends PackagesClient implements ClientModInitializer {
	public FabricClientInit() {
		super(new FabricClientPlatformSupport());
	}
	
	@Override
	public void onInitializeClient() {
		earlySetup();
		
		FrexCompat.onInitializeClient();
	}
}
