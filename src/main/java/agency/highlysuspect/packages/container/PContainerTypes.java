package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.PackagesInit;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class PContainerTypes {
	public static final Identifier PACKAGE_MAKER = new Identifier(PackagesInit.MODID, "package_maker");
	
	public static void onInitialize() {
		ContainerProviderRegistry.INSTANCE.registerFactory(PACKAGE_MAKER, PackageMakerScreenHandler::constructFromNetwork);
	}
	
	public static void openPackageMaker(PlayerEntity player, BlockPos pos) {
		ContainerProviderRegistry.INSTANCE.openContainer(PACKAGE_MAKER, player, (buf) -> {
			buf.writeBlockPos(pos);
		});
	}
}
