package agency.highlysuspect.packages.config;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.net.PackageAction;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record PackageActionBinding(PackageAction action, boolean ctrl, boolean sneak, boolean alt, MainTrigger mainTrigger) implements Comparable<PackageActionBinding> {
	public enum MainTrigger {
		PUNCH, USE, UNDEFINED;
		
		@Override
		public String toString() {return name().toLowerCase(Locale.ROOT);}
	}
	
	public int specificity() {
		int s = 0;
		if(ctrl) s++;
		if(sneak) s++;
		if(alt) s++;
		return s;
	}
	
	@Override
	public int compareTo(@NotNull PackageActionBinding o) {
		//minus sign so that the "natural order" comparing puts more specific bindings first
		return -Integer.compare(specificity(), o.specificity());
	}
	
	public String asString() {
		if(mainTrigger == MainTrigger.UNDEFINED) return "";
		
		List<String> parts = new ArrayList<>();
		if(ctrl) parts.add("ctrl");
		if(sneak) parts.add("sneak");
		if(alt) parts.add("alt");
		if(mainTrigger == MainTrigger.PUNCH) parts.add("punch");
		if(mainTrigger == MainTrigger.USE) parts.add("use");
		return String.join("-", parts);
	}
	
	public static PackageActionBinding fromString(PackageAction action, String s) {
		PackageActionBinding.Builder b = new PackageActionBinding.Builder(action);
		if(s.isEmpty()) return b.build();
		
		for(String option : s.split("-")) {
			switch(option.toLowerCase(Locale.ROOT)) {
				case "ctrl" -> b.ctrl = true;
				case "sneak", "shift" -> b.sneak = true;
				case "alt" -> b.alt = true;
				case "punch", "attack", "left" -> b.mainTrigger = MainTrigger.PUNCH;
				case "use", "right", "activate" -> b.mainTrigger = MainTrigger.USE;
				default -> Packages.LOGGER.warn("Unknown PackageActionBinding component '" + option + "', skipping");
			}
		}
		return b.build();
	}
	
	public static class Builder {
		public Builder(PackageAction action) {
			this.action = action;
		}
		
		public final PackageAction action;
		public boolean ctrl, sneak, alt;
		public MainTrigger mainTrigger = MainTrigger.UNDEFINED;
		
		public Builder ctrl() { this.ctrl = true; return this; }
		public Builder sneak() { this.sneak = true; return this; }
		public Builder alt() { this.alt = true; return this; }
		public Builder punch() { this.mainTrigger = MainTrigger.PUNCH; return this; }
		public Builder use() { mainTrigger = MainTrigger.USE; return this; }
		
		public PackageActionBinding build() {
			return new PackageActionBinding(action, ctrl, sneak, alt, mainTrigger);
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME) public @interface For { PackageAction value(); }
	public static class SerializerDeserializer implements ConfigShape2.SerializerDeserializer<PackageActionBinding> {
		@Override
		public PackageActionBinding parse(Field field, String value) {
			return PackageActionBinding.fromString(field.getAnnotation(For.class).value(), value);
		}
		
		@Override
		public String write(PackageActionBinding value) {
			return value.asString();
		}
	}
}
