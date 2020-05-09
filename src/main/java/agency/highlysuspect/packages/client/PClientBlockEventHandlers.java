package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.net.PNetClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class PClientBlockEventHandlers {
	private static BlockPos lastPunchPos;
	private static long lastPunchTick;
	
	public static void onInitialize() {
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if(player.isSpectator()) return ActionResult.PASS;
			
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() instanceof PackageBlock) {
				if(player.getStackInHand(hand).isEffectiveOn(state)) return ActionResult.PASS;
				
				Direction frontDir = state.get(PackageBlock.FACING).primaryDirection;
				if(direction == frontDir) {
					if(world.isClient) {
						//Hack to work around AttackBlockCallback getting fired way too often (every tick, plus an extra time when you first punch)
						//TODO this is ass figure out how to fix it actually
						if (pos.equals(lastPunchPos) && (world.getTime() - lastPunchTick < 4)) return ActionResult.SUCCESS;
						lastPunchPos = pos;
						lastPunchTick = world.getTime();
						
						PNetClient.requestTake(pos, player.isSneaking() ? 1 : 0);
					}
					
					return ActionResult.SUCCESS;
				}
			}
			
			return ActionResult.PASS;
		});
		
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if(player.isSpectator()) return ActionResult.PASS;
			
			BlockPos pos = hitResult.getBlockPos();
			Direction direction = hitResult.getSide();
			if(pos == null || direction == null) return ActionResult.PASS; //is this even needed lmao
			
			BlockState state = world.getBlockState(pos);
			BlockEntity pkg = world.getBlockEntity(pos);
			if(state.getBlock() instanceof PackageBlock && pkg instanceof PackageBlockEntity) {
				Direction frontDir = state.get(PackageBlock.FACING).primaryDirection;
				if(direction == frontDir) {
					if(world.isClient) {
						PNetClient.requestInsert(pos, hand, player.isSneaking() ? 1 : 0);
					}
					return ActionResult.SUCCESS;
				}
			}
			
			return ActionResult.PASS;
		});
	}
}
