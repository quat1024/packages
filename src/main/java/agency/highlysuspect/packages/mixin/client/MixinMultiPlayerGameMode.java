package agency.highlysuspect.packages.mixin.client;

import agency.highlysuspect.packages.client.PClientBlockEventHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric API has a MixinClientPlayerInteractionManager (= MultiPlayerGameMode) that hooks into their AttackBlockCallback.
 * For some ungodly reason, the hook is fired twice when you begin left clicking a block, and also once every tick you continue
 * to hold left click on the block. It also passes you a "hand" parameter that is simply a lie; it's always MAIN_HAND.
 * As far as I can tell there isn't a way to tell the calls apart other than trying to stash the current tick count somewhere.
 * 
 * This sucks, so I'm making my own. This is only fired once when you actually *begin* left clicking a block.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {
	@Shadow @Final private Minecraft minecraft;
	
	@Inject(at = @At(value = "HEAD"), method = "startDestroyBlock", cancellable = true)
	public void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		assert minecraft.level != null;
		boolean result = PClientBlockEventHandlers.startDestroyBlock(minecraft.player, minecraft.level, pos, direction);
		if(result) cir.setReturnValue(true);
	}
}
