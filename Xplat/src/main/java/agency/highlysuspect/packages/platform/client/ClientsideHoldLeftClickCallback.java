package agency.highlysuspect.packages.platform.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ClientsideHoldLeftClickCallback {
	InteractionResult interact(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction);
}
