package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.CommonPlatformConfig;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ForgePlatformSupport implements PlatformSupport {
	public ForgePlatformSupport() {
		MinecraftForge.EVENT_BUS.addListener(this::actuallyRegisterDispenserBehaviors);
	}
	
	private final Map<Registry<?>, DeferredRegister<?>> deferredRegistries = new HashMap<>();
	
	@SuppressWarnings("unchecked") //Go directly to generics hell. Do not pass Go or collect $200.
	private <T> DeferredRegister<T> getDeferredRegister(Registry<?> reg) {
		IForgeRegistry<T> registrySpicy = RegistryManager.ACTIVE.getRegistry(((Registry<T>) reg).key());
		
		return (DeferredRegister<T>) deferredRegistries.computeIfAbsent(reg, __ -> {
			DeferredRegister<T> deferred = DeferredRegister.create(registrySpicy, Packages.MODID);
			deferred.register(FMLJavaModLoadingContext.get().getModEventBus());
			return deferred;
		});
	}
	
	@Override
	public <T> RegistryHandle<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> thingMaker) {
		if(!id.getNamespace().equals(Packages.MODID)) throw new IllegalArgumentException("Forge enforces one modid per DeferredRegister");
		
		RegistryObject<T> obj = (getDeferredRegister(registry)).register(id.getPath(), thingMaker);
		return new RegistryObjectRegistryHandle<>(obj);
	}
	
	@SuppressWarnings("ClassCanBeRecord")
	private static class RegistryObjectRegistryHandle<T> implements RegistryHandle<T> {
		RegistryObjectRegistryHandle(RegistryObject<T> obj) {
			this.obj = obj;
		}
		
		private final RegistryObject<T> obj;
		
		@Override
		public T get() {
			return obj.get();
		}
		
		@Override
		public ResourceLocation getId() {
			return obj.getId();
		}
	}
	
	@SuppressWarnings("ConstantConditions") //null DFU type
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		return new BlockEntityType<>(factory::create, Set.of(blocks), null); //Access widened by forge
	}
	
	@Override
	public CreativeModeTab makeCreativeModeTab(ResourceLocation id, Supplier<ItemStack> icon) {
		return new CreativeModeTab(id.getNamespace() + "." + id.getPath()) {
			@Override
			public ItemStack makeIcon() {
				return icon.get();
			}
		};
	}
	
	private final Map<RegistryHandle<? extends ItemLike>, DispenseItemBehavior> dispenseBehaviorsToRegister = new HashMap<>();
	
	@Override
	public void registerDispenserBehavior(RegistryHandle<? extends ItemLike> item, DispenseItemBehavior behavior) {
		dispenseBehaviorsToRegister.put(item, behavior);
	}
	
	private void actuallyRegisterDispenserBehaviors(FMLCommonSetupEvent e) {
		dispenseBehaviorsToRegister.forEach((handle, behavior) -> DispenserBlock.registerBehavior(handle.get(), behavior));
	}
	
	@Override
	public <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier) {
		//Looks the same as on FabricPlatformSupport but it's private in mojang source so i can't use it there without access widening
		return new MenuType<>(supplier::create);
	}
	
	@Override
	public void registerActionPacketHandler() {
		ForgeInit.CHANNEL.registerMessage(ActionPacket.SHORT_ID, ActionPacket.class, ActionPacket::write, ActionPacket::read, (action, ctxSupplier) -> {
			NetworkEvent.Context ctx = ctxSupplier.get();
			//Forge uses the same networkstuff for client -> server and server -> client packets.
			//This is a client -> server packet, so of course the sender is a nonnull player, but Forge doesn't statically know that
			ServerPlayer player = ctx.getSender(); if(player == null) return;
			action.handle(player);
			ctx.setPacketHandled(true);
		});
	}
	
	@Override
	public CommonPlatformConfig makePlatformConfig() {
		return new ForgeCommonPlatformConfig();
	}
}
