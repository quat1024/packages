package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.config.ConfigSchema;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.BlockEntityFactory;
import agency.highlysuspect.packages.platform.MyMenuSupplier;
import agency.highlysuspect.packages.platform.RegistryHandle;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mod("packages")
public class ForgeInit extends Packages {
	public static ForgeInit instanceForge;
	
	public final SimpleChannel channel = NetworkRegistry.newSimpleChannel(id("n"), () -> "0", "0"::equals, "0"::equals);
	
	private final Map<Registry<?>, DeferredRegister<?>> deferredRegistries = new HashMap<>();
	private final Map<RegistryHandle<? extends ItemLike>, DispenseItemBehavior> dispenseBehaviorsToRegister = new HashMap<>();
	
	private final ForgeConfigSpec.Builder forgeSpec = new ForgeConfigSpec.Builder();
	
	public ForgeInit() {
		if(instanceForge != null) throw new IllegalStateException("Packages forgeInit initialized twice!");
		instanceForge = this;
		
		//general setup
		earlySetup();
		
		//finish up config (earlySetup populated the forgeSpec)
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, forgeSpec.build(), "packages-common.toml");
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadConfig);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onReloadConfig);
		
		//misc events
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::actuallyRegisterDispenserBehaviors);
		MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::attachCaps);
		
		//If i was forge i would simply have client entrypoints
		if(FMLEnvironment.dist == Dist.CLIENT) {
			try {
				Class.forName("agency.highlysuspect.packages.platform.forge.client.ForgeClientInit").getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Packages had a problem initializing ForgeClientInit", e);
			}
		}
	}
	
	@Override
	public boolean isForge() {
		return true;
	}
	
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
	
	private record RegistryObjectRegistryHandle<T>(RegistryObject<T> obj) implements RegistryHandle<T> {
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
		FMLJavaModLoadingContext.get().getModEventBus().addListener((CreativeModeTabEvent.Register e) -> {
			e.registerCreativeModeTab(id, builder -> {
				builder.title(Component.translatable("asd"))
					.displayItems((params, out) -> PItems.addItemStacks(out::accept))
					.icon(icon);
			});
		});
		
		return null; //TODO
	}
	
	@Override
	public void registerDispenserBehavior(RegistryHandle<? extends ItemLike> item, DispenseItemBehavior behavior) {
		dispenseBehaviorsToRegister.put(item, behavior);
	}
	
	@Override
	public <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier) {
		//Looks the same as on FabricPlatformSupport but it's private in mojang source so i can't use it there without access widening
		return new MenuType<>(supplier::create, FeatureFlagSet.of());
	}
	
	@Override
	public void registerActionPacketHandler() {
		channel.registerMessage(ActionPacket.SHORT_ID, ActionPacket.class, ActionPacket::write, ActionPacket::read, (action, ctxSupplier) -> {
			NetworkEvent.Context ctx = ctxSupplier.get();
			//Forge uses the same networkstuff for client -> server and server -> client packets.
			//This is a client -> server packet, so of course the sender is a nonnull player, but Forge doesn't statically know that
			ServerPlayer player = ctx.getSender();
			if(player == null) return;
			
			action.handle(player);
			ctx.setPacketHandled(true);
		});
	}
	
	@Override
	public ConfigSchema.Bakery commonConfigBakery() {
		return new ForgeBackedConfig.Bakery(forgeSpec);
	}
	
	private void actuallyRegisterDispenserBehaviors(FMLCommonSetupEvent e) {
		dispenseBehaviorsToRegister.forEach((handle, behavior) -> DispenserBlock.registerBehavior(handle.get(), behavior));
	}
	
	private void onLoadConfig(ModConfigEvent.Loading e) {
		if(e.getConfig().getModId().equals(MODID)) refreshConfig();
	}
	
	private void onReloadConfig(ModConfigEvent.Reloading e) {
		if(e.getConfig().getModId().equals(MODID)) refreshConfig();
	}
	
	//forge doesn't automatically wrap iinventories with item handlers anymore :pensive:
	//not all bad; with the Package i think a custom implementation is beneficial anyway
	private void attachCaps(AttachCapabilitiesEvent<BlockEntity> e) {
		if(e.getObject() instanceof PackageBlockEntity pkg) {
			e.addCapability(Packages.id("a"), new ICapabilityProvider() {
				@Override
				public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
					return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> new PackageItemHandler(pkg.getContainer())).cast());
				}
			});
		} else if(e.getObject() instanceof PackageMakerBlockEntity pmbe) {
			e.addCapability(Packages.id("b"), new ICapabilityProvider() {
				@Override
				public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
					return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> new SidedInvWrapper(pmbe, side)));
				}
			});
		}
	}
}
