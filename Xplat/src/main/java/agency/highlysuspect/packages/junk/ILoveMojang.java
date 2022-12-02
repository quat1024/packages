package agency.highlysuspect.packages.junk;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ILoveMojang {
	public static MutableComponent translatable(String key, Object... value) {
		return new TranslatableComponent(key, value);
	}
}
