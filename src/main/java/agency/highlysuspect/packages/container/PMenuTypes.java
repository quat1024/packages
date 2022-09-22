package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.Packages;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;

public class PMenuTypes {
	public static MenuType<PackageMakerMenu> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE_MAKER = Registry.register(Registry.MENU, Packages.id("package_maker"), new MenuType<>(PackageMakerMenu::new));
	}
}
