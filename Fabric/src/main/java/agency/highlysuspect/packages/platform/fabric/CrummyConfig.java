package agency.highlysuspect.packages.platform.fabric;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.config.ConfigProperty;
import agency.highlysuspect.packages.config.ConfigSchema;
import agency.highlysuspect.packages.config.CookedConfig;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CrummyConfig implements CookedConfig {
	public CrummyConfig(ConfigSchema schema, Path path) {
		this.schema = schema;
		this.path = path;
	}
	
	private final ConfigSchema schema;
	private final Path path;
	private final Logger log = Packages.LOGGER;
	
	private final Map<ConfigProperty<?>, Object> parsedValues = new IdentityHashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigProperty<T> key) {
		return (T) parsedValues.computeIfAbsent(key, ConfigProperty::defaultValue);
	}
	
	@Override
	public boolean refresh() {
		try {
			parse();
			write();
			return true;
		} catch (Exception e) {
			log.error("Problem loading config at " + path + ": " + e.getMessage(), e);
			return false;
		}
	}
	
	public void parse() throws IOException {
		//CrummyConfig#get() has "get-or-default" semantics - it's fine to leave the map empty
		if(Files.notExists(path)) return;
		
		parsedValues.clear();
		
		Map<String, ConfigProperty<?>> props = schema.propertiesByName();
		
		Iterator<String> lineserator = Files.readAllLines(path, StandardCharsets.UTF_8).iterator();
		int lineNo = 0;
		while(lineserator.hasNext()) {
			lineNo++;
			String line = lineserator.next().trim();
			if(line.isEmpty() || line.startsWith("#")) continue;
			
			int colonIdx = line.indexOf(':');
			if(colonIdx == -1) {
				//Try an equal sign instead; the previous (pre-3.4.1) config format used one
				colonIdx = line.indexOf('=');
				
				if(colonIdx == -1) {
					log.error("On line {} of {}, there's no colon to split a key-value pair.", lineNo, path);
					continue;
				}
			}
			String key = line.substring(0, colonIdx).trim();
			String value = line.substring(colonIdx + 1).trim();
			
			ConfigProperty<?> prop = props.get(key);
			if(prop == null) {
				log.error("On line {} of {}, there's no known option named '{}'.", lineNo, path, key);
				continue;
			}
			
			parsedValues.put(prop, parse(lineNo, prop, value));
		}
	}
	
	private <T> T parse(int lineNo, ConfigProperty<T> prop, String value) {
		Optional<T> parsedWrapped = prop.parse(value);
		if(parsedWrapped.isEmpty()) {
			log.error("On line {} of {}, option '{}' failed to parse. Defaulting to {}.", lineNo, path, prop.name(), prop.write(prop.defaultValue()));
			return prop.defaultValue();
		}
		
		T parsed = parsedWrapped.get();
		if(!prop.validate(parsed)) {
			log.error("On line {} of {}, option '{}' did not pass validation. Defaulting to {}.", lineNo, path, prop.name(), prop.write(prop.defaultValue()));
			return prop.defaultValue();
		}
		
		return parsed;
	}
	
	public void write() throws IOException {
		List<String> out = new ArrayList<>();
		
		schema.accept(new ConfigSchema.Visitor() {
			@Override
			public void visitSection(String section) {
				//String bar = "#".repeat(section.length() + 6); //Not in Java 8
				@SuppressWarnings("SuspiciousRegexArgument") //i really do want to replace every character
				String bar = "######" + section.replaceAll(".", "#");
				out.add(bar);
				out.add("## " + section + " ##");
				out.add(bar);
				out.add("");
			}
			
			@Override
			public <T> void visitOption(ConfigProperty<T> option) {
				for(String s : option.comment()) out.add("# " + s);
				
				if(!option.name().equals("configVersion")) { //silly special-case
					T defaultValue = option.defaultValue();
					String writtenDefaultValue = option.write(defaultValue);
					if(writtenDefaultValue.isEmpty()) writtenDefaultValue = "<empty>";
					out.add("# Default: " + writtenDefaultValue);
				}
				
				T currentValue = get(option);
				out.add(option.name() + ": " + option.write(currentValue));
				
				out.add("");
			}
		});
		
		if(path.getParent() != null) Files.createDirectories(path.getParent()); //can be null if it's at the fs root lol
		Files.write(path, out, StandardCharsets.UTF_8);
	}
	
	public record Bakery(Path path) implements ConfigSchema.Bakery {
		@Override
		public CookedConfig cook(ConfigSchema schema) {
			return new CrummyConfig(schema, path);
		}
	}
}
