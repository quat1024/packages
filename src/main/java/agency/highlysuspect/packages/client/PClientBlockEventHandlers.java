package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.net.PNetClient;
import agency.highlysuspect.packages.net.BarrelAction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class PClientBlockEventHandlers {
	//Minecraft has startAttack and continueAttack methods.
	//startAttack is called when you start left clicking, and continueAttack is called every tick you continue to left click.
	//If you are looking at a block, this trickles down into MultiPlayerGameMode#startDestroyBlock and continueDestroyBlock.
	//
	//continueAttack will call continueDestroyBlock, which (if you never start the block destruction process) will act as if you were
	//mining, holding the mouse button down, and moving your vision to a different block. which calls startDestroyBlock again.
	//
	//I want to detect when you really begin a new real left-click on the real block, without ever starting the destroy process, because
	//the destroy process will send packets and play sounds and do a bunch of stuff I don't want to do. Hooking startDestroyBlock alone\
	//is not enough, because it will get called from continueDestroyBlock every tick you continue to hold left click.
	//
	//Really, I need to tell apart whether the startDestroyBlock call is ultimately from startAttack or continueAttack.
	//So I set this global variable to the correct value in MixinMinecraft.
	public static Hell causeOfPunch = null;
	public enum Hell { START_ATTACK, CONTINUE_ATTACK }
	
	//Returning "true" will suppress breaking-block behavior
	public static boolean startDestroyBlock(LocalPlayer player, Level level, BlockPos pos, Direction direction) {
		if(player.isSpectator()) return false;
		if(!level.isClientSide) return false; //Integrated server sometimes calls startDestroyBlock too
		
		if(causeOfPunch == Hell.CONTINUE_ATTACK) {
			return true;
		} else if(causeOfPunch != Hell.START_ATTACK) return false;
		causeOfPunch = null;
		
		BlockState state = level.getBlockState(pos);
		if(!(state.getBlock() instanceof PackageBlock)) return false;
		if(player.getItemInHand(InteractionHand.MAIN_HAND).isCorrectToolForDrops(state)) return false; //So it's easier to break the package
		
		Direction frontDir = state.getValue(PackageBlock.FACING).primaryDirection;
		if(direction != frontDir) return false;
		
		PNetClient.requestTake(pos, player.isShiftKeyDown() ? BarrelAction.STACK : BarrelAction.ONE);
		return true;
	}
	
	public static void onInitializeClient() {
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
						final ItemStack contentsOrEmpty = ((PackageBlockEntity) pkg).findFirstNonemptyStack();
						if(contentsOrEmpty.isEmpty() || contentsOrEmpty.sameItem(player.getItemInHand(hand))) {
							PNetClient.requestInsert(pos, hand, player.isShiftKeyDown() ? BarrelAction.STACK : BarrelAction.ONE);
							return InteractionResult.CONSUME;
						}
						if(!contentsOrEmpty.isEmpty()) {
							int slot = player.getInventory().findSlotMatchingItem(contentsOrEmpty);
							if(slot != -1) {
								// This will still pass hand, but we'll check on the server side for available items if the hand stack doesn't match.
								PNetClient.requestInsert(pos, hand, player.isShiftKeyDown() ? BarrelAction.STACK : BarrelAction.ONE);
								return InteractionResult.CONSUME;
							}
						}
					}
				}
			}
			
			return InteractionResult.PASS;
		});
	}
}
