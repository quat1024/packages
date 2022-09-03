package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.item.PItems;
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
	
	//The item currently inside the Package.
	public ItemStack getFilterStack() {
		for(ItemStack stack : inv) {
			if(!stack.isEmpty()) return stack;
		}
		return ItemStack.EMPTY;
	}
	
	//THe number of items inside the Package.
	public int getCount() {
		int count = 0;
		for(ItemStack stack : inv) count += stack.getCount();
		return count;
	}
	
	//The amount of items per-internal-slot that this Package is allowed to hold.
	//Packages normally hold eight stacks of items, but to nerf nesting a bit, packages can only hold eight packages.
	public int maxStackAmountAllowed(ItemStack stack) {
		PackageContainer recur = fromItemStack(stack);
		if(recur != null && recur.getCount() > 0) return 1;
		else return Math.min(stack.getMaxStackSize(), 64);
	}
	
	//The amount of layers of nested Packages.
	private int calcRecursionLevel() {
		PackageContainer recur = fromItemStack(getFilterStack());
		if(recur == null) return 0;
		else return 1 + recur.calcRecursionLevel();
	}
	
	//HopperBlockEntity.canMergeItems copy, with modifications
	private boolean canMergeItems(ItemStack first, ItemStack second) {
		if(first.isEmpty() || second.isEmpty()) return true; //My modification: empty stacks coerce to everything
		else if(first.getItem() != second.getItem()) return false;
		else if(first.getDamageValue() != second.getDamageValue()) return false;
		//else if(first.getCount() > first.getMaxStackSize()) return false; //This is from vanilla, idk what it's all about, yes it's really "first" twice
		else if(first.getCount() + second.getCount() > maxStackAmountAllowed(second)) return false; //My modification: respect custom maxStackAmountAllowed semantics
		else return ItemStack.tagMatches(first, second);
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
		return maxStackAmountAllowed(getFilterStack());
	}
	
	@Override
	public boolean stillValid(Player player) {
		return true;
	}
	
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		PackageContainer containerToInsert = fromItemStack(stack);
		if(containerToInsert != null && containerToInsert.calcRecursionLevel() > RECURSION_LIMIT) return false;
		
		return canMergeItems(getFilterStack(), stack);
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
		ItemStack first = getFilterStack();
		if(!first.isEmpty()) {
			CompoundTag stackTag = getFilterStack().save(new CompoundTag());
			stackTag.putByte("Count", (byte) 1);
			
			writeTo.put("stack", stackTag);
			writeTo.putInt("realCount", getCount());
		} else {
			writeTo.putInt("realCount", 0);
		}
		
		return writeTo;
	}
	
	public void readFromTag(CompoundTag tag) {
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
	
	public static PackageContainer fromTag(CompoundTag tag) {
		PackageContainer r = new PackageContainer();
		r.readFromTag(tag);
		return r;
	}
	
	public static @Nullable PackageContainer fromItemStack(ItemStack stack) {
		if(stack.isEmpty() || stack.getItem() != PItems.PACKAGE) return null;
		
		CompoundTag tag = stack.getTag();
		if(tag == null) return null;
		else return fromTag(tag.getCompound("BlockEntityTag").getCompound(KEY));
	}
	
	public ItemStack writeToStackTag(ItemStack stack) {
		stack.getOrCreateTagElement("BlockEntityTag").put(KEY, toTag());
		return stack;
	}
}
