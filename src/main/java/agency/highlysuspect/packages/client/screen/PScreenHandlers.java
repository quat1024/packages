package agency.highlysuspect.packages.client.screen;

import agency.highlysuspect.packages.container.PContainerTypes;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;

public class PScreenHandlers {
	public static void onInitialize() {
		ScreenProviderRegistry.INSTANCE.registerFactory(PContainerTypes.PACKAGE_MAKER, PackageMakerScreen::new);
	}
}
