package agency.highlysuspect.packages.platform.fabric.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.client.PackagesClient;
import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.RegistryHandle;
import agency.highlysuspect.packages.platform.client.ClientPlatformConfig;
import agency.highlysuspect.packages.platform.client.ClientsideHoldLeftClickCallback;
import agency.highlysuspect.packages.platform.client.ClientsideUseBlockCallback;
import agency.highlysuspect.packages.platform.client.EarlyClientsideLeftClickCallback;
import agency.highlysuspect.packages.platform.client.MyScreenConstructor;
import agency.highlysuspect.packages.platform.fabric.client.model.FrapiMeshPackageMakerModel;
import agency.highlysuspect.packages.platform.fabric.client.model.FrapiMeshPackageModel;
import agency.highlysuspect.packages.platform.fabric.compat.frex.FrexCompat;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FabricClientInit extends PackagesClient implements ClientModInitializer {
	public FabricClientInit() {
		super();
	}
	
	@Override
	public void onInitializeClient() {
		earlySetup();
		FrexCompat.onInitializeClient();
	}
	
	@Override
	public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerMenuScreen(RegistryHandle<MenuType<T>> type, MyScreenConstructor<T, U> cons) {
		MenuScreens.register(type.get(), cons::create);
	}
	
	@Override
	public void bakeSpritesOnto(ResourceLocation atlasTexture, ResourceLocation... sprites) {
		ClientSpriteRegistryCallback.event(atlasTexture).register((tex, reg) -> {
			for(ResourceLocation sprite : sprites) reg.register(sprite);
		});
	}
	
	@Override
	public <T extends BlockEntity> void setBlockEntityRenderer(RegistryHandle<? extends BlockEntityType<T>> type, BlockEntityRendererProvider<? super T> renderer) {
		BlockEntityRendererRegistry.register(type.get(), renderer);
	}
	
	@Override
	public void setRenderType(RegistryHandle<? extends Block> block, RenderType type) {
		BlockRenderLayerMap.INSTANCE.putBlock(block.get(), type);
	}
	
	public final Event<EarlyClientsideLeftClickCallback> EARLY_LEFT_CLICK_EVENT = EventFactory.createArrayBacked(EarlyClientsideLeftClickCallback.class,
		listeners -> (player, world, pos, direction) -> {
			for (EarlyClientsideLeftClickCallback event : listeners) {
				if(event.interact(player, world, pos, direction)) return true;
			}
			return false;
		}
	);
	
	@Override
	public void installEarlyClientsideLeftClickCallback(EarlyClientsideLeftClickCallback callback) {
		EARLY_LEFT_CLICK_EVENT.register(callback);
	}
	
	@Override
	public void installClientsideHoldLeftClickCallback(ClientsideHoldLeftClickCallback callback) {
		AttackBlockCallback.EVENT.register(callback::interact);
	}
	
	@Override
	public void installClientsideUseBlockCallback(ClientsideUseBlockCallback callback) {
		UseBlockCallback.EVENT.register(callback::interact);
	}
	
	//modelshit !
	
	public static UnbakedModel packageModel;
	public static UnbakedModel packageMakerModel;
	
	@Override
	public void setupCustomModelLoaders() {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(res -> (id, ctx) -> {
			if(FrapiMeshPackageModel.BLOCK_SPECIAL.equals(id) || FrapiMeshPackageModel.ITEM_SPECIAL.equals(id)) {
				if(packageModel == null) packageModel = createPackageModel(ctx);
				return packageModel;
			}
			
			if(FrapiMeshPackageMakerModel.BLOCK_SPECIAL.equals(id) || FrapiMeshPackageMakerModel.ITEM_SPECIAL.equals(id)) {
				if(packageMakerModel == null) packageMakerModel = createPackageMakerModel(ctx);
				return packageMakerModel;
			}
			
			return null;
		});
		
		//I'm pretty sure this is safe?
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return Packages.id("dump-models");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				packageModel = null;
				packageMakerModel = null;
			}
		});
	}
	
	private UnbakedModel createPackageModel(ModelProviderContext ctx) {
		return new FrapiMeshPackageModel();
	}
	
	private UnbakedModel createPackageMakerModel(ModelProviderContext ctx) {
		return new FrapiMeshPackageMakerModel();
	}
	
	//networking
	
	@Override
	public void sendActionPacket(ActionPacket packet) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		packet.write(buf);
		ClientPlayNetworking.send(ActionPacket.LONG_ID, buf);
	}
	
	//configshit
	
	@Override
	public ClientPlatformConfig makeClientPlatformConfig() {
		return new FabricClientPlatformConfig();
	}
}
