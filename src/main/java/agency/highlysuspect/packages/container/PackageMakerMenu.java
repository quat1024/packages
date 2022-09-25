package agency.highlysuspect.packages.container;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PackageMakerBlockEntity;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class PackageMakerMenu extends AbstractContainerMenu {
	public PackageMakerMenu(int syncId, Inventory playerInventory) {
		this(syncId, playerInventory, new SimpleContainer(PackageMakerBlockEntity.SIZE));
	}
	
	public PackageMakerMenu(int syncId, Inventory playerInventory, Container container) {
		super(PMenuTypes.PACKAGE_MAKER.get(), syncId);
		this.container = container;
		
		addSlot(new FunkySlot(container, PackageMakerBlockEntity.FRAME_SLOT ,  16, 25, FRAME_BG, PackageMakerBlockEntity::matchesFrameSlot));
		addSlot(new FunkySlot(container, PackageMakerBlockEntity.INNER_SLOT ,  36, 25, INNER_BG, PackageMakerBlockEntity::matchesInnerSlot));
		addSlot(new FunkySlot(container, PackageMakerBlockEntity.DYE_SLOT   ,  16, 45, DYE_BG  , PackageMakerBlockEntity::matchesDyeSlot));
		addSlot(new FunkySlot(container, PackageMakerBlockEntity.EXTRA_SLOT ,  36, 45, EXTRA_BG, PackageMakerBlockEntity::matchesExtraSlot));
		addSlot(new FunkySlot(container, PackageMakerBlockEntity.OUTPUT_SLOT, 134, 35, null, stack -> false));
		
		//lazy cut-paste from Generic3x3Container
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 9; col++) {
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		
		for(int col = 0; col < 9; col++) addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
	}
	
	@Override
	public boolean clickMenuButton(Player player, int id) {
		if(id == 0 || id == 1) {
			if(container instanceof PackageMakerBlockEntity be) {
				be.performCraft((id == 1) ? 64 : 1);
				return true;
			}
		}
		
		return false;
	}
	
	public final Container container;
	
	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
	}
	
	public ItemStack quickMoveStack(Player player, int invSlot) {
		//Based on copy paste from generic3x3 container as well
		ItemStack what = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if(slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			what = itemStack2.copy();
			if(invSlot < PackageMakerBlockEntity.SIZE) {
				if(!this.moveItemStackTo(itemStack2, PackageMakerBlockEntity.SIZE, PackageMakerBlockEntity.SIZE + 36, true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.moveItemStackTo(itemStack2, 0, PackageMakerBlockEntity.SIZE, false)) {
				return ItemStack.EMPTY;
			}
			
			if (itemStack2.isEmpty()) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
			if (itemStack2.getCount() == what.getCount()) return ItemStack.EMPTY;
			slot.onTake(player, itemStack2);
		}
		
		return what;
	}
	
	public static final ResourceLocation FRAME_BG = Packages.id("gui/slot_frame");
	public static final ResourceLocation INNER_BG = Packages.id("gui/slot_inner");
	public static final ResourceLocation DYE_BG = Packages.id("gui/slot_dye");
	public static final ResourceLocation EXTRA_BG = Packages.id("gui/slot_extra");
	
	public static class FunkySlot extends Slot {
		public FunkySlot(Container inventory, int slot, int x, int y, @Nullable ResourceLocation background, Predicate<ItemStack> mayPlace) {
			super(inventory, slot, x, y);
			this.background = background;
			this.mayPlace = mayPlace;
		}
		
		//remember to register this to the block/item texture atlas (see PackageMakerScreen#onInitializeClient)
		private final ResourceLocation background;
		private final Predicate<ItemStack> mayPlace;
		
		@Override
		public boolean mayPlace(ItemStack stack) {
			return mayPlace.test(stack);
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
