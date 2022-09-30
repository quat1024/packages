package agency.highlysuspect.packages.platform.fabric.config;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.config.PlatformConfig;
import agency.highlysuspect.packages.config.PlatformConfigBuilder;
import agency.highlysuspect.packages.platform.fabric.FabricPlatformSupport;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.StringRepresentable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BarebonesConfigShape implements PlatformConfigBuilder {
	public sealed interface Element permits Heading, Option {}
	public static record Heading(String name) implements Element {}
	public static record Option(String key, String defaultValue, List<String> comment) implements Element {}
	
	private final List<Element> elements = new ArrayList<>();
	private final Map<String, Option> intOptions = new HashMap<>();
	private final Map<String, Option> doubleOptions = new HashMap<>();
	private final Map<String, Option> stringOptions = new HashMap<>();
	private final Map<String, Option> enumOptions = new HashMap<>();
	private final Map<String, Option> booleanOptions = new HashMap<>();
	
	private void addOption(Map<String, Option> map, Option o) {
		elements.add(o);
		map.put(o.key(), o);
	}
	
	@Override
	public PlatformConfigBuilder setSection(String name) {
		elements.add(new Heading(name));
		return this;
	}
	
	@Override
	public PlatformConfigBuilder addIntProperty(String name, int defaultValue, int lowInclusive, int highInclusive, String... comment) {
		//TODO: write the bounds comment (packages doesn't use it)
		addOption(intOptions, new Option(name, Integer.toString(defaultValue), List.of(comment)));
		return this;
	}
	
	@Override
	public PlatformConfigBuilder addDoubleProperty(String name, double defaultValue, double lowInclusive, double highInclusive, String... comment) {
		//TODO: write the bounds comment (packages doesn't use it)
		addOption(doubleOptions, new Option(name, Double.toString(defaultValue), List.of(comment)));
		return this;
	}
	
	@Override
	public PlatformConfigBuilder addStringProperty(String name, String defaultValue, String... comment) {
		addOption(stringOptions, new Option(name, defaultValue, List.of(comment)));
		return this;
	}
	
	@Override
	public <T extends Enum<T>> PlatformConfigBuilder addEnumProperty(String name, T enumm, String... comment) {
		String x;
		if(enumm instanceof StringRepresentable sr) x = sr.getSerializedName();
		else x = enumm.name();
		
		addOption(enumOptions, new Option(name, x, List.of(comment)));
		return this;
	}
	
	@Override
	public PlatformConfigBuilder addBooleanProperty(String name, boolean defaultValue, String... comment) {
		addOption(booleanOptions, new Option(name, Boolean.toString(defaultValue), List.of(comment)));
		return this;
	}
	
	@Override
	public PlatformConfig build(ConfigType configType) {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(Packages.MODID + "-" + configType.toString() + ".cfg");
		return new ConfigState(path);
	}
	
	///
	
	public static class ConfigParseException extends RuntimeException {
		public ConfigParseException(String message) { super(message); }
		public ConfigParseException(String message, Throwable cause) { super(message, cause); }
	}
	
	class ConfigState implements PlatformConfig {
		public ConfigState(Path path) {
			this.path = path;
		}
		
		final Path path;
		final Map<String, String> values = new HashMap<>();
		
		void read() throws IOException {
			values.clear();
			
			//basically a glorified java properties parser
			for(String value : Files.readAllLines(path)) {
				String s = value.trim();
				if(s.isBlank() || s.startsWith("#")) continue;
				
				int eqIndex = s.indexOf('=');
				if(eqIndex == -1) throw new ConfigParseException("there is no equal sign to split a key/value pair");
				values.put(s.substring(0, eqIndex).trim(), s.substring(eqIndex + 1).trim());
			}
		}
		
		void write() throws IOException {
			List<String> lines = new ArrayList<>();
			for(Element e : elements) {
				if(e instanceof Heading h) {
					String bar = "#".repeat(h.name.length() + 6);
					lines.add(bar);
					lines.add("## " + h.name + " ##");
					lines.add(bar);
				} else if(e instanceof Option opt) {
					opt.comment.forEach(commentLine -> lines.add(commentLine.isEmpty() ? "" : "# " + commentLine));
					lines.add(opt.key + " = " + values.get(opt.key));
				}
				lines.add("");
			}
			lines.remove(lines.size() - 1); //snip extra blank line
			Files.write(path, lines);
		}
		
		@Override
		public int getInt(String name) {
			return Integer.parseInt(values.get(name));
		}
		
		@Override
		public double getDouble(String name) {
			return Double.parseDouble(values.get(name));
		}
		
		@Override
		public String getString(String name) {
			return values.get(name);
		}
		
		@Override
		public Enum<?> getEnum(String name) {
			//TODO oops will need to rearchitect
			return null;
		}
		
		@Override
		public boolean getBoolean(String name) {
			return Boolean.parseBoolean(name);
		}
		
		@Override
		public void installConfigLoadListener(Consumer<PlatformConfig> action) {
			if(Packages.instance.plat instanceof FabricPlatformSupport f) {
				//TODO
			}
		}
	}
}
