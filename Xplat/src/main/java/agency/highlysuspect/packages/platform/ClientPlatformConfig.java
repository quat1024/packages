package agency.highlysuspect.packages.platform;

import agency.highlysuspect.packages.client.PackageActionBinding;

public interface ClientPlatformConfig {
	PackageActionBinding insertOneBinding();
	PackageActionBinding insertStackBinding();
	PackageActionBinding insertAllBinding();
	PackageActionBinding takeOneBinding();
	PackageActionBinding takeStackBinding();
	PackageActionBinding takeAllBinding();
	
	int punchRepeat();
	double fontVerticalShift();
	boolean cacheMeshes();
	boolean swapRedAndBlue();
	boolean frexSupport();
	
	///
	
	/**
	 * Yes it's a magic does-it-all method, but splitting it up into several pieces ended up being more confusing in practice.
	 *
	 * - Register the config with whatever platform config service exists
	 * - Create the actual config file if it doesn't exist
	 * - Load the initial state of the config and write it to Packages.instance.config
	 * - Set it up so when a resource reload or platform-specific config file changes, Packages.instance.config changes too
	 */
	void registerAndLoadAndAllThatJazz();
}
