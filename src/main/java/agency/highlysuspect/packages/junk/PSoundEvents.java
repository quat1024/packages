package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.PackagesInit;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class PSoundEvents {
	public static final SoundEvent PACKAGE_MAKER_CRAFT = new SoundEvent(new ResourceLocation(PackagesInit.MODID, "package_maker_craft"));
	
	public static void onInitialize() {
		Registry.register(Registry.SOUND_EVENT, PACKAGE_MAKER_CRAFT.getLocation(), PACKAGE_MAKER_CRAFT);
	}
}
