package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.Packages;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

//TODO 1.19.4: restrucutre?
public class PSoundEvents {
	public static final SoundEvent PACKAGE_MAKER_CRAFT = SoundEvent.createVariableRangeEvent(Packages.id("package_maker_craft"));
	public static final SoundEvent INSERT_ONE = SoundEvent.createVariableRangeEvent(Packages.id("insert_one"));
	public static final SoundEvent TAKE_ONE = SoundEvent.createVariableRangeEvent(Packages.id("take_one"));
	public static final SoundEvent INSERT_STACK = SoundEvent.createVariableRangeEvent(Packages.id("insert_stack"));
	public static final SoundEvent TAKE_STACK = SoundEvent.createVariableRangeEvent(Packages.id("take_stack"));
	public static final SoundEvent INSERT_ALL = SoundEvent.createVariableRangeEvent(Packages.id("insert_all"));
	public static final SoundEvent TAKE_ALL = SoundEvent.createVariableRangeEvent(Packages.id("take_all"));
	
	public static void onInitialize() {
		Packages.instance.register(BuiltInRegistries.SOUND_EVENT, PACKAGE_MAKER_CRAFT.getLocation(), () -> PACKAGE_MAKER_CRAFT);
		Packages.instance.register(BuiltInRegistries.SOUND_EVENT, INSERT_ONE.getLocation(), () -> INSERT_ONE);
		Packages.instance.register(BuiltInRegistries.SOUND_EVENT, TAKE_ONE.getLocation(), () -> TAKE_ONE);
		Packages.instance.register(BuiltInRegistries.SOUND_EVENT, INSERT_STACK.getLocation(), () -> INSERT_STACK);
		Packages.instance.register(BuiltInRegistries.SOUND_EVENT, TAKE_STACK.getLocation(), () -> TAKE_STACK);
		Packages.instance.register(BuiltInRegistries.SOUND_EVENT, INSERT_ALL.getLocation(), () -> INSERT_ALL);
		Packages.instance.register(BuiltInRegistries.SOUND_EVENT, TAKE_ALL.getLocation(), () -> TAKE_ALL);
	}
}
