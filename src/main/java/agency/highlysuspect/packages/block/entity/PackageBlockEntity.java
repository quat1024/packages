package agency.highlysuspect.packages.block.entity;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.item.PackageItem;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PackageBlockEntity extends BlockEntity implements SidedInventory, RenderAttachmentBlockEntity, BlockEntityClientSerializable {
	public PackageBlockEntity(BlockEntityType<?> type) {
		super(type);
	}
	
	public PackageBlockEntity() {
		this(PBlockEntityTypes.PACKAGE);
	}
	
	private static final String CONTENTS_KEY = "PackageContents";
	
	private static final int SLOT_COUNT = 8;
	private static final int[] NO_SLOTS = {};
	private static final int[] ALL_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7};
	
	private DefaultedList<ItemStack> inv = DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY);
	private PackageStyle style = PackageStyle.FALLBACK;
	
	@Override
	public Object getRenderAttachmentData() {
		return style;
	}
	
	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		tag.put(PackageStyle.KEY, style.toTag());
		return tag;
	}
	
	@Override
	public void fromClientTag(CompoundTag tag) {
		style = PackageStyle.fromTag(tag.getCompound(PackageStyle.KEY));
	}
	
	public void setStyleAndSync(World world, PackageStyle style) {
		this.style = style;
		if(!world.isClient) sync();
	}
	
	public ItemStack findFirstNonemptyStack() {
		for(ItemStack stack : inv) {
			if(!stack.isEmpty()) return stack;
		}
		return ItemStack.EMPTY;
	}
	
	public int countItems() {
		int count = 0;
		for(ItemStack stack : inv) count += stack.getCount();
		return count;
	}
	
	public static int maxStackAmountAllowed(ItemStack stack) {
		if(stack.isEmpty()) return 64;
		else if(stack.getItem() instanceof PackageItem) return 1; //TODO only return 1 when the package is not empty
		else return Math.min(stack.getMaxCount(), 64); //just in case
	}
	
	//Inventory bullshit
	@Override
	public int[] getInvAvailableSlots(Direction side) {
		if(world == null) return NO_SLOTS;
		
		BlockState state = world.getBlockState(pos);
		if(state.getBlock() instanceof PackageBlock) {
			return state.get(PackageBlock.FACING).primaryDirection == side ? NO_SLOTS : ALL_SLOTS;
		}
		
		return NO_SLOTS;
	}
	
	@Override
	public boolean canInsertInvStack(int slot, ItemStack stack, Direction dir) {
		return isValidInvStack(slot, stack);
	}
	
	@Override
	public boolean canExtractInvStack(int slot, ItemStack stack, Direction dir) {
		return true;
	}
	
	@Override
	public int getInvSize() {
		return SLOT_COUNT;
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
		markDirty();
		return Inventories.splitStack(inv, slot, amount);
	}
	
	@Override
	public ItemStack removeInvStack(int slot) {
		markDirty();
		return Inventories.removeStack(inv, slot);
	}
	
	@Override
	public void setInvStack(int slot, ItemStack stack) {
		inv.set(slot, stack);
		markDirty();
	}
	
	@Override
	public int getInvMaxStackAmount() {
		return maxStackAmountAllowed(findFirstNonemptyStack());
	}
	
	@Override
	public boolean canPlayerUseInv(PlayerEntity player) {
		return true;
	}
	
	@Override
	public boolean isValidInvStack(int slot, ItemStack stack) {
		return canMergeItems(findFirstNonemptyStack(), stack);
	}
	
	@Override
	public void clear() {
		inv.clear();
	}
	
	//HopperBlockEntity.canMergeItems copy, with a modification
	private static boolean canMergeItems(ItemStack first, ItemStack second) {
		if(first.isEmpty() || second.isEmpty()) return true; //My modification
		
		if (first.getItem() != second.getItem()) {
			return false;
		} else if (first.getDamage() != second.getDamage()) {
			return false;
		} else if (first.getCount() > first.getMaxCount()) {
			return false;
		} else {
			return ItemStack.areTagsEqual(first, second);
		}
	}
	
	//Serialization
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		//Contents
		CompoundTag contents = new CompoundTag();
		
		ItemStack first = findFirstNonemptyStack();
		if(!first.isEmpty()) {
			CompoundTag stackTag = findFirstNonemptyStack().toTag(new CompoundTag());
			stackTag.putByte("Count", (byte) 1);
			
			contents.put("stack", stackTag);
			contents.putInt("realCount", countItems());
		} else {
			contents.putInt("realCount", 0);
		}
		
		tag.put(CONTENTS_KEY, contents);
		
		//Style
		tag.put(PackageStyle.KEY, style.toTag());
		
		return super.toTag(tag);
	}
	
	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		
		//Contents
		clear();
		CompoundTag contents = tag.getCompound(CONTENTS_KEY);
		int count = contents.getInt("realCount");
		if(count != 0) {
			ItemStack stack = ItemStack.fromTag(contents.getCompound("stack"));
			int maxPerSlot = maxStackAmountAllowed(stack);
			
			for(int remaining = count, slot = 0; remaining > 0 && slot < SLOT_COUNT; remaining -= maxPerSlot, slot++) {
				ItemStack toInsert = stack.copy();
				toInsert.setCount(Math.min(remaining, maxPerSlot));
				setInvStack(slot, toInsert);
			}
		}
		
		//Style
		style = PackageStyle.fromTag(tag.getCompound(PackageStyle.KEY));
	}
}
