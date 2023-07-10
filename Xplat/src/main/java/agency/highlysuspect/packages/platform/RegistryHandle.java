package agency.highlysuspect.packages.platform;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface RegistryHandle<T> extends Supplier<T> {
	ResourceLocation getId();
	
	record Immediate<T>(T thing, ResourceLocation id) implements RegistryHandle<T> {
		@Override
		public T get() {
			return thing;
		}
		
		@Override
		public ResourceLocation getId() {
			return id;
		}
	}
}
