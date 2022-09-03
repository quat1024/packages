package agency.highlysuspect.packages.mixin.particleslol;

import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TerrainParticle.class)
public class MixinTerrainParticle {
	@Inject(
		method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
		at = @At("TAIL"),
		require = 0 //Unimportant feature
	)
	private void packages$onInit(ClientLevel level, double d, double e, double f, double g, double h, double i, BlockState state, BlockPos pos, CallbackInfo ci) {
		if(level.getBlockEntity(pos) instanceof PackageBlockEntity be && be.getRenderAttachmentData() instanceof PackageStyle style) {
			((AccessorTextureSheetParticle) this).packages$setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(style.innerBlock().defaultBlockState()));
		}
	}
}
