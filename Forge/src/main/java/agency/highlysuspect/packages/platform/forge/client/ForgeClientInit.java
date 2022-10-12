package agency.highlysuspect.packages.platform.forge.client;

import agency.highlysuspect.packages.client.PackagesClient;

public class ForgeClientInit extends PackagesClient {
	public ForgeClientInit() {
		super(new ForgeClientPlatformSupport());
		
		earlySetup();
	}
}
