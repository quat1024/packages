package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.container.PackageMakerContainer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PNetCommon {
	public static void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(PMessageTypes.INSERT, (ctx, buf) -> {
			BlockPos pos = buf.readBlockPos();
			Hand hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
			int mode = buf.readByte();
			
			ctx.getTaskQueue().execute(() -> {
				PlayerEntity player = ctx.getPlayer();
				World world = player.world;
				
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
				PlayerEntity player = ctx.getPlayer();
				World world = player.world;
				
				if (!packageSanityCheck(world, ctx.getPlayer(), pos)) return;
				
				PackageBlockEntity be = (PackageBlockEntity) world.getBlockEntity(pos);
				assert be != null; //sanity checked
				
				be.take(player, mode == 1);
			});
		});
		
		ServerSidePacketRegistry.INSTANCE.register(PMessageTypes.PACKAGE_CRAFT, (ctx, buf) -> {
			boolean all = buf.readBoolean();
			ctx.getTaskQueue().execute(() -> {
				PlayerEntity player = ctx.getPlayer();
				
				if(player.container instanceof PackageMakerContainer) {
					for(int i = 0; i < 64; i++) { //Janky hack mate
						((PackageMakerContainer) player.container).be.performCraft();
						if(!all) return;
					}
				}
			});
		});
	}
	
	@SuppressWarnings({"RedundantIfStatement", "deprecation"})
	private static boolean packageSanityCheck(World world, PlayerEntity player, BlockPos pos) {
		if(!world.isChunkLoaded(pos)) return false;
		if(player.getBlockPos().getSquaredDistance(pos) > 8 * 8) return false;
		
		BlockState state = world.getBlockState(pos);
		if(!(state.getBlock() instanceof PackageBlock)) return false;
		
		BlockEntity be = world.getBlockEntity(pos);
		if(!(be instanceof PackageBlockEntity)) return false;
		
		return true;
	}
}
