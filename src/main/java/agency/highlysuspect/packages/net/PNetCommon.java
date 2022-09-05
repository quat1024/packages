package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PNetCommon {
	public static void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(PMessageTypes.ACTION, (server, player, handler, buf, resp) -> {
			BlockPos pos = buf.readBlockPos();
			InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			PackageAction action = PackageAction.get(buf.readByte());
			
			server.submit(() -> {
				PackageBlockEntity be = getPackageChecked(player.level, player, pos);
				if(be != null) be.performAction(player, hand, action);
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
