package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.Init;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;

public class PMenuTypes {
	public static MenuType<PackageMakerMenu> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE_MAKER = Registry.register(Registry.MENU, Init.id("package_maker"), new MenuType<>(PackageMakerMenu::new));
	}
}
