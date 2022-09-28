package agency.highlysuspect.packages.platform;

import agency.highlysuspect.packages.net.ActionPacket;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;

public interface ClientPlatformSupport {
	//Menustuff
	<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerMenuScreen(PlatformSupport.RegistryHandle<MenuType<T>> type, MyScreenConstructor<T, U> cons);
	interface MyScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> { U create(T var1, Inventory var2, Component var3); }
	
	//Rendering
	void bakeSpritesOnto(ResourceLocation atlasTexture, ResourceLocation... sprites);
	<T extends BlockEntity> void setBlockEntityRenderer(PlatformSupport.RegistryHandle<? extends BlockEntityType<T>> type, BlockEntityRendererProvider<? super T> renderer);
	void setRenderType(PlatformSupport.RegistryHandle<? extends Block> block, RenderType type);
	void setupCustomModelLoaders(); //place to hide all the weird bakedmodel stuff
	
	//Interactions
	void installEarlyClientsideLeftClickCallback(EarlyClientsideLeftClickCallback callback);
	interface EarlyClientsideLeftClickCallback { boolean interact(Player player, Level level, BlockPos pos, Direction direction); }
	void installClientsideHoldLeftClickCallback(ClientsideHoldLeftClickCallback callback);
	interface ClientsideHoldLeftClickCallback { InteractionResult interact(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction); }
	void installClientsideUseBlockCallback(ClientsideUseBlockCallback callback);
	interface ClientsideUseBlockCallback { InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult); }
	
	//Networking
	void sendActionPacket(ActionPacket packet);
}
