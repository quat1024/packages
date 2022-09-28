package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("packages")
public class ForgeInit extends Packages {
	public ForgeInit() {
		super(new ForgePlatformSupport());
		
		earlySetup();
		
		if(FMLEnvironment.dist == Dist.CLIENT) {
			try {
				Class.forName("agency.highlysuspect.packages.platform.forge.client.ForgeClientInit").getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
