package agency.highlysuspect.packages.platform.fabric;

import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.CommonPlatformConfig;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class FabricPlatformSupport implements PlatformSupport {
	public FabricPlatformSupport() {
		//Some legacy handling.
		//Packages used to not split its config file into separate common/client files. This was a mistake.
		//I don't want to leave the old files hanging around though. So this little snippet that runs before the new
		//config parser runs, will copy the player's old settings.
		//
		//This is pretty lazy, obviously it copies over all of the settings instead of just the ones relevant to the new file.
		//But the config reading code will soon stomp over this file with a rewritten copy.
		//(This code doesn't exist on Forge simply because versions of the mod for Forge that used a unified config file were not publicly released.)
		try {
			Path configDir = FabricLoader.getInstance().getConfigDir();
			Path legacyConfigFile = configDir.resolve("packages.cfg");
			Path newCommonConfigFile = configDir.resolve("packages-common.cfg");
			Path newClientConfigFile = configDir.resolve("packages-client.cfg");
			if(Files.exists(legacyConfigFile) && !Files.exists(newCommonConfigFile) && !Files.exists(newClientConfigFile)) {
				Files.copy(legacyConfigFile, newCommonConfigFile);
				Files.copy(legacyConfigFile, newClientConfigFile);
				Files.delete(legacyConfigFile);
			}
		} catch (Exception e) {
			e.printStackTrace(); //Not a big deal
		}
	}
	
	@Override
	public <T> RegistryHandle<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> thingMaker) {
		//It's safe to intialize and register the object right away on Fabric.
		T thing = thingMaker.get();
		Registry.register(registry, id, thing);
		
		//Return a handle to it.
		return new ImmediateRegistryHandle<>(thing, id);
	}
	
	@SuppressWarnings("ClassCanBeRecord")
	private static class ImmediateRegistryHandle<T> implements RegistryHandle<T> {
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
	public void registerActionPacketHandler() {
		ServerPlayNetworking.registerGlobalReceiver(ActionPacket.LONG_ID, (server, player, handler, buf, resp) -> ActionPacket.read(buf).handle(player));
	}
	
	@Override
	public CommonPlatformConfig makePlatformConfig() {
		return new FabricCommonPlatformConfig();
	}
}
