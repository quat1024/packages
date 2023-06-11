package agency.highlysuspect.packages.platform;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface RegistryHandle<T> extends Supplier<T> {
	ResourceLocation getId();
}
