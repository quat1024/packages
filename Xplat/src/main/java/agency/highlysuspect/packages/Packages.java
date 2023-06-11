package agency.highlysuspect.packages;

import agency.highlysuspect.packages.block.PBlockEntityTypes;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.container.PMenuTypes;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PDispenserBehaviors;
import agency.highlysuspect.packages.junk.PTags;
import agency.highlysuspect.packages.junk.PSoundEvents;
import agency.highlysuspect.packages.junk.SidedProxy;
import agency.highlysuspect.packages.platform.BlockEntityFactory;
import agency.highlysuspect.packages.platform.CommonPlatformConfig;
import agency.highlysuspect.packages.platform.MyMenuSupplier;
import agency.highlysuspect.packages.platform.RegistryHandle;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public abstract class Packages {
	public static final String MODID = "packages";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static Packages instance;
	
	//This defaults to null on purpose. I initially had it set to a default instance to avoid errors, but I'd rather kaboom
	//so I know I'm reading data that doesn't correspond to the config file, because that'd be effectively garbage data.
	public PackagesConfig config = null;
	
	//Reset from PackagesClient
	public SidedProxy proxy = new SidedProxy();
	
	public Packages() {
		if(instance != null) throw new IllegalStateException("Initializing Packages twice!");
		instance = this;
		
		makePlatformConfig().registerAndLoadAndAllThatJazz();
	}
	
	public void earlySetup() {
		PBlocks.onInitialize();
		PBlockEntityTypes.onInitialize();
		PItems.onInitialize();
		
		PDispenserBehaviors.onInitialize();
		PTags.onInitialize();
		
		PMenuTypes.onInitialize();
		registerActionPacketHandler();
		
		PSoundEvents.onInitialize();
	}
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	public abstract <T> RegistryHandle<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> thingMaker);
	public abstract CreativeModeTab makeCreativeModeTab(ResourceLocation id, Supplier<ItemStack> icon);
	public abstract void registerDispenserBehavior(RegistryHandle<? extends ItemLike> item, DispenseItemBehavior behavior);
	public abstract <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks);
	public abstract <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier);
	public abstract void registerActionPacketHandler();
	public abstract CommonPlatformConfig makePlatformConfig();
}
