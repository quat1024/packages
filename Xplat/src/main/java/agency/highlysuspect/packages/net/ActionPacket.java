package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.Packages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public final class ActionPacket {
	public static final ResourceLocation LONG_ID = Packages.id("action"); //used on fabric probably
	public static final byte SHORT_ID = 0; //used on forge probably
	
	public ActionPacket(BlockPos pos, InteractionHand hand, PackageAction action) {
		this.pos = pos;
		this.hand = hand;
		this.action = action;
	}
	
	public BlockPos pos;
	public InteractionHand hand;
	public PackageAction action;
	
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(pos);
		buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
		action.write(buf);
	}
	
	public static ActionPacket read(FriendlyByteBuf buf) {
		return new ActionPacket(buf.readBlockPos(), buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, PackageAction.read(buf));
	}
}
