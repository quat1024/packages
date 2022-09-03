package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.junk.EarlyClientsideAttackBlockCallback;
import agency.highlysuspect.packages.junk.PackageContainer;
import agency.highlysuspect.packages.net.PNetClient;
import agency.highlysuspect.packages.net.PackageAction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
			
			BlockState state = level.getBlockState(pos);
			BlockEntity be = level.getBlockEntity(pos);
			if(!(state.getBlock() instanceof PackageBlock) || !(be instanceof PackageBlockEntity pkg)) return false;
			PackageContainer container = pkg.getContainer();
			
			PackageAction action = PackageAction.TAKE_ONE;
			if(player.isShiftKeyDown()) action = PackageAction.TAKE_STACK;
			if(Screen.hasControlDown()) action = PackageAction.TAKE_ALL;
			
			PackageContainer.PlayerTakeResult result = container.take(player, InteractionHand.MAIN_HAND, action, true);
			if(result.successful()) {
				PNetClient.performAction(pos, InteractionHand.MAIN_HAND, action);
				return true;
			} else return false;
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
			BlockEntity be = level.getBlockEntity(pos);
			if(!(state.getBlock() instanceof PackageBlock) || !(be instanceof PackageBlockEntity pkg)) return InteractionResult.PASS;
			PackageContainer container = pkg.getContainer();
			
			Direction frontDir = state.getValue(PackageBlock.FACING).primaryDirection;
			if(direction != frontDir) return InteractionResult.PASS;
			
			PackageAction action = PackageAction.INSERT_ONE;
			if(player.isShiftKeyDown()) action = PackageAction.INSERT_STACK;
			if(Screen.hasControlDown()) action = PackageAction.INSERT_ALL;
			
			//Simulate performing the action. If anything happened...
			if(container.insert(player, hand, action, true)) {
				//...send a packet to do it for real
				PNetClient.performAction(pos, hand, action);
				player.swing(hand);
				return InteractionResult.CONSUME; //SUCCESS sends a block-place packet too because useblockcallback wacky
			} else return InteractionResult.PASS;
		});
	}
}
