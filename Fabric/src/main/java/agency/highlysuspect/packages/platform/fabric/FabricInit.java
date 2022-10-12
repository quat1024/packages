package agency.highlysuspect.packages.platform.fabric;

import agency.highlysuspect.packages.Packages;
import net.fabricmc.api.ModInitializer;

public class FabricInit extends Packages implements ModInitializer {
	public FabricInit() {
		super(new FabricPlatformSupport());
	}
	
	@Override
	public void onInitialize() {
		earlySetup();
	}
}
