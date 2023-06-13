package agency.highlysuspect.packages.junk;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Let's party like it's Forge 1.12.
 */
public class SidedProxy {
	public void forceChunkRerender(Level level, BlockPos pos) {
		//No-op in common code
	}
	
	public boolean hasShiftDownForTooltip() {
		//Anything sniffing tooltips server-side (I forget if this is safe or not, lol) should get all info
		return true;
	}
	
	public boolean useRedBarWhenFull() {
		return false;
	}
}
