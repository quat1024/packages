package agency.highlysuspect.packages.platform.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface EarlyClientsideLeftClickCallback {
	boolean interact(Player player, Level level, BlockPos pos, Direction direction);
}
