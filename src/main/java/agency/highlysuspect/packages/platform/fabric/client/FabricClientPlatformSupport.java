package agency.highlysuspect.packages.platform.fabric.client;

import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.ClientPlatformSupport;
import agency.highlysuspect.packages.platform.PlatformSupport;
import agency.highlysuspect.packages.platform.fabric.client.model.FrapiPackageMakerModel;
import agency.highlysuspect.packages.platform.fabric.client.model.FrapiPackageModel;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
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
	
	@Override
	public UnbakedModel createPackageModel() {
		return new FrapiPackageModel();
	}
	
	@Override
	public UnbakedModel createPackageMakerModel() {
		return new FrapiPackageMakerModel();
	}
	
	//networking
	
	@Override
	public void sendActionPacket(ActionPacket packet) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		packet.write(buf);
		ClientPlayNetworking.send(ActionPacket.LONG_ID, buf);
	}
}
