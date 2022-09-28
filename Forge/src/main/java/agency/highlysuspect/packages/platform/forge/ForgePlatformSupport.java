package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
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
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForgePlatformSupport implements PlatformSupport {
	private final Map<Registry<?>, DeferredRegister<?>> deferredRegistries = new HashMap<>();
	private final Map<RegistryHandle<? extends ItemLike>, DispenseItemBehavior> dispenseBehaviorsToRegister = new HashMap<>();
	
	public ForgePlatformSupport() {
		MinecraftForge.EVENT_BUS.addListener((FMLCommonSetupEvent e) -> actuallyRegisterDispenserBehaviors());
	}
	
	@SuppressWarnings({
		"unchecked", //casting magic
		"deprecation" //forge presumptiously deprecating all the vanilla registries
	})
	private <T extends IForgeRegistryEntry<T>> DeferredRegister<T> getDeferredRegister(Registry<?> reg) {
		IForgeRegistry<T> what;
		if(reg == Registry.BLOCK) {
			what = (IForgeRegistry<T>) ForgeRegistries.BLOCKS;
		} else if(reg == Registry.BLOCK_ENTITY_TYPE) {
			what = (IForgeRegistry<T>) ForgeRegistries.BLOCK_ENTITIES;
		} else if(reg == Registry.ITEM) {
			what = (IForgeRegistry<T>) ForgeRegistries.ITEMS;
		} else if(reg == Registry.MENU) {
			what = (IForgeRegistry<T>) ForgeRegistries.CONTAINERS; //I Love To Rename Shit For Aesthetic Reasons
		} else if(reg == Registry.SOUND_EVENT) {
			what = (IForgeRegistry<T>) ForgeRegistries.SOUND_EVENTS;
		} else throw new IllegalStateException("i forgot a registry lol " + reg);
		
		return (DeferredRegister<T>) deferredRegistries.computeIfAbsent(reg, __ -> {
			DeferredRegister<T> deferred = DeferredRegister.create(what, Packages.MODID);
			deferred.register(FMLJavaModLoadingContext.get().getModEventBus());
			return deferred;
		});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> RegistryHandle<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> thingMaker) {
		if(!id.getNamespace().equals(Packages.MODID)) throw new IllegalArgumentException("Forge enforces a modid for some reason in DeferredRegister");
		
		RegistryObject<T> obj = ((DeferredRegister<T>) getDeferredRegister(registry)).register(id.getPath(), thingMaker);
		return new RegistryObjectRegistryHandle<>(obj);
	}
	
	static record RegistryObjectRegistryHandle<T>(RegistryObject<T> obj) implements RegistryHandle<T> {
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
		return new BlockEntityType<>(factory::create, Set.of(blocks), null);
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
	
	@Override
	public void registerDispenserBehavior(RegistryHandle<? extends ItemLike> item, DispenseItemBehavior behavior) {
		dispenseBehaviorsToRegister.put(item, behavior);
	}
	
	private void actuallyRegisterDispenserBehaviors() {
		dispenseBehaviorsToRegister.forEach((handle, behavior) -> DispenserBlock.registerBehavior(handle.get(), behavior));
	}
	
	@Override
	public <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier) {
		//looks the same as on fabric, but it's access widened by both modloaders so i can't use it in common source
		return new MenuType<>(supplier::create);
	}
	
	@Override
	public void registerGlobalPacketHandler(ResourceLocation packetId, GlobalPacketHandler blah) {
		//TODO forge networking is weird!
	}
	
	@Override
	public Path getConfigFolder() {
		return FMLPaths.CONFIGDIR.get();
	}
	
	@Override
	public void installResourceReloadListener(Consumer<ResourceManager> listener, ResourceLocation name, PackType... types) {
		MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent e) -> {
			e.addListener(new SimplePreparableReloadListener<>() {
				@Override
				public String getName() {
					return name.toString();
				}
				
				@Override
				protected Object prepare(ResourceManager mgr, ProfilerFiller prof) {
					return null;
				}
				
				@Override
				protected void apply(Object preparedObject, ResourceManager mgr, ProfilerFiller prof) {
					listener.accept(mgr);
				}
			});
		});
	}
}
