package agency.highlysuspect.packages.mixin.client;

import agency.highlysuspect.packages.client.PClientBlockEventHandlers;
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
			if(PClientBlockEventHandlers.onEarlyLeftClick(player, level, bhr.getBlockPos(), bhr.getDirection())) {
				player.swing(InteractionHand.MAIN_HAND);
				cir.setReturnValue(true);
			}
		}
	}
	
	//old comment in forge:
	
	//I was going to be like "hey, credit where it's due, I needed a kludge for this on Fabric".
	//Turns out that ClickInputEvent is fired in both Minecraft#startAttack ***and Minecraft#continueAttack***???
	//This is the exact situation I am trying to use lower-level input-based events to *avoid*!!!
	//
	//Cancelling mining the block from the LeftClickBlock event (which is more of a "start mining block" event)
	//kinda causes you to restart trying to mine the block every tick, which is a problem when I am only trying
	//to detect the first left click. This makes sense tbh and happens on Fabric too.
	//That's why I use a lower-level click event anyway, I'm only interested in the first time you try to
	//mine the block, not all the other spam times.
	//
	//But because Forge fires this event in continueAttack as well, completely defeating the purpose
	//of offering a click event separate from LeftClickBlock in the first place, mixin it is then. Auuhgh.
	//
	//See MixinMinecraft.
	
	//these days i use the same mixin on fabric and forge too but wanted to preserve the comment lol
}
