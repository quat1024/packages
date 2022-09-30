package agency.highlysuspect.packages.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public interface PlatformConfig {
	int getInt(String name);
	double getDouble(String name);
	String getString(String name);
	Enum<?> getEnum(String name);
	boolean getBoolean(String name);
	
	void installConfigLoadListener(Consumer<PlatformConfig> action);
	
	default PackagesConfig mapToPojo() {
		PackagesConfig config = new PackagesConfig();
		
		try {
			for(Field field : config.getClass().getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;
				field.setAccessible(true);
				
				if(field.getType() == Integer.TYPE || field.getType() == Integer.class) {
					field.set(config, getInt(field.getName()));
				} else if(field.getType() == Double.TYPE || field.getType() == Double.class) {
					field.set(config, getDouble(field.getName()));
				} else if(field.getType() == String.class) {
					field.set(config, getString(field.getName()));
				} else if(Enum.class.isAssignableFrom(field.getType())) { //its backwards!!!!
					field.set(config, getEnum(field.getName()));
				} else if(field.getType() == Boolean.TYPE || field.getType() == Boolean.class) {
					field.set(config, getBoolean(field.getName()));
				} else if(field.getType() == PackageActionBinding.class) {
					//TODO: Not this
					field.set(config, new PackageActionBinding.SerializerDeserializer().parse(field, getString(field.getName())));
				} else {
					throw new IllegalStateException("Unmappable field in PackagesConfig: " + field);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		config.finish();
		return config;
	}
}
