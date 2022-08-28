package agency.highlysuspect.packages.mixin.client;

import agency.highlysuspect.packages.client.PClientBlockEventHandlers;
import agency.highlysuspect.packages.junk.EarlyClientsideAttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @see PClientBlockEventHandlers
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {
	@Shadow @Nullable public LocalPlayer player;
	@Shadow @Nullable public ClientLevel level;
	
	@Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void packages$startAttack$beforeStartDestroyingBlock(CallbackInfoReturnable<Boolean> cir, boolean miscLocal, BlockHitResult hit, BlockPos hitPos) {
		if(EarlyClientsideAttackBlockCallback.EVENT.invoker().interact(player, level, hitPos, hit.getDirection())) {
			player.swing(InteractionHand.MAIN_HAND);
			cir.setReturnValue(true);
		}
	}
}
