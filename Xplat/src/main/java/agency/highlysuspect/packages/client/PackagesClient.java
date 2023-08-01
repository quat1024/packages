package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PBlockEntityTypes;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.config.ConfigSchema;
import agency.highlysuspect.packages.config.CookedConfig;
import agency.highlysuspect.packages.menu.PMenuTypes;
import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.net.PackageAction;
import agency.highlysuspect.packages.platform.RegistryHandle;
import agency.highlysuspect.packages.platform.client.MyScreenConstructor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class PackagesClient {
	public static PackagesClient instance;
	
	public CookedConfig config = CookedConfig.Unset.INSTANCE;
	
	//Bindings, parsed from the config, and sorted such that the more specific ones are at the front of the list
	//(so the game checks ctrl-shift-alt, before ctrl-alt, before alt, etc.)
	//Kind of a clumsy spot to put this
	public List<PackageActionBinding> sortedBindings = new ArrayList<>();
	
	public PackagesClient() {
		if(instance != null) throw new IllegalStateException("Initializing PackagesClient twice!");
		instance = this;
	}
	
	public void earlySetup() {
		Packages.instance.proxy = new ClientProxy();
		
		config = clientConfigBakery().cook(PropsClient.visit(new ConfigSchema()));
		
		setupCustomModelLoaders();
		
		registerMenuScreen(PMenuTypes.PACKAGE_MAKER, PackageMakerScreen::new);
		
		setBlockEntityRenderer(PBlockEntityTypes.PACKAGE, PackageRenderer::new);
		setRenderType(PBlocks.PACKAGE_MAKER, RenderType.cutoutMipped());
	}
	
	public void refreshConfig() {
		config.refresh();
		
		sortedBindings = new ArrayList<>(Arrays.asList(
			PackageActionBinding.fromString(PackageAction.INSERT_ONE, config.get(PropsClient.INSERT_ONE_BINDING_UNPARSED)),
			PackageActionBinding.fromString(PackageAction.INSERT_STACK, config.get(PropsClient.INSERT_STACK_BINDING_UNPARSED)),
			PackageActionBinding.fromString(PackageAction.INSERT_ALL, config.get(PropsClient.INSERT_ALL_BINDING_UNPARSED)),
			PackageActionBinding.fromString(PackageAction.TAKE_ONE, config.get(PropsClient.TAKE_ONE_BINDING_UNPARSED)),
			PackageActionBinding.fromString(PackageAction.TAKE_STACK, config.get(PropsClient.TAKE_STACK_BINDING_UNPARSED)),
			PackageActionBinding.fromString(PackageAction.TAKE_ALL, config.get(PropsClient.TAKE_ALL_BINDING_UNPARSED))
		));
		sortedBindings.sort(Comparator.naturalOrder());
	}
	
	public abstract <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerMenuScreen(RegistryHandle<MenuType<T>> type, MyScreenConstructor<T, U> cons);
	public abstract <T extends BlockEntity> void setBlockEntityRenderer(RegistryHandle<? extends BlockEntityType<T>> type, BlockEntityRendererProvider<? super T> renderer);
	public abstract void setRenderType(RegistryHandle<? extends Block> block, RenderType type);
	public abstract void setupCustomModelLoaders();
	public abstract void sendActionPacket(ActionPacket packet);
	
	public abstract ConfigSchema.Bakery clientConfigBakery();
}
