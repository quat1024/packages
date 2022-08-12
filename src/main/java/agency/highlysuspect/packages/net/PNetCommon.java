package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.container.PackageMakerMenu;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PNetCommon {
	public static void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(PMessageTypes.INSERT, (server, player, handler, buf, resp) -> {
			BlockPos pos = buf.readBlockPos();
			InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			PSneakingStatus mode = PSneakingStatus.safeGetStatusFromByte(buf.readByte());
			
			server.submit(() -> {
				Level world = player.level;
				
				if(!packageSanityCheck(world, player, pos)) return;
				
				PackageBlockEntity be = (PackageBlockEntity) world.getBlockEntity(pos);
				assert be != null; //sanity checked
				
				//TODO: fix this insertion logic, probably move it out of the BE (just needed somewhere to put it for now)
				be.insert(player, hand, mode == PSneakingStatus.IS_SNEAKING);
			});
		});
		
		ServerPlayNetworking.registerGlobalReceiver(PMessageTypes.TAKE, (server, player, handler, buf, resp) -> {
			BlockPos pos = buf.readBlockPos();
            PSneakingStatus mode = PSneakingStatus.safeGetStatusFromByte(buf.readByte());
			
			server.submit(() -> {
				Level world = player.level;
				
				if (!packageSanityCheck(world, player, pos)) return;
				
				PackageBlockEntity be = (PackageBlockEntity) world.getBlockEntity(pos);
				assert be != null; //sanity checked
				
				be.take(player, mode == PSneakingStatus.IS_SNEAKING);
			});
		});
		
		ServerPlayNetworking.registerGlobalReceiver(PMessageTypes.PACKAGE_CRAFT, (server, player, handler, buf, resp) -> {
			boolean all = buf.readBoolean();
			server.submit(() -> {
				if(player.containerMenu instanceof PackageMakerMenu) {
					for(int i = 0; i < 64; i++) { //Janky hack mate
						((PackageMakerMenu) player.containerMenu).be.performCraft();
						if(!all) return;
					}
				}
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
