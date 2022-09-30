package agency.highlysuspect.packages.platform.forge.config;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.config.PlatformConfig;
import agency.highlysuspect.packages.config.PlatformConfigBuilder;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ForgeConfigBuilder implements PlatformConfigBuilder {
	private final ForgeConfigSpec.Builder bob = new ForgeConfigSpec.Builder().push("Uncategorized");
	
	private final Map<String, ForgeConfigSpec.ConfigValue<Integer>> intValues = new HashMap<>();
	private final Map<String, ForgeConfigSpec.ConfigValue<Double>> doubleValues = new HashMap<>();
	private final Map<String, ForgeConfigSpec.ConfigValue<String>> stringValues = new HashMap<>();
	private final Map<String, ForgeConfigSpec.EnumValue<?>> enumValues = new HashMap<>();
	private final Map<String, ForgeConfigSpec.ConfigValue<Boolean>> booleanValues = new HashMap<>();
	
	private ForgeConfigSpec.Builder comment2(ForgeConfigSpec.Builder bob, String... comment) {
		if(comment != null) bob.comment(comment);
		return bob;
	}
	
	@Override
	public PlatformConfigBuilder setSection(String name) {
		bob.pop();
		bob.push(name);
		return this;
	}
	
	@Override
	public PlatformConfigBuilder addIntProperty(String name, int defaultValue, int lowInclusive, int highInclusive, String... comment) {
		comment2(bob, comment);
		if(lowInclusive == Integer.MIN_VALUE && highInclusive == Integer.MAX_VALUE) {
			intValues.put(name, bob.define(name, defaultValue));
		} else {
			intValues.put(name, bob.defineInRange(name, defaultValue, lowInclusive, highInclusive));
		}
		
		return this;
	}
	
	@Override
	public PlatformConfigBuilder addDoubleProperty(String name, double defaultValue, double lowInclusive, double highInclusive, String... comment) {
		comment2(bob, comment);
		if(lowInclusive == -Double.MAX_VALUE && highInclusive == Double.MAX_VALUE) {
			doubleValues.put(name, bob.define(name, defaultValue));
		} else {
			doubleValues.put(name, bob.defineInRange(name, defaultValue, lowInclusive, highInclusive));
		}
		
		return this;
	}
	
	@Override
	public PlatformConfigBuilder addStringProperty(String name, String defaultValue, String... comment) {
		stringValues.put(name, comment2(bob, comment).define(name, defaultValue));
		return this;
	}
	
	@Override
	public <T extends Enum<T>> PlatformConfigBuilder addEnumProperty(String name, T enumm, String... comment) {
		enumValues.put(name, comment2(bob, comment).defineEnum(name, enumm));
		return this;
	}
	
	@Override
	public PlatformConfigBuilder addBooleanProperty(String name, boolean defaultValue, String... comment) {
		booleanValues.put(name, comment2(bob, comment).define(name, defaultValue));
		return this;
	}
	
	@Override
	public PlatformConfig build(ConfigType configType) {
		ModLoadingContext.get().registerConfig(switch(configType) {
			case CLIENT -> ModConfig.Type.CLIENT;
			case COMMON -> ModConfig.Type.COMMON;
		}, bob.build());
		
		return new PlatformConfig() {
			@Override
			public int getInt(String name) {
				return intValues.get(name).get();
			}
			
			@Override
			public double getDouble(String name) {
				return doubleValues.get(name).get();
			}
			
			@Override
			public String getString(String name) {
				return stringValues.get(name).get();
			}
			
			@Override
			public Enum<?> getEnum(String name) {
				return enumValues.get(name).get();
			}
			
			@Override
			public boolean getBoolean(String name) {
				return booleanValues.get(name).get();
			}
			
			@Override
			public void installConfigLoadListener(Consumer<PlatformConfig> action) {
				FMLJavaModLoadingContext.get().getModEventBus().addListener((ModConfigEvent.Loading load) -> {
					if(load.getConfig().getModId().equals(Packages.MODID)) action.accept(this);
				});
				
				FMLJavaModLoadingContext.get().getModEventBus().addListener((ModConfigEvent.Reloading reload) -> {
					if(reload.getConfig().getModId().equals(Packages.MODID)) action.accept(this);
				});
			}
		};
	}
}
