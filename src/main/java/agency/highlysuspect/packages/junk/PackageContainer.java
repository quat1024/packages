package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.item.PackageItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

public class PackageContainer implements Container {
	public static final int SLOT_COUNT = 8;
	public static final int RECURSION_LIMIT = 3;
	public static final String KEY = "PackageContents";
	
	@VisibleForTesting //"the visibility of this field is deprecated, and it will be made private". Just havent migrated the clicking-on-pkg logic yet
	public final NonNullList<ItemStack> inv = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	
	private final List<ContainerListener> listeners = new ArrayList<>();
	
	/// listeners
	
	public void addListener(ContainerListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ContainerListener listener) {
		listeners.remove(listener);
	}
	
	/// helpers
	
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
	
	public int maxStackAmountAllowed(ItemStack stack) {
		if(stack.isEmpty()) return 64;
		else if(stack.getItem() instanceof PackageItem) {
			return stack.hasTag() ? 1 : 64;
//			//TODO reimpl
//			if(!stack.hasTag()) return 64;
//			CompoundTag beTag = stack.getTagElement("BlockEntityTag");
//			if(beTag == null) return 64;
//			CompoundTag contentsTag = beTag.getCompound(CONTENTS_KEY);
//			if(contentsTag.getInt("realCount") > 0) return 1;
//			else return 64;
		}
		else return Math.min(stack.getMaxStackSize(), 64); //just in case
	}
	
	private int calcPackageRecursion(ItemStack stack) {
		//TODO reimpl
		CompoundTag beTag = stack.getTagElement("BlockEntityTag");
		if(beTag != null) {
			CompoundTag contentsTag = beTag.getCompound("PackageContents");
			if(!contentsTag.isEmpty()) {
				int count = contentsTag.getInt("realCount");
				ItemStack containedStack = ItemStack.of(contentsTag.getCompound("stack"));
				if(count != 0 && !containedStack.isEmpty()) {
					return 1 + calcPackageRecursion(containedStack);
				}
			}
		}
		
		return 0;
	}
	
	//HopperBlockEntity.canMergeItems copy, with a modification
	private boolean canMergeItems(ItemStack first, ItemStack second) {
		if(first.isEmpty() || second.isEmpty()) return true; //My modification
		
		if (first.getItem() != second.getItem()) {
			return false;
		} else if (first.getDamageValue() != second.getDamageValue()) {
			return false;
		} else if (first.getCount() > first.getMaxStackSize()) {
			return false;
		} else {
			return ItemStack.tagMatches(first, second);
		}
	}
	
	public boolean matches(ItemStack stack) {
		return !stack.isEmpty() && canPlaceItem(0, stack);
	}
	
	/// Container
	
	@Override
	public int getContainerSize() {
		return SLOT_COUNT;
	}
	
	@Override
	public boolean isEmpty() {
		for(ItemStack stack : inv) {
			if(!stack.isEmpty()) return false;
		}
		return true;
	}
	
	@Override
	public ItemStack getItem(int slot) {
		return inv.get(slot);
	}
	
	@Override
	public ItemStack removeItem(int slot, int amount) {
		setChanged();
		return ContainerHelper.removeItem(inv, slot, amount);
	}
	
	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		setChanged();
		return ContainerHelper.takeItem(inv, slot);
	}
	
	@Override
	public void setItem(int slot, ItemStack stack) {
		inv.set(slot, stack);
		setChanged();
	}
	
	@Override
	public int getMaxStackSize() {
		return maxStackAmountAllowed(findFirstNonemptyStack());
	}
	
	@Override
	public boolean stillValid(Player player) {
		return true;
	}
	
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if(stack.getItem() == PItems.PACKAGE && calcPackageRecursion(stack) > RECURSION_LIMIT) return false;
		
		return canMergeItems(findFirstNonemptyStack(), stack);
	}
	
	@Override
	public void setChanged() {
		for(ContainerListener l : listeners) l.containerChanged(this);
	}
	
	@Override
	public void clearContent() {
		inv.clear();
		setChanged();
	}
	
	/// NBT guts
	
	public CompoundTag toTag() {
		return toTag(new CompoundTag());
	}
	
	public CompoundTag toTag(CompoundTag writeTo) {
		ItemStack first = findFirstNonemptyStack();
		if(!first.isEmpty()) {
			CompoundTag stackTag = findFirstNonemptyStack().save(new CompoundTag());
			stackTag.putByte("Count", (byte) 1);
			
			writeTo.put("stack", stackTag);
			writeTo.putInt("realCount", countItems());
		} else {
			writeTo.putInt("realCount", 0);
		}
		
		return writeTo;
	}
	
	public void fromTag(CompoundTag tag) {
		clearContent();
		int count = tag.getInt("realCount");
		if(count != 0) {
			ItemStack stack = ItemStack.of(tag.getCompound("stack"));
			int maxPerSlot = maxStackAmountAllowed(stack);
			
			for(int remaining = count, slot = 0; remaining > 0 && slot < SLOT_COUNT; remaining -= maxPerSlot, slot++) {
				ItemStack toInsert = stack.copy();
				toInsert.setCount(Math.min(remaining, maxPerSlot));
				setItem(slot, toInsert);
			}
		}
	}
	
	public static @Nullable PackageContainer fromItemStack(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if(tag == null) return null;
		
		PackageContainer r = new PackageContainer();
		r.fromTag(tag.getCompound("BlockEntityTag").getCompound(KEY));
		return r;
	}
	
	public ItemStack writeToStackTag(ItemStack stack) {
		stack.getOrCreateTagElement("BlockEntityTag").put(KEY, toTag());
		return stack;
	}
}
