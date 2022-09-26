package agency.highlysuspect.packages.platform.fabric.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.client.model.AbstractPackageModel;
import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.ClientPlatformSupport;
import agency.highlysuspect.packages.platform.PlatformSupport;
import agency.highlysuspect.packages.platform.fabric.client.model.FrapiBakedQuadPackageMakerModel;
import agency.highlysuspect.packages.platform.fabric.client.model.FrapiBakedQuadPackageModel;
import agency.highlysuspect.packages.platform.fabric.client.model.FrapiMeshPackageMakerModel;
import agency.highlysuspect.packages.platform.fabric.client.model.FrapiMeshPackageModel;
import io.netty.buffer.Unpooled;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FabricClientPlatformSupport implements ClientPlatformSupport {
	@Override
	public void bakeSpritesOnto(ResourceLocation atlasTexture, ResourceLocation... sprites) {
		ClientSpriteRegistryCallback.event(atlasTexture).register((tex, reg) -> {
			for(ResourceLocation sprite : sprites) reg.register(sprite);
		});
	}
	
	@Override
	public <T extends BlockEntity> void setBlockEntityRenderer(PlatformSupport.RegistryHandle<? extends BlockEntityType<T>> type, BlockEntityRendererProvider<? super T> renderer) {
		BlockEntityRendererRegistry.register(type.get(), renderer);
	}
	
	@Override
	public void setRenderType(PlatformSupport.RegistryHandle<? extends Block> block, RenderType type) {
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
			if(AbstractPackageModel.PACKAGE_BLOCK_SPECIAL.equals(id) || AbstractPackageModel.PACKAGE_ITEM_SPECIAL.equals(id)) {
				if(packageModel == null) packageModel = createPackageModel(ctx);
				return packageModel;
			}
			
			if(AbstractPackageModel.PACKAGE_MAKER_BLOCK_SPECIAL.equals(id) || AbstractPackageModel.PACKAGE_MAKER_ITEM_SPECIAL.equals(id)) {
				if(packageMakerModel == null) packageMakerModel = createPackageMakerModel(ctx);
				return packageMakerModel;
			}
			
			return null;
		});
		
		//I'm pretty sure this is safe?
		Packages.instance.plat.installResourceReloadListener(mgr -> {
			packageModel = null;
			packageMakerModel = null;
		}, Packages.id("dump_caches"), PackType.CLIENT_RESOURCES);
	}
	
	private UnbakedModel createPackageModel(ModelProviderContext ctx) {
		return switch(Packages.instance.config.meshBackend) {
			case FRAPI_MESH -> new FrapiMeshPackageModel();
			case FRAPI_BAKEDQUAD -> new FrapiBakedQuadPackageModel();
			case SKIP -> ctx.loadModel(Packages.id("block/package")); //load json model directly
		};
	}
	
	private UnbakedModel createPackageMakerModel(ModelProviderContext ctx) {
		return switch(Packages.instance.config.meshBackend) {
			case FRAPI_MESH -> new FrapiMeshPackageMakerModel();
			case FRAPI_BAKEDQUAD -> new FrapiBakedQuadPackageMakerModel();
			case SKIP -> ctx.loadModel(Packages.id("block/package_maker"));
		};
	}
	
	//networking
	
	@Override
	public void sendActionPacket(ActionPacket packet) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		packet.write(buf);
		ClientPlayNetworking.send(ActionPacket.LONG_ID, buf);
	}
}
