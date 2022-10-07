package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PBlockEntityTypes;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.container.PMenuTypes;
import agency.highlysuspect.packages.platform.ClientPlatformSupport;
import net.minecraft.client.renderer.RenderType;

public abstract class PackagesClient {
	public static PackagesClient instance;
	
	public final ClientPlatformSupport plat;
	public PackagesClientConfig config = null;
	
	public PackagesClient(ClientPlatformSupport plat) {
		if(instance != null) throw new IllegalStateException("Initializing PackagesClient twice!");
		instance = this;
		
		this.plat = plat;
		plat.makeClientPlatformConfig().registerAndLoadAndAllThatJazz();
	}
	
	public void earlySetup() {
		plat.setupCustomModelLoaders();
		
		plat.registerMenuScreen(PMenuTypes.PACKAGE_MAKER, PackageMakerScreen::new);
		PackageMakerScreen.initIcons(plat);
		
		PClientBlockEventHandlers.onInitializeClient(plat);
		
		plat.setBlockEntityRenderer(PBlockEntityTypes.PACKAGE, PackageRenderer::new);
		plat.setRenderType(PBlocks.PACKAGE_MAKER, RenderType.cutoutMipped());
	}
}
