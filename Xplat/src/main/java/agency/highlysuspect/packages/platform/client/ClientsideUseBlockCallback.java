package agency.highlysuspect.packages.platform.client;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface ClientsideUseBlockCallback {
	InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult);
}
