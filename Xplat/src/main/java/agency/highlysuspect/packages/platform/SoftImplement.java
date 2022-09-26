package agency.highlysuspect.packages.platform;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Copied from Botania, this annotation marks a method as belonging to an interface that cannot actually be implemented.
 * For example, the implementor is in the common sourceset, but the implementee is not.
 * 
 * These invariants must manually be checked when porting the mod.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface SoftImplement {
	String value();
}
