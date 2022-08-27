package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.Init;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.world.inventory.MenuType;

public class PMenuTypes {
	public static MenuType<PackageMakerMenu> PACKAGE_MAKER;
	
	public static void onInitialize() {
		//TODO: ScreenHandlerRegistry is deprecated
		PACKAGE_MAKER = ScreenHandlerRegistry.registerExtended(Init.id("package_maker"), PackageMakerMenu::constructFromNetwork);
	}
}
