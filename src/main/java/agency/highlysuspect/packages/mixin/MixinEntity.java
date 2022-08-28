package agency.highlysuspect.packages.mixin;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public class MixinEntity {
	@Unique private BlockPos lastSprintParticlePos;
	@Shadow public Level level;
	
	@Inject(
		method = "spawnSprintParticle",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
		locals = LocalCapture.CAPTURE_FAILSOFT //Very unimportant feature.
	)
	private void packages$spawnSprintParticle$stashBlockPos(CallbackInfo ci, int i, int j, int k, BlockPos pos) {
		lastSprintParticlePos = pos;
	}
	
	@ModifyArg(
		method = "spawnSprintParticle",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/particles/BlockParticleOption;<init>(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)V"),
		require = 0 //Very unimportant feature.
	)
	private BlockState packages$spawnSprintParticle$modifyParticleOptionArg(BlockState state) {
		if(lastSprintParticlePos != null &&
			state.getBlock() == PBlocks.PACKAGE &&
			level.getBlockEntity(lastSprintParticlePos) instanceof PackageBlockEntity be && 
			be.getRenderAttachmentData() instanceof PackageStyle style) {
			return style.innerBlock().defaultBlockState();
		} else return state;
	}
}
