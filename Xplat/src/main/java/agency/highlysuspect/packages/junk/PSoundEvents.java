package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.platform.PlatformSupport;
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
	
	public static void onInitialize(PlatformSupport plat) {
		//Here I am ignoring the returned RegistryHandle because SoundEvent is just a wrapper around ResourceLocation.
		//It's okay to initialize them once at startup and register them later. Even on Forge.
		
		plat.register(Registry.SOUND_EVENT, PACKAGE_MAKER_CRAFT.getLocation(), () -> PACKAGE_MAKER_CRAFT);
		plat.register(Registry.SOUND_EVENT, INSERT_ONE.getLocation(), () -> INSERT_ONE);
		plat.register(Registry.SOUND_EVENT, TAKE_ONE.getLocation(), () -> TAKE_ONE);
		plat.register(Registry.SOUND_EVENT, INSERT_STACK.getLocation(), () -> INSERT_STACK);
		plat.register(Registry.SOUND_EVENT, TAKE_STACK.getLocation(), () -> TAKE_STACK);
		plat.register(Registry.SOUND_EVENT, INSERT_ALL.getLocation(), () -> INSERT_ALL);
		plat.register(Registry.SOUND_EVENT, TAKE_ALL.getLocation(), () -> TAKE_ALL);
	}
}
