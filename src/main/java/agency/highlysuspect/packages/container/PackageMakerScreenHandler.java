package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.entity.PackageMakerBlockEntity;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class PackageMakerScreenHandler extends ScreenHandler {
	public static PackageMakerScreenHandler constructFromNetwork(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		
		BlockEntity be = inventory.player.world.getBlockEntity(pos);
		if(!(be instanceof PackageMakerBlockEntity)) throw new IllegalStateException("no package maker at " + pos.toString());
		
		return new PackageMakerScreenHandler(syncId, inventory, (PackageMakerBlockEntity) be);
	}
	
	public PackageMakerScreenHandler(int syncId, PlayerInventory playerInventory, PackageMakerBlockEntity be) {
		super(PScreenHandlers.PACKAGE_MAKER, syncId);
		this.be = be;
		
		addSlot(new WorkingSlot(be, PackageMakerBlockEntity.OUTPUT_SLOT, 134, 35, null));
		addSlot(new WorkingSlot(be, PackageMakerBlockEntity.FRAME_SLOT, 26, 17, FRAME_BG));
		addSlot(new WorkingSlot(be, PackageMakerBlockEntity.INNER_SLOT, 26, 35, INNER_BG));
		addSlot(new WorkingSlot(be, PackageMakerBlockEntity.DYE_SLOT, 26, 53, DYE_BG));
		
		//lazy cut-paste from Generic3x3Container
		int m, l;
		for(m = 0; m < 3; ++m) {
			for(l = 0; l < 9; ++l) {
				this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
			}
		}
		
		for(m = 0; m < 9; ++m) {
			this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
		}
	}
	
	public final PackageMakerBlockEntity be;
	
	@Override
	public boolean canUse(PlayerEntity player) {
		return be.canPlayerUse(player);
	}
	
	public ItemStack transferSlot(PlayerEntity player, int invSlot) {
		//Based on copy paste from generic3x3 container as well
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if (slot.hasStack()) {
			ItemStack itemStack2 = slot.getStack();
			itemStack = itemStack2.copy();
			if (invSlot < 4) {
				if (!this.insertItem(itemStack2, 4, 40, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItem(itemStack2, 0, 4, false)) {
				return ItemStack.EMPTY;
			}
			
			if (itemStack2.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
			
			if (itemStack2.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}
			
			slot.onTakeItem(player, itemStack2);
		}
		
		return itemStack;
	}
	
	public static final Identifier FRAME_BG = new Identifier(PackagesInit.MODID, "gui/slot_frame");
	public static final Identifier INNER_BG = new Identifier(PackagesInit.MODID, "gui/slot_inner");
	public static final Identifier DYE_BG = new Identifier(PackagesInit.MODID, "gui/slot_dye");
	
	public static class WorkingSlot extends Slot {
		public WorkingSlot(Inventory inventory, int invSlot, int xPosition, int yPosition, Identifier tex) {
			super(inventory, invSlot, xPosition, yPosition);
			this.invSlot2 = invSlot;
			this.tex = tex;
		}
		
		private final int invSlot2;
		private final Identifier tex;
		
		@Override
		public boolean canInsert(ItemStack stack) {
			return inventory.isValid(invSlot2, stack);
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public Pair<Identifier, Identifier> getBackgroundSprite() {
			if(tex == null) return null;
			return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, tex);
		}
	}
}
