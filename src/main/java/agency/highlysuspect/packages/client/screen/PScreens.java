package agency.highlysuspect.packages.client.screen;

import agency.highlysuspect.packages.container.PMenuTypes;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class PScreens {
	public static void onInitializeClient() {
		ScreenRegistry.register(PMenuTypes.PACKAGE_MAKER, PackageMakerScreen::new);
	}
}
