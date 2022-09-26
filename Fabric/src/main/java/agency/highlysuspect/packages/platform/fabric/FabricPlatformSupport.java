package agency.highlysuspect.packages.platform.fabric;

import agency.highlysuspect.packages.platform.PlatformSupport;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FabricPlatformSupport implements PlatformSupport {
	@Override
	public <T> RegistryHandle<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> thingMaker) {
		//It's safe to intialize and register the object right away on Fabric.
		T thing = thingMaker.get();
		Registry.register(registry, id, thing);
		
		//Return a handle to it.
		return new ImmediateRegistryHandle<>(thing, id);
	}
	
	@SuppressWarnings("ClassCanBeRecord")
	static class ImmediateRegistryHandle<T> implements RegistryHandle<T> {
		public ImmediateRegistryHandle(T thing, ResourceLocation id) {
			this.thing = thing;
			this.id = id;
		}
		
		private final T thing;
		private final ResourceLocation id;
		
		@Override
		public T get() {
			return thing;
		}
		
		@Override
		public ResourceLocation getId() {
			return id;
		}
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		return FabricBlockEntityTypeBuilder.create(factory::create, blocks).build();
	}
	
	@Override
	public CreativeModeTab makeCreativeModeTab(ResourceLocation id, Supplier<ItemStack> icon) {
		return FabricItemGroupBuilder.build(id, icon);
	}
	
	@Override
	public void registerDispenserBehavior(RegistryHandle<? extends ItemLike> item, DispenseItemBehavior behavior) {
		DispenserBlock.registerBehavior(item.get(), behavior);
	}
	
	@Override
	public <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier) {
		return new MenuType<>(supplier::create);
	}
	
	@Override
	public void registerGlobalPacketHandler(ResourceLocation packetId, GlobalPacketHandler blah) {
		ServerPlayNetworking.registerGlobalReceiver(packetId, (server, player, handler, buf, resp) -> blah.handle(server, player, buf));
	}
	
	@Override
	public Path getConfigFolder() {
		return FabricLoader.getInstance().getConfigDir();
	}
	
	@Override
	public void installResourceReloadListener(Consumer<ResourceManager> listener, ResourceLocation name, PackType... types) {
		IdentifiableResourceReloadListener hello = new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return name;
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				listener.accept(resourceManager);
			}
		};
		
		for(PackType type : types) ResourceManagerHelper.get(type).registerReloadListener(hello);
	}
}
