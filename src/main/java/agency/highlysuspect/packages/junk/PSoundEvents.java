package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.Packages;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;

public class PSoundEvents {
	public static final SoundEvent PACKAGE_MAKER_CRAFT = new SoundEvent(Packages.id("package_maker_craft"));
	public static final SoundEvent INSERT_ONE = new SoundEvent(Packages.id("insert_one"));
	public static final SoundEvent TAKE_ONE = new SoundEvent(Packages.id("take_one"));
	public static final SoundEvent INSERT_STACK = new SoundEvent(Packages.id("insert_stack"));
	public static final SoundEvent TAKE_STACK = new SoundEvent(Packages.id("take_stack"));
	public static final SoundEvent INSERT_ALL = new SoundEvent(Packages.id("insert_all"));
	public static final SoundEvent TAKE_ALL = new SoundEvent(Packages.id("take_all"));
	
	public static void onInitialize() {
		Registry.register(Registry.SOUND_EVENT, PACKAGE_MAKER_CRAFT.getLocation(), PACKAGE_MAKER_CRAFT);
		Registry.register(Registry.SOUND_EVENT, INSERT_ONE.getLocation(), INSERT_ONE);
		Registry.register(Registry.SOUND_EVENT, TAKE_ONE.getLocation(), TAKE_ONE);
		Registry.register(Registry.SOUND_EVENT, INSERT_STACK.getLocation(), INSERT_STACK);
		Registry.register(Registry.SOUND_EVENT, TAKE_STACK.getLocation(), TAKE_STACK);
		Registry.register(Registry.SOUND_EVENT, INSERT_ALL.getLocation(), INSERT_ALL);
		Registry.register(Registry.SOUND_EVENT, TAKE_ALL.getLocation(), TAKE_ALL);
	}
}
