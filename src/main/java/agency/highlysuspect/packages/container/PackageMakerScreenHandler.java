package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.entity.PackageMakerBlockEntity;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PackageMakerScreenHandler extends AbstractContainerMenu {
	public static PackageMakerScreenHandler constructFromNetwork(int syncId, Inventory inventory, FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		
		BlockEntity be = inventory.player.level.getBlockEntity(pos);
		if(!(be instanceof PackageMakerBlockEntity)) throw new IllegalStateException("no package maker at " + pos.toString());
		
		return new PackageMakerScreenHandler(syncId, inventory, (PackageMakerBlockEntity) be);
	}
	
	public PackageMakerScreenHandler(int syncId, Inventory playerInventory, PackageMakerBlockEntity be) {
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
	public boolean stillValid(Player player) {
		return be.stillValid(player);
	}
	
	public ItemStack quickMoveStack(Player player, int invSlot) {
		//Based on copy paste from generic3x3 container as well
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if (slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (invSlot < 4) {
				if (!this.moveItemStackTo(itemStack2, 4, 40, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 0, 4, false)) {
				return ItemStack.EMPTY;
			}
			
			if (itemStack2.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			
			if (itemStack2.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}
			
			slot.onTake(player, itemStack2);
		}
		
		return itemStack;
	}
	
	public static final ResourceLocation FRAME_BG = new ResourceLocation(PackagesInit.MODID, "gui/slot_frame");
	public static final ResourceLocation INNER_BG = new ResourceLocation(PackagesInit.MODID, "gui/slot_inner");
	public static final ResourceLocation DYE_BG = new ResourceLocation(PackagesInit.MODID, "gui/slot_dye");
	
	public static class WorkingSlot extends Slot {
		public WorkingSlot(Container inventory, int invSlot, int xPosition, int yPosition, ResourceLocation tex) {
			super(inventory, invSlot, xPosition, yPosition);
			this.invSlot2 = invSlot;
			this.tex = tex;
		}
		
		private final int invSlot2;
		private final ResourceLocation tex;
		
		@Override
		public boolean mayPlace(ItemStack stack) {
			return container.canPlaceItem(invSlot2, stack);
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
			if(tex == null) return null;
			return Pair.of(InventoryMenu.BLOCK_ATLAS, tex);
		}
	}
}
