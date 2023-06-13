package agency.highlysuspect.packages.menu;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.platform.RegistryHandle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public class PMenuTypes {
	public static RegistryHandle<MenuType<PackageMakerMenu>> PACKAGE_MAKER;
	
	public static void onInitialize() {
		PACKAGE_MAKER = Packages.instance.register(BuiltInRegistries.MENU, Packages.id("package_maker"), () -> Packages.instance.makeMenuType(PackageMakerMenu::new));
	}
}
