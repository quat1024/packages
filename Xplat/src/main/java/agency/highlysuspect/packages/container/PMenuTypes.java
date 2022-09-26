package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;

public class PMenuTypes {
	public static PlatformSupport.RegistryHandle<MenuType<PackageMakerMenu>> PACKAGE_MAKER;
	
	public static void onInitialize(PlatformSupport plat) {
		PACKAGE_MAKER = plat.register(Registry.MENU, Packages.id("package_maker"), () -> new MenuType<>(PackageMakerMenu::new));
	}
}
