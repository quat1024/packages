package agency.highlysuspect.packages.platform.fabric;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.config.ConfigSchema;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.BlockEntityFactory;
import agency.highlysuspect.packages.platform.MyMenuSupplier;
import agency.highlysuspect.packages.platform.RegistryHandle;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
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

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FabricInit extends Packages implements ModInitializer {
	@Override
	public void onInitialize() {
		earlySetup();
		
		//load config once now
		refreshConfig();
		
		//and again on resource load
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return Packages.id("fabric-config-reload");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				refreshConfig();
			}
		});
	}
	
	@Override
	public boolean isFabric() {
		return true;
	}
	
	@Override
	public <T> RegistryHandle<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> thingMaker) {
		//It's safe to initialize and register the object right away on Fabric.
		T thing = thingMaker.get();
		Registry.register(registry, id, thing);
		
		//Return a handle to it.
		return new ImmediateRegistryHandle<>(thing, id);
	}
	
	private record ImmediateRegistryHandle<T>(T thing, ResourceLocation id) implements RegistryHandle<T> {
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
	public void makeCreativeModeTab(ResourceLocation id, Supplier<ItemStack> icon, Consumer<Consumer<ItemStack>> contents) {
		FabricItemGroup.builder(id)
			.icon(icon)
			.displayItems((params, out) -> contents.accept(out::accept))
			.build();
	}
	
	@Override
	public void registerDispenserBehavior(RegistryHandle<? extends ItemLike> item, DispenseItemBehavior behavior) {
		DispenserBlock.registerBehavior(item.get(), behavior);
	}
	
	@Override
	public <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier) {
		return new MenuType<>(supplier::create, FeatureFlagSet.of());
	}
	
	@Override
	public void registerActionPacketHandler() {
		ServerPlayNetworking.registerGlobalReceiver(ActionPacket.LONG_ID, (server, player, handler, buf, resp) -> ActionPacket.read(buf).handle(player));
	}
	
	@Override
	public ConfigSchema.Bakery commonConfigBakery() {
		return new CrummyConfig.Bakery(FabricLoader.getInstance().getConfigDir().resolve("packages-common.cfg"));
	}
}
