package agency.highlysuspect.packages.config;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * based on paste from Auto Third Person, modified to hardcode writers and readers a little bit less
 */
public class ConfigShape2 {
	public sealed interface Element permits Heading, Option {}
	public static record Heading(String name) implements Element {}
	public static record Option<T>(String key, T defaultValue, List<String> comment, BiFunction<Field, String, T> parser, Function<T, String> writer, Consumer<T> validator, Field field) implements Element {
		@SuppressWarnings("unchecked")
		String getAndWriteErased(Object pojo) {
			try {
				return writer.apply((T) field.get(pojo));
			} catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
		}
		
		@SuppressWarnings("unchecked")
		void validateErased(Object thing) {
			validator.accept((T) thing);
		}
	}
	
	public static class ConfigParseException extends RuntimeException {
		public ConfigParseException(String message) { super(message); }
		public ConfigParseException(String message, Throwable cause) { super(message, cause); }
	}
	
	private final List<Element> elements = new ArrayList<>();
	private final Map<String, Option<?>> optionsByName = new HashMap<>();
	private final Map<Class<?>, SerializerDeserializer<?>> serdeByType = new HashMap<>();
	private final Map<String, SerializerDeserializer<?>> serdeByName = new HashMap<>();
	
	{
		//Here, I'll give you a few freebies
		installSerializer(String.class, SerializerDeserializer.makeSimple(x -> x, x -> x));
		installSerializer(Integer.class, SerializerDeserializer.withObjectToString(Integer::parseInt));
		installSerializer(Integer.TYPE, SerializerDeserializer.withObjectToString(Integer::parseInt));
		installSerializer(Boolean.class, SerializerDeserializer.withObjectToString(Boolean::parseBoolean));
		installSerializer(Boolean.TYPE, SerializerDeserializer.withObjectToString(Boolean::parseBoolean));
	}
	
	public void add(Element element) {
		elements.add(element);
		if(element instanceof Option opt) optionsByName.put(opt.key, opt);
	}
	
	public <T> ConfigShape2 installSerializer(Class<T> classs, SerializerDeserializer<T> serde) {
		serdeByType.put(classs, serde);
		return this;
	}
	
	public <T> ConfigShape2 installNamedSerializer(String name, SerializerDeserializer<T> serde) {
		serdeByName.put(name, serde);
		return this;
	}
	
	// writing and reading into a pojo //
	
	@SuppressWarnings("DuplicateExpressions")
	public List<String> write(Object pojo) {
		List<String> lines = new ArrayList<>();
		for(Element e : elements) {
			if(e instanceof Heading h) {
				lines.add("#".repeat(h.name.length() + 6));
				lines.add("## " + h.name + " ##");
				lines.add("#".repeat(h.name.length() + 6));
			} else if(e instanceof Option<?> opt) {
				opt.comment.forEach(commentLine -> lines.add(commentLine.isEmpty() ? "" : "# " + commentLine)); //skip # gutter for empty comment lines
				lines.add(opt.key + " = " + opt.getAndWriteErased(pojo));
			}
			lines.add("");
		}
		lines.remove(lines.size() - 1); //snip extra blank line
		return lines;
	}
	
	public <P> P readInto(List<String> file, P pojo) {
		for(int line = 0; line < file.size(); line++) {
			try {
				String s = file.get(line).trim();
				if(s.isBlank() || s.startsWith("#")) continue;
				
				//Split
				int eqIndex = s.indexOf('=');
				if(eqIndex == -1) throw new ConfigParseException("there is no equal sign to split a key/value pair.");
				
				//Parse key
				String keyStr = s.substring(0, eqIndex).trim();
				Option<?> opt = optionsByName.get(keyStr);
				//Good place to extend if you want more functionality: config upgrade system instead of crashing lol
				if(opt == null) throw new ConfigParseException("there is no option named '" + keyStr + "'.");
				
				//Parse value
				String valueStr = s.substring(eqIndex + 1).trim();
				Object result;
				try {
					result = opt.parser.apply(opt.field, valueStr);
				} catch (RuntimeException e) {
					throw new ConfigParseException("unable to parse '" + valueStr + "' for option " + keyStr + ".", e);
				}
				
				//Validate value
				opt.validateErased(result);
				
				//Store
				opt.field.set(pojo, result);
			} catch (ConfigParseException e) {
				//Include the line number, also hopefully funge into something that will be of more value to nonprogrammers
				throw new ConfigParseException("On line " + (line + 1) + ", " + e.getMessage(), e.getCause());
			} catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
		}
		return pojo;
	}
	
	// file handling //
	
	public void writeFile(Path path, Object pojo) throws IOException {
		Files.createDirectories(path.getParent());
		Files.write(path, write(pojo));
	}
	
	public <P> P readFromOrCreateFile(Path path, P pojoContainingDefaultValues) throws IOException {
		P result = Files.exists(path) ? readInto(Files.readAllLines(path), pojoContainingDefaultValues) : pojoContainingDefaultValues;
		writeFile(path, result); //always write the file back. ensures all the comments etc are updated
		
		return result;
	}
	
	// annotation magic //
	
	//Easily the most kludgy part. You could (theoretically) create a ConfigShape by repeatedly calling `add` yourself.
	//A bunch of people, including me, like being able to make them from annotations though.
	//This could be made less messy/special-casey but it'd be more code.
	
	/** Add to a field to break a new config section before it. */
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Section { String value(); }
	/** Don't write the 'default value' comment. Useful for secret config options, like a "config version" param */
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface SkipDefault {}
	/** Include a comment. Specifying multiple strings will concatenate them with newlines. */
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Comment { String[] value(); }
	/** Use a specifically named SerializerDeserializer instead of guessing from the type. */
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Use { String value(); }
	
	/** Assert that integers are at least this. */
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface AtLeast { int value(); }
	
	public interface SerializerDeserializer<T> {
		T parse(Field field, String value);
		String write(T value);
		
		static <T> SerializerDeserializer<T> makeSimple(Function<String, T> parser, Function<T, String> writer) {
			return new SerializerDeserializer<>() {
				@Override public T parse(Field field, String value) { return parser.apply(value); }
				@Override public String write(T value) { return writer.apply(value); }
			};
		}
		
		static <T> SerializerDeserializer<T> withObjectToString(Function<String, T> parser) {
			return makeSimple(parser, Objects::toString);
		}
	}
	
	@SuppressWarnings("unchecked") //actually kind of dangerous lol
	public <T> SerializerDeserializer<T> getSerde(Field field) {
		Use use = field.getAnnotation(Use.class);
		if(use != null) return (SerializerDeserializer<T>) serdeByName.get(use.value());
		else return (SerializerDeserializer<T>) serdeByType.get(field.getType());
	}
	
	public ConfigShape2 readPropsFromPojo(Object pojoContainingDefaultSettings) {
		try {
			for(Field field : pojoContainingDefaultSettings.getClass().getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;
				field.setAccessible(true);
				
				String key = field.getName();
				Object defaultValue = field.get(pojoContainingDefaultSettings);
				SerializerDeserializer<Object> serde = getSerde(field);
				if(serde == null) throw new RuntimeException("No serde for field " + field);
				
				//Section headings. With this annotation scheme, they go before the first field of the section.
				//Headings are purely for decoration and do not create a new namespace of config values
				Section headingAnnotation = field.getAnnotation(Section.class);
				if(headingAnnotation != null) add(new Heading(headingAnnotation.value()));
				
				//Comment, including mention of the default value
				List<String> comment = new ArrayList<>();
				Comment commentAnnotation = field.getAnnotation(Comment.class);
				if(commentAnnotation != null) comment.addAll(List.of(commentAnnotation.value()));
				if(field.getAnnotation(SkipDefault.class) == null) comment.add("Default: " + serde.write(defaultValue));
				
				//Validator (TODO: bodge this less)
				Consumer<Object> validator = x -> {};
				AtLeast atLeastAnnotation = field.getAnnotation(AtLeast.class);
				if(atLeastAnnotation != null && (field.getType() == Integer.TYPE || field.getType() == Integer.class)) {
					int min = atLeastAnnotation.value();
					validator = t -> {
						if(min > ((Integer) t)) throw new ConfigParseException("The value for this option must be at least " + min + ", but it is set to " + t + ".");
					};
				}
				
				add(new Option<>(key, defaultValue, comment, serde::parse, serde::write, validator, field));
			}
		} catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
		return this;
	}
}
