package agency.highlysuspect.packages.net;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.util.Hand;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class PNetClient {
	public static void onInitialize() {
		
	}
	
	public static void requestInsert(BlockPos pos, Hand hand, int mode) { //TODO magic ints bad
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(pos);
		buf.writeBoolean(hand == Hand.MAIN_HAND);
		buf.writeByte(mode);
		ClientSidePacketRegistry.INSTANCE.sendToServer(PMessageTypes.INSERT, buf);
	}
	
	public static void requestTake(BlockPos pos, int mode) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(pos);
		buf.writeByte(mode);
		ClientSidePacketRegistry.INSTANCE.sendToServer(PMessageTypes.TAKE, buf);
	}
	
	public static void requestPackageMakerCraft(boolean all) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBoolean(all);
		ClientSidePacketRegistry.INSTANCE.sendToServer(PMessageTypes.PACKAGE_CRAFT, buf);
	}
}
