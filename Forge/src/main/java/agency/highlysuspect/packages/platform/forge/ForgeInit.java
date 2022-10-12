package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod("packages")
public class ForgeInit extends Packages {
	//Idk where else to put this
	private static final String NET_VERSION = "0";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(id("n"), () -> NET_VERSION, NET_VERSION::equals, NET_VERSION::equals);
	
	public ForgeInit() {
		super(new ForgePlatformSupport());
		
		earlySetup();
		
		if(FMLEnvironment.dist == Dist.CLIENT) {
			try {
				Class.forName("agency.highlysuspect.packages.platform.forge.client.ForgeClientInit").getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Packages had a problem initializing ForgeClientInit", e);
			}
		}
	}
}
