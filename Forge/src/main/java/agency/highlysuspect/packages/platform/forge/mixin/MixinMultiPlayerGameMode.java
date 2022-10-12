package agency.highlysuspect.packages.platform.forge.mixin;

import agency.highlysuspect.packages.client.PackagesClient;
import agency.highlysuspect.packages.platform.ClientPlatformSupport;
import agency.highlysuspect.packages.platform.forge.client.ForgeClientPlatformSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {
	@Shadow @Final private Minecraft minecraft;
	
	@Inject(
		method = "startDestroyBlock",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;sendBlockAction(Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V", ordinal = 0),
		cancellable = true
	)
	private void packages$startDestroyBlock$creativeBreakBeforePacketSentToServer(BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> cir) {
		destroyBlockImpl(pos, dir, cir);
	}
	
	@Inject(
		method = "continueDestroyBlock",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;sendBlockAction(Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V", ordinal = 0),
		cancellable = true
	)
	private void packages$continueDestroyBlock$creativeBreakBeforePacketSentToServer(BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> cir) {
		destroyBlockImpl(pos, dir, cir);
	}
	
	@Unique private void destroyBlockImpl(BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> cir) {
		//See comment in ForgeClientPlatformSupport#installClientsideHoldLeftClickCallback.
		//By this point in the injectors, creative mode was already checked.
		if(minecraft.player != null && minecraft.level != null && PackagesClient.instance.plat instanceof ForgeClientPlatformSupport frog) {
			for(ClientPlatformSupport.ClientsideHoldLeftClickCallback callback : frog.holdLeftClickCallbacksForCreativeMode) {
				if(callback.interact(minecraft.player, minecraft.level, InteractionHand.MAIN_HAND, pos, dir).consumesAction()) {
					//Would it be a good idea to manually fire off the forge event in this case?
					cir.setReturnValue(true);
				}
			}
		}
	}
}
