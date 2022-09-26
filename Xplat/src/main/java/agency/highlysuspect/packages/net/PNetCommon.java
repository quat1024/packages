package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PNetCommon {
	public static void onInitialize(PlatformSupport plat) {
		plat.registerGlobalPacketHandler(ActionPacket.LONG_ID, (server, player, buf) -> {
			ActionPacket actionPacket = ActionPacket.read(buf);
			server.submit(() -> {
				PackageBlockEntity be = getPackageChecked(player.level, player, actionPacket.pos);
				if(be != null) be.performAction(player, actionPacket.hand, actionPacket.action);
			});
		});
	}
	
	@SuppressWarnings({"deprecation", "BooleanMethodIsAlwaysInverted"})
	private static @Nullable PackageBlockEntity getPackageChecked(Level level, Player player, BlockPos pos) {
		if(!level.hasChunkAt(pos) || player.blockPosition().distSqr(pos) > 8 * 8) return null;
		if(!(level.getBlockState(pos).getBlock() instanceof PackageBlock)) return null;
		return level.getBlockEntity(pos) instanceof PackageBlockEntity pbe ? pbe : null;
	}
}
