package agency.highlysuspect.packages.net;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ActionPacket {
	public static final ResourceLocation LONG_ID = Packages.id("action"); //used on fabric
	public static final byte SHORT_ID = 0; //used on forge
	
	public ActionPacket(BlockPos pos, InteractionHand hand, PackageAction action) {
		this.pos = pos;
		this.hand = hand;
		this.action = action;
	}
	
	public final BlockPos pos;
	public final InteractionHand hand;
	public final PackageAction action;
	
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(pos);
		buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
		action.write(buf);
	}
	
	public static ActionPacket read(FriendlyByteBuf buf) {
		return new ActionPacket(buf.readBlockPos(), buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, PackageAction.read(buf));
	}
	
	public void handle(ServerPlayer sender) {
		sender.server.submit(() -> {
			PackageBlockEntity be = getPackageChecked(sender.level(), sender, pos);
			if(be != null) be.performAction(sender, hand, action, false);
		});
	}
	
	@SuppressWarnings("deprecation") //hasChunkAt
	private static @Nullable PackageBlockEntity getPackageChecked(Level level, Player player, BlockPos pos) {
		if(!level.hasChunkAt(pos) || player.blockPosition().distSqr(pos) > 8 * 8) return null;
		if(!(level.getBlockState(pos).getBlock() instanceof PackageBlock)) return null;
		return level.getBlockEntity(pos) instanceof PackageBlockEntity pbe ? pbe : null;
	}
}
