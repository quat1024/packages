package agency.highlysuspect.packages.platform.forge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Supplier;

public record NoConfigGeometryLoader<T extends IUnbakedGeometry<T>>(Supplier<T> what) implements IGeometryLoader<T> {
	@Override
	public T read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
		return what.get();
	}
}
