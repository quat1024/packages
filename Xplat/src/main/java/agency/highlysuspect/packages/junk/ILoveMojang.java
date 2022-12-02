package agency.highlysuspect.packages.junk;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ILoveMojang {
	public static MutableComponent translatable(String key, Object... value) {
		return Component.translatable(key, value);
	}
}
