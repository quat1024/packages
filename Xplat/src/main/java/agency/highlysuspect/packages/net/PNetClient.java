package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.client.PackagesClient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

public class PNetClient {
	public static void performAction(BlockPos pos, InteractionHand hand, PackageAction mode) {
		PackagesClient.instance.sendActionPacket(new ActionPacket(pos, hand, mode));
	}
}
