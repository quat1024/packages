package agency.highlysuspect.packages.mixin.client;

import agency.highlysuspect.packages.block.PackageMakerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
	@Shadow @Final private Minecraft minecraft;
	
	@Inject(method = "handleBlockEntityData", at = @At(value = "RETURN")) //Should be running on the main thread now.
	private void packages$handleBlockEntityData$hackyPackageMakerRerender(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket, CallbackInfo ci) {
		if(minecraft.level != null && minecraft.level.getBlockEntity(clientboundBlockEntityDataPacket.getPos()) instanceof PackageMakerBlockEntity) {
			//Chunk rerenders usually don't happen when only the block entity data, but not anything about the blockstate, changes. I need to manually cause one.
			BlockPos pos = clientboundBlockEntityDataPacket.getPos();
			minecraft.levelRenderer.setSectionDirty(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()), SectionPos.blockToSectionCoord(pos.getZ()));
		}
	}
}
