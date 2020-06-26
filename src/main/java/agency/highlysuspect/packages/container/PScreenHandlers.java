package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.PackagesInit;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class PScreenHandlers {
	public static ScreenHandlerType<PackageMakerScreenHandler> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE_MAKER = ScreenHandlerRegistry.registerExtended(new Identifier(PackagesInit.MODID, "package_maker"), PackageMakerScreenHandler::constructFromNetwork);
	}
}
