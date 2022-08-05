package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.net.PNetClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class PClientBlockEventHandlers {
	private static BlockPos lastPunchPos;
	private static long lastPunchTick;
	
	public static void onInitializeClient() {
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if(player.isSpectator()) return InteractionResult.PASS;
			
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() instanceof PackageBlock) {
				if(player.getItemInHand(hand).isCorrectToolForDrops(state)) return InteractionResult.PASS;
				
				Direction frontDir = state.getValue(PackageBlock.FACING).primaryDirection;
				if(direction == frontDir) {
					if(world.isClientSide) {
						//Hack to work around AttackBlockCallback getting fired way too often (every tick, plus an extra time when you first punch)
						//TODO this is ass figure out how to fix it actually
						if (pos.equals(lastPunchPos) && (world.getGameTime() - lastPunchTick < 4)) return InteractionResult.SUCCESS;
						lastPunchPos = pos;
						lastPunchTick = world.getGameTime();
						
						PNetClient.requestTake(pos, player.isShiftKeyDown() ? 1 : 0);
					}
					
					return InteractionResult.SUCCESS;
				}
			}
			
			return InteractionResult.PASS;
		});
		
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if(player.isSpectator()) return InteractionResult.PASS;
			
			BlockPos pos = hitResult.getBlockPos();
			Direction direction = hitResult.getDirection();
			if(pos == null || direction == null) return InteractionResult.PASS; //is this even needed lmao
			
			BlockState state = world.getBlockState(pos);
			BlockEntity pkg = world.getBlockEntity(pos);
			if(state.getBlock() instanceof PackageBlock && pkg instanceof PackageBlockEntity) {
				Direction frontDir = state.getValue(PackageBlock.FACING).primaryDirection;
				if(direction == frontDir) {
					if(world.isClientSide) {
						PNetClient.requestInsert(pos, hand, player.isShiftKeyDown() ? 1 : 0);
					}
					return InteractionResult.CONSUME;
				}
			}
			
			return InteractionResult.PASS;
		});
	}
}
