package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.Init;
import agency.highlysuspect.packages.block.PackageMakerBlockEntity;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class PackageMakerMenu extends AbstractContainerMenu {
	public PackageMakerMenu(int syncId, Inventory playerInventory) {
		this(syncId, playerInventory, new SimpleContainer(4));
	}
	
	public PackageMakerMenu(int syncId, Inventory playerInventory, Container container) {
		super(PMenuTypes.PACKAGE_MAKER, syncId);
		this.container = container;
		
		addSlot(new CanPlaceItemRespectingSlot(container, PackageMakerBlockEntity.OUTPUT_SLOT, 134, 35, null));
		addSlot(new CanPlaceItemRespectingSlot(container, PackageMakerBlockEntity.FRAME_SLOT, 26, 17, FRAME_BG));
		addSlot(new CanPlaceItemRespectingSlot(container, PackageMakerBlockEntity.INNER_SLOT, 26, 35, INNER_BG));
		addSlot(new CanPlaceItemRespectingSlot(container, PackageMakerBlockEntity.DYE_SLOT, 26, 53, DYE_BG));
		
		//lazy cut-paste from Generic3x3Container
		for(int row = 0; row < 3; ++row) {
			for(int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		
		for(int col = 0; col < 9; ++col) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}
	}
	
	public final Container container;
	
	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
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
	
	public static final ResourceLocation FRAME_BG = Init.id("gui/slot_frame");
	public static final ResourceLocation INNER_BG = Init.id("gui/slot_inner");
	public static final ResourceLocation DYE_BG = Init.id("gui/slot_dye");
	
	public static class CanPlaceItemRespectingSlot extends Slot {
		public CanPlaceItemRespectingSlot(Container inventory, int slot, int x, int y, @Nullable ResourceLocation background) {
			super(inventory, slot, x, y);
			this.background = background;
		}
		
		//remember to register this to the block/item texture atlas (see PackageMakerScreen#onInitializeClient)
		private final ResourceLocation background;
		
		@Override
		public boolean mayPlace(ItemStack stack) {
			//Regular Slot doesn't delegate to Container#canPlaceItem
			return container.canPlaceItem(getContainerSlot(), stack);
		}
		
		@Override
		@Environment(EnvType.CLIENT)
		@Nullable
		public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
			if(background == null) return null;
			return Pair.of(InventoryMenu.BLOCK_ATLAS, background);
		}
	}
}
