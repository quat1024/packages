package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.container.PackageMakerScreenHandler;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PNetCommon {
	public static void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(PMessageTypes.INSERT, (ctx, buf) -> {
			BlockPos pos = buf.readBlockPos();
			InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			int mode = buf.readByte();
			
			ctx.getTaskQueue().execute(() -> {
				Player player = ctx.getPlayer();
				Level world = player.level;
				
				if(!packageSanityCheck(world, ctx.getPlayer(), pos)) return;
				
				PackageBlockEntity be = (PackageBlockEntity) world.getBlockEntity(pos);
				assert be != null; //sanity checked
				
				//TODO: fix this insertion logic, probably move it out of the BE (just needed somewhere to put it for now)
				be.insert(player, hand, mode == 1);
			});
		});
		
		ServerSidePacketRegistry.INSTANCE.register(PMessageTypes.TAKE, (ctx, buf) -> {
			BlockPos pos = buf.readBlockPos();
			int mode = buf.readByte();
			
			ctx.getTaskQueue().execute(() -> {
				Player player = ctx.getPlayer();
				Level world = player.level;
				
				if (!packageSanityCheck(world, ctx.getPlayer(), pos)) return;
				
				PackageBlockEntity be = (PackageBlockEntity) world.getBlockEntity(pos);
				assert be != null; //sanity checked
				
				be.take(player, mode == 1);
			});
		});
		
		ServerSidePacketRegistry.INSTANCE.register(PMessageTypes.PACKAGE_CRAFT, (ctx, buf) -> {
			boolean all = buf.readBoolean();
			ctx.getTaskQueue().execute(() -> {
				Player player = ctx.getPlayer();
				
				if(player.containerMenu instanceof PackageMakerScreenHandler) {
					for(int i = 0; i < 64; i++) { //Janky hack mate
						((PackageMakerScreenHandler) player.containerMenu).be.performCraft();
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
