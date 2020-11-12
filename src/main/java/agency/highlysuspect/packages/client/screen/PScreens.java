package agency.highlysuspect.packages.client.screen;

import agency.highlysuspect.packages.container.PScreenHandlers;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class PScreens {
	public static void onInitializeClient() {
		ScreenRegistry.register(PScreenHandlers.PACKAGE_MAKER, PackageMakerScreen::new);
	}
}
