package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.block.PackageMakerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod("packages")
public class ForgeInit extends Packages {
	//Idk where else to put this
	private static final String NET_VERSION = "0";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(id("n"), () -> NET_VERSION, NET_VERSION::equals, NET_VERSION::equals);
	
	public ForgeInit() {
		super(new ForgePlatformSupport());
		
		earlySetup();
		
		if(FMLEnvironment.dist == Dist.CLIENT) {
			try {
				Class.forName("agency.highlysuspect.packages.platform.forge.client.ForgeClientInit").getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Packages had a problem initializing ForgeClientInit", e);
			}
		}
		
		//Funny moments.
		//Apparently Forge doesn't automagically wrap Containers with IItemHandlers anymore, that's a bit annoying.
		//Oh well, I think doing a bespoke implementation is fine anyway.
		MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, (AttachCapabilitiesEvent<BlockEntity> e) -> {
			if(e.getObject() instanceof PackageBlockEntity pkg) {
				e.addCapability(Packages.id("a"), new ICapabilityProvider() {
					@Override
					public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
						return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> new PackageItemHandler(pkg.getContainer())).cast());
					}
				});
			} else if(e.getObject() instanceof PackageMakerBlockEntity pmbe) {
				e.addCapability(Packages.id("b"), new ICapabilityProvider() {
					@Override
					public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
						return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> new SidedInvWrapper(pmbe, side)));
					}
				});
			}
		});
	}
}
