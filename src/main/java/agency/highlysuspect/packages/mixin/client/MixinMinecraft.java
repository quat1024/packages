package agency.highlysuspect.packages.mixin.client;

import agency.highlysuspect.packages.client.PClientBlockEventHandlers;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This is as kludgy as it looks, yes
 * @see PClientBlockEventHandlers
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {
	@Inject(method = "startAttack", at = @At("HEAD"))
	private void packages$startAttack(CallbackInfoReturnable<Boolean> cir) {
		PClientBlockEventHandlers.causeOfPunch = PClientBlockEventHandlers.Hell.START_ATTACK;
	}
	
	@Inject(method = "continueAttack", at = @At("HEAD"))
	private void packages$continueAttack(boolean bl, CallbackInfo ci) {
		PClientBlockEventHandlers.causeOfPunch = PClientBlockEventHandlers.Hell.CONTINUE_ATTACK;
	}
}
