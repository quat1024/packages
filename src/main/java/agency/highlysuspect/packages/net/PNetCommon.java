package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PNetCommon {
	public static void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(PMessageTypes.ACTION, (server, player, handler, buf, resp) -> {
			BlockPos pos = buf.readBlockPos();
			InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			PackageAction action = PackageAction.get(buf.readByte());
			
			server.submit(() -> {
				Level world = player.level;
				
				if(!packageSanityCheck(world, player, pos)) return;
				
				PackageBlockEntity be = (PackageBlockEntity) world.getBlockEntity(pos);
				assert be != null; //sanity checked
				
				be.performAction(player, hand, action);
			});
		});
	}
	
	@SuppressWarnings({"RedundantIfStatement", "deprecation", "BooleanMethodIsAlwaysInverted"})
	private static boolean packageSanityCheck(Level world, Player player, BlockPos pos) {
		if(!world.hasChunkAt(pos)) return false;
		if(player.blockPosition().distSqr(pos) > 8 * 8) return false;
		
		BlockState state = world.getBlockState(pos);
		if(!(state.getBlock() instanceof PackageBlock)) return false;
		
		BlockEntity be = world.getBlockEntity(pos);
		if(!(be instanceof PackageBlockEntity)) return false;
		
		return true;
	}
}
