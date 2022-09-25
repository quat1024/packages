package agency.highlysuspect.packages.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface PlatformSupport {
	//Registration
	<T> RegistryHandle<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> thingMaker);
	interface RegistryHandle<T> extends Supplier<T> { ResourceLocation getId(); }
	
	<T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks);
	interface BlockEntityFactory<T extends BlockEntity> {
		T create(BlockPos blockPos, BlockState blockState);
	}
	
	CreativeModeTab makeCreativeModeTab(ResourceLocation id, Supplier<ItemStack> icon);
	void registerDispenserBehavior(RegistryHandle<? extends ItemLike> item, DispenseItemBehavior behavior);
	
	//Networking
	void registerGlobalPacketHandler(ResourceLocation packetId, GlobalPacketHandler blah);
	interface GlobalPacketHandler {
		void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buf);
	}
	
	//Misc
	Path getConfigFolder();
	void installResourceReloadListener(Consumer<ResourceManager> listener, ResourceLocation name, PackType... types);
}
