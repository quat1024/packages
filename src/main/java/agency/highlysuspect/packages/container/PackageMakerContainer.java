package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.block.entity.PackageMakerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PackageMakerContainer extends Container {
	public static PackageMakerContainer constructFromNetwork(int syncId, Identifier id, PlayerEntity player, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		
		World world = player.world;
		BlockEntity be = world.getBlockEntity(pos);
		
		if(!(be instanceof PackageMakerBlockEntity)) throw new IllegalStateException("no package maker at " + pos.toString());
		
		return new PackageMakerContainer(syncId, player, (PackageMakerBlockEntity) be);
	}
	
	public PackageMakerContainer(int syncId, PlayerEntity player, PackageMakerBlockEntity be) {
		super(null, syncId);
		this.playerInventory = player.inventory;
		this.be = be;
		
		addSlot(new WorkingSlot(be, PackageMakerBlockEntity.OUTPUT_SLOT, 116, 35));
		addSlot(new WorkingSlot(be, PackageMakerBlockEntity.FRAME_SLOT, 44, 17));
		addSlot(new WorkingSlot(be, PackageMakerBlockEntity.INNER_SLOT, 44, 35));
		addSlot(new WorkingSlot(be, PackageMakerBlockEntity.DYE_SLOT, 44, 53));
		
		//lazy cut-paste from Generic3x3Container
		int m, l;
		for(m = 0; m < 3; ++m) {
			for(l = 0; l < 9; ++l) {
				//change from +9 to +4...                               ...here
				this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
			}
		}
		
		for(m = 0; m < 9; ++m) {
			this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
		}
	}
	
	public final PlayerInventory playerInventory;
	public final PackageMakerBlockEntity be;
	
	@Override
	public boolean canUse(PlayerEntity player) {
		return be.canPlayerUseInv(player);
	}
	
	public ItemStack transferSlot(PlayerEntity player, int invSlot) {
		//Based on copy paste from generic3x3 container as well
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if (slot != null && slot.hasStack()) {
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
	
	public static class WorkingSlot extends Slot {
		public WorkingSlot(Inventory inventory, int invSlot, int xPosition, int yPosition) {
			super(inventory, invSlot, xPosition, yPosition);
			this.invSlot2 = invSlot;
		}
		int invSlot2;
		
		@Override
		public boolean canInsert(ItemStack stack) {
			return inventory.isValidInvStack(invSlot2, stack);
		}
	}
}
