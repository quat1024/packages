package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.Init;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;

public class PSoundEvents {
	public static final SoundEvent PACKAGE_MAKER_CRAFT = new SoundEvent(Init.id("package_maker_craft"));
	
	public static void onInitialize() {
		Registry.register(Registry.SOUND_EVENT, PACKAGE_MAKER_CRAFT.getLocation(), PACKAGE_MAKER_CRAFT);
	}
}
