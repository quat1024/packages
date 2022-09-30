package agency.highlysuspect.packages.config;

import java.util.Locale;

public interface PlatformConfigBuilder {
	PlatformConfigBuilder setSection(String name);
	
	default PlatformConfigBuilder addIntProperty(String name, int defaultValue, String... comment) {
		return addIntProperty(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, comment);
	}
	PlatformConfigBuilder addIntProperty(String name, int defaultValue, int lowInclusive, int highInclusive, String... comment);
	
	default PlatformConfigBuilder addDoubleProperty(String name, double defaultValue, String... comment) {
		//in an impressive display of Java:tm:, Double.MIN_VALUE is actually the smallest *positive nonzero* double value
		return addDoubleProperty(name, defaultValue, -Double.MAX_VALUE, Double.MAX_VALUE, comment);
	}
	PlatformConfigBuilder addDoubleProperty(String name, double defaultValue, double lowInclusive, double highInclusive, String... comment);
	
	PlatformConfigBuilder addStringProperty(String name, String defaultValue, String... comment);
	<T extends Enum<T>> PlatformConfigBuilder addEnumProperty(String name, T enumm, String... comment);
	PlatformConfigBuilder addBooleanProperty(String name, boolean defaultValue, String... comment);
	
	PlatformConfig build(ConfigType configType);
	
	default String joinWithNewlines(String... comment) {
		return String.join("\n", comment);
	}
	
	enum ConfigType {
		CLIENT, COMMON;
		
		@Override
		public String toString() {
			return name().toLowerCase(Locale.ROOT);
		}
	}
}
