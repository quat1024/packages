package agency.highlysuspect.packages.platform.fabric.mixin;

import agency.highlysuspect.packages.platform.fabric.client.FabricClientInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	@Shadow @Nullable public LocalPlayer player;
	@Shadow @Nullable public ClientLevel level;
	@Shadow @Nullable public HitResult hitResult;
	
	@Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"), cancellable = true)
	private void packages$startAttack$beforeStartDestroyingBlock(CallbackInfoReturnable<Boolean> cir) {
		if(player != null && level != null && hitResult instanceof BlockHitResult bhr) {
			if(FabricClientInit.instanceFabric.EARLY_LEFT_CLICK_EVENT.invoker().interact(player, level, bhr.getBlockPos(), bhr.getDirection())) {
				player.swing(InteractionHand.MAIN_HAND);
				cir.setReturnValue(true);
			}
		}
	}
}
