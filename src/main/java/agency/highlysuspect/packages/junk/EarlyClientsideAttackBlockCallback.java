package agency.highlysuspect.packages.junk;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * This callback is fired on the client-side when the player begins to press their "use" key on a block.
 * Unlike Fabric's AttackBlockCallback, which is slightly deeper into the weeds of use-key handling, this
 * is hooked ASAP after it is determined that the player is pressing "use" and looking at a block.
 * Returning "true" will cancel all further processing: startDestroyBlock will not be called at all, which means
 * the AttackBlockCallback won't be called either for the start of the left click.
 * 
 * continueDestroyBlock may still continue to be called and fire more AttackBlockCallbacks, so pairing this
 * callback with the regular Fabric AttackBlockCallback isn't a bad idea.
 */
public interface EarlyClientsideAttackBlockCallback {
	Event<EarlyClientsideAttackBlockCallback> EVENT = EventFactory.createArrayBacked(EarlyClientsideAttackBlockCallback.class,
		listeners -> (player, world, pos, direction) -> {
			for (EarlyClientsideAttackBlockCallback event : listeners) {
				if(event.interact(player, world, pos, direction)) return true;
			}
			return false;
		}
	);
	
	boolean interact(Player player, Level level, BlockPos pos, Direction direction);
}
