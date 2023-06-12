package agency.highlysuspect.packages.config;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public interface ConfigProperty<T> {
	String name();
	List<String> comment();
	T defaultValue();
	String write(T thing);
	T parse(String s);
	
	default boolean validate(T thing) {
		return true; //seems good
	}
	
	//mainly as a utility for Forge
	@SuppressWarnings("unchecked")
	default boolean validateErased(Object thing) {
		if(thing == null || thing.getClass() != defaultValue().getClass()) return false;
		else return validate((T) thing);
	}
	
	//meh, kind of clumsy
	default ConfigProperty<T> withValidator(Predicate<T> test) {
		return new Validation<>(this, test);
	}
	
	static ConfigProperty<String> stringOpt(String name, String defaultValue, String... comment) {
		return new StringOpt(name, defaultValue, Arrays.asList(comment));
	}
	
	static ConfigProperty<Boolean> boolOpt(String name, boolean defaultValue, String... comment) {
		return new BoolOpt(name, defaultValue, Arrays.asList(comment));
	}
	
	static ConfigProperty<Integer> intOpt(String name, int defaultValue, String... comment) {
		return intOpt(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, comment);
	}
	
	static ConfigProperty<Integer> intOpt(String name, int defaultValue, int min, int max, String... comment) {
		return new IntOpt(name, defaultValue, Arrays.asList(comment), min, max);
	}
	
	//The code is concise because automatically-generated record getters partially implement the interface.
	
	record StringOpt(String name, String defaultValue, List<String> comment) implements ConfigProperty<String> {
		@Override
		public String write(String thing) {
			return thing;
		}
		
		@Override
		public String parse(String s) {
			return s.trim();
		}
	}
	
	record BoolOpt(String name, Boolean defaultValue, List<String> comment) implements ConfigProperty<Boolean> {
		@Override
		public String write(Boolean thing) {
			return Boolean.toString(thing);
		}
		
		@Override
		public Boolean parse(String s) {
			return Boolean.parseBoolean(s.trim());
		}
	}
	
	record IntOpt(String name, Integer defaultValue, List<String> comment, int min, int max) implements ConfigProperty<Integer> {
		@Override
		public String write(Integer thing) {
			return Integer.toString(thing);
		}
		
		@Override
		public Integer parse(String s) {
			try {
				return Integer.parseInt(s.trim());
			} catch (Exception e) {
				System.err.println("unable to parse option '" + name + "' as integer!");
				return min;
			}
		}
		
		@Override
		public boolean validate(Integer thing) {
			if(thing >= min && thing <= max) return true; //seems good
			
			System.err.println("option '" + name + "' is not within its bounds; defaulting to " + min);
			if(min != Integer.MIN_VALUE) System.err.println("\\-> it should be at least " + min);
			if(max != Integer.MAX_VALUE) System.err.println("\\-> it should be at most " + max);
			return false;
		}
	}
	
	//Not sure how 2 feel about this
	record Validation<T>(ConfigProperty<T> inner, Predicate<T> validator) implements ConfigProperty<T> {
		@Override public String name() {return inner.name();}
		@Override public List<String> comment() {return inner.comment();}
		@Override public T defaultValue() {return inner.defaultValue();}
		@Override public String write(T thing) {return inner.write(thing);}
		@Override public T parse(String s) {return inner.parse(s);}
		
		@Override
		public boolean validate(T thing) {
			return inner.validate(thing) && validator.test(thing);
		}
	}
}
