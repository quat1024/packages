package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.junk.SidedProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

public class ClientProxy extends SidedProxy {
	@Override
	public void forceChunkRerender(Level level, BlockPos pos) {
		if(level == Minecraft.getInstance().level) {
			Minecraft.getInstance().levelRenderer.setSectionDirty(
				SectionPos.blockToSectionCoord(pos.getX()),
				SectionPos.blockToSectionCoord(pos.getY()),
				SectionPos.blockToSectionCoord(pos.getZ())
			);
		}
	}
	
	@Override
	public boolean hasShiftDownForTooltip() {
		return Screen.hasShiftDown();
	}
}
