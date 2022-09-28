package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import net.minecraftforge.fml.common.Mod;

@Mod("packages")
public class ForgeInit extends Packages {
	public ForgeInit() {
		super(new ForgePlatformSupport());
		
		earlySetup();
	}
}
