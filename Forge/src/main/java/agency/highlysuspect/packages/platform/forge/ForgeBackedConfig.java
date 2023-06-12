package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.config.ConfigProperty;
import agency.highlysuspect.packages.config.ConfigSchema;
import agency.highlysuspect.packages.config.CookedConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ForgeBackedConfig implements CookedConfig {
	public ForgeBackedConfig(Map<ConfigProperty<?>, ForgeConfigSpec.ConfigValue<?>> liveValues) {
		this.liveValues = liveValues;
	}
	
	private final Map<ConfigProperty<?>, ForgeConfigSpec.ConfigValue<?>> liveValues;
	private final Map<ConfigProperty<?>, Object> parsedValues = new IdentityHashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigProperty<T> prop) {
		return (T) parsedValues.computeIfAbsent(prop, this::getFresh);
	}
	
	private <T> T getFresh(ConfigProperty<T> prop) {
		T fresh = toFreshGetter(prop).get();
		
		boolean valid = prop.validate(fresh);
		if(valid) return fresh;
		
		System.err.println("Config property '" + prop.name() + "' failed validation, defaulting to " + prop.write(prop.defaultValue()));
		return prop.defaultValue();
	}
	
	@SuppressWarnings("unchecked")
	private <T> ForgeConfigSpec.ConfigValue<T> toFreshGetter(ConfigProperty<T> prop) {
		return (ForgeConfigSpec.ConfigValue<T>) liveValues.get(prop);
	}
	
	@Override
	public boolean refresh() {
		parsedValues.clear();
		return true;
	}
	
	public static class Bakery implements ConfigSchema.Bakery {
		public Bakery(ForgeConfigSpec.Builder spec) {
			this.spec = spec;
		}
		
		public final ForgeConfigSpec.Builder spec;
		
		@Override
		public CookedConfig cook(ConfigSchema schema) {
			Map<ConfigProperty<?>, ForgeConfigSpec.ConfigValue<?>> liveValues = new HashMap<>();
			
			spec.push("Uncategorized");
			
			schema.accept(new ConfigSchema.Visitor() {
				@Override
				public void visitSection(String section) {
					spec.pop().push(section);
				}
				
				@Override
				public <T> void visitOption(ConfigProperty<T> option) {
					spec.comment(option.comment().stream().map(s -> " " + s).toArray(String[]::new));
					spec.comment(" Default: " + option.write(option.defaultValue()));
					
					T def = option.defaultValue();
					if(def instanceof String || def instanceof Integer || def instanceof Boolean) {
						//using a weird define() method because less-weird ones always seem to pass `Object.class` here 👇 instead of the specific type
						liveValues.put(option, spec.define(List.of(option.name()), () -> def, option::validateErased, def.getClass()));
					} else {
						throw new IllegalArgumentException("unhandled type in ForgeConfigSpec: " + def.getClass());
					}
				}
			});
			
			return new ForgeBackedConfig(liveValues);
		}
	}
}
