package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.junk.EarlyClientsideAttackBlockCallback;
import agency.highlysuspect.packages.net.PNetClient;
import agency.highlysuspect.packages.net.PackageAction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class PClientBlockEventHandlers {
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean canAttack(Player player, Level level, BlockPos pos, Direction direction) {
		if(player.isSpectator()) return false;
		BlockState state = level.getBlockState(pos);
		if(!(state.getBlock() instanceof PackageBlock)) return false;
		Direction frontDir = state.getValue(PackageBlock.FACING).primaryDirection;
		if(direction != frontDir) return false;
		if(player.getItemInHand(InteractionHand.MAIN_HAND).isCorrectToolForDrops(state)) return false; //So it's easier to break the package
		return true;
	}
	
	public static void onInitializeClient() {
		EarlyClientsideAttackBlockCallback.EVENT.register((player, level, pos, direction) -> {
			if(!canAttack(player, level, pos, direction)) return false;
			PNetClient.performAction(pos, InteractionHand.MAIN_HAND, player.isShiftKeyDown() ? PackageAction.TAKE_STACK : PackageAction.TAKE_ONE);
			return true;
		});
		
		//This callback is usually fired when you start left-clicking a block, but also every tick while you continue to left click it.
		//EarlyClientsideAttackBlockCallback will prevent the start-left-clicking one from being fired. This regular callback will
		//also help prevent the block from being mined.
		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
			if(canAttack(player, level, pos, direction)) return InteractionResult.CONSUME;
			else return InteractionResult.PASS;
		});
		
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if(!level.isClientSide || player.isSpectator()) return InteractionResult.PASS;
			
			BlockPos pos = hitResult.getBlockPos();
			Direction direction = hitResult.getDirection();
			if(pos == null || direction == null) return InteractionResult.PASS; //is this even needed lmao
			
			BlockState state = level.getBlockState(pos);
			BlockEntity pkg = level.getBlockEntity(pos);
			if(!(state.getBlock() instanceof PackageBlock) || !(pkg instanceof PackageBlockEntity)) return InteractionResult.PASS;
			
			Direction frontDir = state.getValue(PackageBlock.FACING).primaryDirection;
			if(direction != frontDir) return InteractionResult.PASS;
			
			ItemStack contentsOrEmpty = ((PackageBlockEntity) pkg).findFirstNonemptyStack();
			//Player is holding something matching the pkg (where an empty package counts as matching everything)
			if(contentsOrEmpty.isEmpty() || contentsOrEmpty.sameItem(player.getItemInHand(hand))) {
				PNetClient.performAction(pos, hand, player.isShiftKeyDown() ? PackageAction.INSERT_STACK : PackageAction.INSERT_ONE);
				return InteractionResult.CONSUME;
			}
			
			//Player is holding something else but there's some other item in the package
			if(!contentsOrEmpty.isEmpty()) {
				int slot = player.getInventory().findSlotMatchingItem(contentsOrEmpty);
				if(slot != -1) {
					//This will still pass hand, but we'll check on the server side for available items if the hand stack doesn't match.
					PNetClient.performAction(pos, hand, player.isShiftKeyDown() ? PackageAction.INSERT_STACK : PackageAction.INSERT_ONE);
					return InteractionResult.CONSUME;
				}
			}
			
			return InteractionResult.PASS;
		});
	}
}
