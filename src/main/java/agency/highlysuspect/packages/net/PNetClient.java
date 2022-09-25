package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.client.ClientInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

public class PNetClient {
	public static void performAction(BlockPos pos, InteractionHand hand, PackageAction mode) {
		ClientInit.plat.sendActionPacket(new ActionPacket(pos, hand, mode));
	}
}
