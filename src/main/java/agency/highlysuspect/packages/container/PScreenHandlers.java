package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.PackagesInit;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class PScreenHandlers {
	public static MenuType<PackageMakerScreenHandler> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE_MAKER = ScreenHandlerRegistry.registerExtended(new ResourceLocation(PackagesInit.MODID, "package_maker"), PackageMakerScreenHandler::constructFromNetwork);
	}
}
