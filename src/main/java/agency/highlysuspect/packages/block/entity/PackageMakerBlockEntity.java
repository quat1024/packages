package agency.highlysuspect.packages.block.entity;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PItemTags;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.Direction;

public class PackageMakerBlockEntity extends BlockEntity implements Nameable, SidedInventory, BlockEntityClientSerializable {
	public PackageMakerBlockEntity(BlockEntityType<?> type) {
		super(type);
	}
	
	public PackageMakerBlockEntity() {
		this(PBlockEntityTypes.PACKAGE_MAKER);
	}
	
	public static final int FRAME_SLOT = 0;
	public static final int INNER_SLOT = 1;
	public static final int DYE_SLOT = 2;
	public static final int OUTPUT_SLOT = 3;
	
	private final DefaultedList<ItemStack> inv = DefaultedList.ofSize(getInvSize(), ItemStack.EMPTY);
	
	public static boolean matchesFrameSlot(ItemStack stack) {
		Item item = stack.getItem();
		if(!(item instanceof BlockItem)) return false;
		if(item.isIn(PItemTags.BANNED_FROM_PACKAGE_MAKER)) return false;
		
		Block b = ((BlockItem) item).getBlock();
		BlockState state = b.getDefaultState();
		return state.isOpaque();
	}
	
	public static boolean matchesInnerSlot(ItemStack stack) {
		return matchesFrameSlot(stack);
	}
	
	public static boolean matchesDyeSlot(ItemStack stack) {
		return stack.getItem() instanceof DyeItem && !PItemTags.BANNED_FROM_PACKAGE_MAKER.contains(stack.getItem());
	}
	
	public ItemStack whatWouldBeCrafted() {
		if(inv.get(FRAME_SLOT).isEmpty() || inv.get(INNER_SLOT).isEmpty() || inv.get(DYE_SLOT).isEmpty()) return ItemStack.EMPTY;
		
		Block frameBlock = ((BlockItem) inv.get(FRAME_SLOT).getItem()).getBlock();
		Block innerBlock = ((BlockItem) inv.get(INNER_SLOT).getItem()).getBlock();
		DyeColor dye = ((DyeItem) inv.get(DYE_SLOT).getItem()).getColor();
		
		return PItems.PACKAGE.createCustomizedStack(frameBlock, innerBlock, dye);
	}
	
	public void performCraft() {
		ItemStack wouldCraft = whatWouldBeCrafted();
		if(wouldCraft.isEmpty()) return;
		
		ItemStack currentOutputStack = inv.get(OUTPUT_SLOT);
		if(currentOutputStack.isEmpty()) {
			inv.set(OUTPUT_SLOT, wouldCraft);
			
		} else {
			if(currentOutputStack.getCount() != currentOutputStack.getMaxCount() &&
				 currentOutputStack.isItemEqual(wouldCraft) &&
				 ItemStack.areTagsEqual(currentOutputStack, wouldCraft)) {
				currentOutputStack.increment(1);
			} else return;
		}
		
		inv.get(FRAME_SLOT).decrement(1);
		inv.get(INNER_SLOT).decrement(1);
		inv.get(DYE_SLOT).decrement(1);
		markDirty();
	}
	
	//<editor-fold desc="SidedInventory">
	public static final int[] FRAME_AND_DYE = new int[] {FRAME_SLOT, DYE_SLOT};
	public static final int[] INNER_AND_DYE = new int[] {INNER_SLOT, DYE_SLOT};
	public static final int[] OUTPUT = new int[] {OUTPUT_SLOT};
	
	@Override
	public int[] getInvAvailableSlots(Direction side) {
		if(side == Direction.DOWN) return OUTPUT;
		else if(side == Direction.UP) return FRAME_AND_DYE;
		else return INNER_AND_DYE;
	}
	
	@Override
	public boolean canInsertInvStack(int slot, ItemStack stack, Direction dir) {
		return isValidInvStack(slot, stack);
	}
	
	@Override
	public boolean canExtractInvStack(int slot, ItemStack stack, Direction dir) {
		return slot == OUTPUT_SLOT;
	}
	
	@Override
	public boolean isValidInvStack(int slot, ItemStack stack) {
		switch(slot) {
			case FRAME_SLOT: return matchesFrameSlot(stack);
			case INNER_SLOT: return matchesInnerSlot(stack);
			case DYE_SLOT: return matchesDyeSlot(stack);
			case OUTPUT_SLOT: default: return false;
		}
	}
	
	@Override
	public int getInvSize() {
		return 4;
	}
	
	@Override
	public boolean isInvEmpty() {
		for(ItemStack stack : inv) {
			if(!stack.isEmpty()) return false; 
		}
		
		return true;
	}
	
	@Override
	public ItemStack getInvStack(int slot) {
		return inv.get(slot);
	}
	
	@Override
	public ItemStack takeInvStack(int slot, int amount) {
		return Inventories.splitStack(inv, slot, amount);
	}
	
	@Override
	public ItemStack removeInvStack(int slot) {
		return Inventories.removeStack(inv, slot);
	}
	
	@Override
	public void setInvStack(int slot, ItemStack stack) {
		inv.set(slot, stack);
	}
	
	@Override
	public boolean canPlayerUseInv(PlayerEntity player) {
		return player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
	}
	
	@Override
	public void clear() {
		inv.clear();
	}
	//</editor-fold>
	
	//<editor-fold desc="Custom name cruft">
	private Text customName;
	
	@Override
	public Text getName() {
		return hasCustomName() ? customName : new TranslatableText(PBlocks.PACKAGE_MAKER.getTranslationKey());
	}
	
	@Override
	public boolean hasCustomName() {
		return customName != null;
	}
	
	@Override
	public Text getDisplayName() {
		return getName();
	}
	
	@Override
	public Text getCustomName() {
		return customName;
	}
	
	public void setCustomName(Text customName) {
		this.customName = customName;
	}
	//</editor-fold>
	
	//Serialization
	//Kind of a cheesy thing, but I happen to not have anything I don't want to send to the client
	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		if(customName != null) {
			tag.putString("CustomName", Text.Serializer.toJson(customName));
		}
		
		Inventories.toTag(tag, inv);
		return tag;
	}
	
	@Override
	public void fromClientTag(CompoundTag tag) {
		if(tag.contains("CustomName", 8)) {
			customName = Text.Serializer.fromJson(tag.getString("CustomName"));
		} else {
			customName = null;
		}
		
		Inventories.fromTag(tag, inv);
	}
	
	@Override
	public void markDirty() {
		if(world != null && !world.isClient) sync();
		super.markDirty();
	}
	
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		return super.toTag(toClientTag(tag));
	}
	
	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		fromClientTag(tag);
	}
}
