package agency.highlysuspect.packages.net;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public class PNetClient {
	public static void requestInsert(BlockPos pos, InteractionHand hand, PackageAction mode) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeBlockPos(pos);
		buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
		buf.writeByte(mode.netValue);
		ClientPlayNetworking.send(PMessageTypes.INSERT, buf);
	}
	
	public static void requestTake(BlockPos pos, PackageAction mode) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeBlockPos(pos);
		buf.writeByte(mode.netValue);
		ClientPlayNetworking.send(PMessageTypes.TAKE, buf);
	}
}
