package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.client.PackageActionBinding.MainTrigger;
import agency.highlysuspect.packages.net.PNetClient;
import agency.highlysuspect.packages.net.PackageAction;
import agency.highlysuspect.packages.platform.ClientPlatformSupport;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PClientBlockEventHandlers {
	private static BlockPos lastPunchPosLegacy;
	private static long lastPunchTickLegacy;
	
	@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "RedundantIfStatement"})
	public static boolean canAttack(Player player, Level level, BlockPos pos, Direction direction) {
		if(player.isSpectator()) return false;
		BlockState state = level.getBlockState(pos);
		if(!(state.getBlock() instanceof PackageBlock)) return false;
		Direction frontDir = state.getValue(PackageBlock.FACING).primaryDirection;
		if(direction != frontDir) return false;
		if(player.getItemInHand(InteractionHand.MAIN_HAND).isCorrectToolForDrops(state)) return false; //So it's easier to break the package
		return true;
	}
	
	public static boolean performPunchAction(Player player, Level level, BlockPos pos, Direction direction) {
		BlockState state = level.getBlockState(pos);
		BlockEntity be = level.getBlockEntity(pos);
		if(!(state.getBlock() instanceof PackageBlock) || !(be instanceof PackageBlockEntity pkg)) return false;
		
		PackageAction action = getApplicableAction(player, MainTrigger.PUNCH);
		if(action == null) return false;
		
		if(pkg.performAction(player, InteractionHand.MAIN_HAND, action, true)) {
			PNetClient.performAction(pos, InteractionHand.MAIN_HAND, action);
			lastPunchTickLegacy = level.getGameTime();
			return true;
		}
		return false;
	}
	
	public static void onInitializeClient(ClientPlatformSupport plat) {
		plat.installEarlyClientsideLeftClickCallback((player, level, pos, direction) -> {
			if(!canAttack(player, level, pos, direction)) return false;
			return performPunchAction(player, level, pos, direction);
		});
		
		//This callback is usually fired when you start left-clicking a block, but also every tick while you continue to left click it.
		//EarlyClientsideAttackBlockCallback will prevent the start-left-clicking one from being fired. This regular callback will
		//also help prevent the block from being mined.
		plat.installClientsideHoldLeftClickCallback((player, level, hand, pos, direction) -> {
			if(canAttack(player, level, pos, direction)) {
				//Legacy stuff! Here's a reimplementation of the old, broken left click antirepeat. Old mod effectively had punchRepeat set to 4 ticks btw.
				//removed in https://github.com/quat1024/packages/commit/401a19818dac539174081b219ca10c797fa0abf0
				if(!level.isClientSide || PackagesClient.instance.config.punchRepeat < 0) return InteractionResult.CONSUME;
				
				if(pos.equals(lastPunchPosLegacy) && (level.getGameTime() - lastPunchTickLegacy <= PackagesClient.instance.config.punchRepeat)) return InteractionResult.CONSUME;
				lastPunchPosLegacy = pos;
				lastPunchTickLegacy = level.getGameTime();
				performPunchAction(player, level, pos, direction);
				return InteractionResult.CONSUME;
			} else return InteractionResult.PASS;
		});
		
		plat.installClientsideUseBlockCallback((player, level, hand, hitResult) -> {
			if(!level.isClientSide || player.isSpectator()) return InteractionResult.PASS;
			
			BlockPos pos = hitResult.getBlockPos();
			BlockState state = level.getBlockState(pos);
			BlockEntity be = level.getBlockEntity(pos);
			if(!(state.getBlock() instanceof PackageBlock) || !(be instanceof PackageBlockEntity pkg)) return InteractionResult.PASS;
			Direction direction = hitResult.getDirection();
			
			Direction frontDir = state.getValue(PackageBlock.FACING).primaryDirection;
			if(direction != frontDir) return InteractionResult.PASS;
			
			PackageAction action = getApplicableAction(player, MainTrigger.USE);
			if(action == null) return InteractionResult.PASS;
			
			//Simulate performing the action. If anything happened...
			if(pkg.performAction(player, hand, action, true)) {
				//...send a packet to do it for real
				//SUCCESS sends a block-place packet too because fabric api is weird, so we use CONSUME+swing to mark the action as successful.
				PNetClient.performAction(pos, hand, action);
				player.swing(hand);
				return InteractionResult.CONSUME;
			}
			
			//Also send CONSUME on failures that get this far in, because in practice it's annoying to be holding a block and have it go "oh, you
			//pressed right click but the package contained a different item? aha, better place the block against the face of the package, covering it".
			//But it *also* also feels weird for the action to still be consumed when Sneak is pressed, even though sneak-clicks are overridden
			//to mean something else in this mod, in my experience. So we have this confusing nonsense line. Hopefully it makes things intuitive.
			return player.isShiftKeyDown() ? InteractionResult.PASS : InteractionResult.CONSUME;
		});
	}
	
	public static @Nullable PackageAction getApplicableAction(Player player, MainTrigger main) {
		//Find the closest one by edit distance
		PackageActionBinding leastWrongBinding = null;
		int leastWrongness = NOPE;
		for(PackageActionBinding binding : PackagesClient.instance.config.sortedBindings) {
			int wrongness = computeWrongness(player, binding, main);
			if(wrongness == 0) return binding.action(); //Exact match, don't bother checking others
			if(wrongness < leastWrongness) {
				leastWrongness = wrongness;
				leastWrongBinding = binding;
			}
		}
		if(leastWrongBinding != null) return leastWrongBinding.action();
		else return null;
	}

//	private static boolean matchesExactly(Player player, PackageActionBinding binding, MainTrigger main) {
//		if(binding.mainTrigger() == MainTrigger.UNDEFINED || binding.mainTrigger() != main) return false;
//		return binding.ctrl() == Screen.hasControlDown() && binding.alt() == Screen.hasAltDown() && binding.sneak() == player.isShiftKeyDown();
//	}
	
	/**
	 * - The binding requires a modifier, and the player is not holding the modifier. No match.
	 * - The binding requires a modifier, and the player is holding the modifier. Perfect match.
	 * - The binding doesn't require a modifier, and the player is not holding the modifier. Perfect match.
	 * - The binding doesn't require a modifier, and the player is holding the modifier...
	 *    But this is an interesting case, it's "wrong" but comes up in practice.
	 *    If you bind things to only ctrl-click and shift-click, if you *alt*-shift-click, you'd still expect the shift-click one to match, right?
	 *    Just because you are pressing an unused modifier key shouldn't invalidate all the other bindings.
	 *    But if you have something actually bound to alt-shift-click, you'd expect it to take priority.
	 * 
	 * So this method returns NOPE if the keybinding doesn't match at all, and otherwise returns a score tallying how wrong it is.
	 * A binding is "wrong" but not NOPE-worthy if the player is holding extra modifier keys.
	 * Run through all the modifiers and pick the one with the lowest score that isn't NOPE.
	 * A score of 0 is an exact match, 1 is one wrong modifier key, etc.
	 */
	private static int computeWrongness(Player player, PackageActionBinding binding, MainTrigger main) {
		if(binding.mainTrigger() == MainTrigger.UNDEFINED || binding.mainTrigger() != main) return NOPE;
		int wrongness = 0;
		if(binding.ctrl() != Screen.hasControlDown()) {
			if(binding.ctrl()) return NOPE;
			else wrongness++;
		}
		if(binding.alt() != Screen.hasAltDown()) {
			if(binding.alt()) return NOPE;
			else wrongness++;
		}
		if(binding.sneak() != player.isShiftKeyDown()) {
			if(binding.sneak()) return NOPE;
			else wrongness++;
		}
		return wrongness;
	}
	private static final int NOPE = 10000;
}