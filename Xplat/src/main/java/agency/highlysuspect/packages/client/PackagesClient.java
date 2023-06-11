package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PBlockEntityTypes;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.container.PMenuTypes;
import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.RegistryHandle;
import agency.highlysuspect.packages.platform.client.ClientPlatformConfig;
import agency.highlysuspect.packages.platform.client.ClientsideHoldLeftClickCallback;
import agency.highlysuspect.packages.platform.client.ClientsideUseBlockCallback;
import agency.highlysuspect.packages.platform.client.EarlyClientsideLeftClickCallback;
import agency.highlysuspect.packages.platform.client.MyScreenConstructor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class PackagesClient {
	public static PackagesClient instance;
	public PackagesClientConfig config = null;
	
	public PackagesClient() {
		if(instance != null) throw new IllegalStateException("Initializing PackagesClient twice!");
		instance = this;
		
		makeClientPlatformConfig().registerAndLoadAndAllThatJazz();
	}
	
	public void earlySetup() {
		Packages.instance.proxy = new ClientProxy();
		
		setupCustomModelLoaders();
		
		registerMenuScreen(PMenuTypes.PACKAGE_MAKER, PackageMakerScreen::new);
		PackageMakerScreen.initIcons();
		
		PClientBlockEventHandlers.onInitializeClient();
		
		setBlockEntityRenderer(PBlockEntityTypes.PACKAGE, PackageRenderer::new);
		setRenderType(PBlocks.PACKAGE_MAKER, RenderType.cutoutMipped());
	}
	
	public abstract <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerMenuScreen(RegistryHandle<MenuType<T>> type, MyScreenConstructor<T, U> cons);
	public abstract void bakeSpritesOnto(ResourceLocation atlasTexture, ResourceLocation... sprites);
	public abstract <T extends BlockEntity> void setBlockEntityRenderer(RegistryHandle<? extends BlockEntityType<T>> type, BlockEntityRendererProvider<? super T> renderer);
	public abstract void setRenderType(RegistryHandle<? extends Block> block, RenderType type);
	public abstract void setupCustomModelLoaders();
	public abstract void installEarlyClientsideLeftClickCallback(EarlyClientsideLeftClickCallback callback);
	public abstract void installClientsideHoldLeftClickCallback(ClientsideHoldLeftClickCallback callback);
	public abstract void installClientsideUseBlockCallback(ClientsideUseBlockCallback callback);
	public abstract void sendActionPacket(ActionPacket packet);
	public abstract ClientPlatformConfig makeClientPlatformConfig();
}
