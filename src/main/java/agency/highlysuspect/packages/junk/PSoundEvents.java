package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.PackagesInit;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PSoundEvents {
	public static final SoundEvent PACKAGE_MAKER_CRAFT = new SoundEvent(new Identifier(PackagesInit.MODID, "package_maker_craft"));
	
	public static void onInitialize() {
		Registry.register(Registry.SOUND_EVENT, PACKAGE_MAKER_CRAFT.getId(), PACKAGE_MAKER_CRAFT);
	}
}
