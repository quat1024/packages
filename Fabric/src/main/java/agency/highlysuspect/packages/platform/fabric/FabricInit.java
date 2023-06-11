package agency.highlysuspect.packages.platform.fabric;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.BlockEntityFactory;
import agency.highlysuspect.packages.platform.CommonPlatformConfig;
import agency.highlysuspect.packages.platform.MyMenuSupplier;
import agency.highlysuspect.packages.platform.RegistryHandle;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class FabricInit extends Packages implements ModInitializer {
	public FabricInit() {
		super();
	}
	
	@Override
	public void onInitialize() {
		earlySetup();
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
	public void registerActionPacketHandler() {
		ServerPlayNetworking.registerGlobalReceiver(ActionPacket.LONG_ID, (server, player, handler, buf, resp) -> ActionPacket.read(buf).handle(player));
	}
	
	@Override
	public CommonPlatformConfig makePlatformConfig() {
		return new FabricCommonPlatformConfig();
	}
}
