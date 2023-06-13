package agency.highlysuspect.packages.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigSchema {
	private final Map<String, List<ConfigProperty<?>>> propertiesByCategory = new LinkedHashMap<>();
	
	//builder style api
	
	private static final String SECTIONLESS = "\ud83d\udc09";
	private String currentSection = SECTIONLESS;
	
	public void section(String sectionName) {
		currentSection = sectionName == null ? SECTIONLESS : sectionName;
	}
	
	public List<ConfigProperty<?>> getSection(String sectionName) {
		return propertiesByCategory.computeIfAbsent(sectionName == null ? SECTIONLESS : sectionName, __ -> new ArrayList<>());
	}
	
	public void option(ConfigProperty<?>... options) {
		getSection(currentSection).addAll(Arrays.asList(options));
	}
	
	public void section(String sectionName, ConfigProperty<?>... options) {
		section(sectionName);
		option(options);
	}
	
	//visitor pattern api
	
	public interface Visitor {
		default void visitSection(String section) {}
		default <T> void visitOption(ConfigProperty<T> option) {}
	}
	
	public void accept(Visitor visitor) {
		propertiesByCategory.forEach((section, options) -> {
			if(!SECTIONLESS.equals(section)) visitor.visitSection(section);
			options.forEach(visitor::visitOption);
		});
	}
	
	//handy
	public Map<String, ConfigProperty<?>> propertiesByName() {
		Map<String, ConfigProperty<?>> result = new LinkedHashMap<>();
		accept(new Visitor() {
			@Override
			public <T> void visitOption(ConfigProperty<T> option) {
				result.put(option.name(), option);
			}
		});
		
		return result;
	}
	
	//bakery
	
	public interface Bakery {
		CookedConfig cook(ConfigSchema schema);
	}
}
