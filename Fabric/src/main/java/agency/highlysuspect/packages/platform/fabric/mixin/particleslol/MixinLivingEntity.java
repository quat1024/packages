package agency.highlysuspect.packages.platform.fabric.mixin.particleslol;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
	@Unique private BlockPos lastFallCheckPos;
	
	@Inject(
		method = "checkFallDamage",
		at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(DD)D"),
		require = 0
	)
	private void packages$checkFallDamages$stashBlockPos(double blah, boolean whoCares, BlockState whatever, BlockPos pos, CallbackInfo ci) {
		lastFallCheckPos = pos;
	}
	
	@ModifyArg(
		method = "checkFallDamage",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/particles/BlockParticleOption;<init>(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)V"),
		require = 0 //Very unimportant feature.
	)
	private BlockState packages$checkFallDamage$modifyParticleOptionArg(BlockState state) {
		if(lastFallCheckPos != null &&
			state.getBlock() == PBlocks.PACKAGE.get() &&
			((Entity) (Object) this).getLevel().getBlockEntity(lastFallCheckPos) instanceof PackageBlockEntity be) {
			return be.getStyle().innerBlock().defaultBlockState();
		} else return state;
	}
}
