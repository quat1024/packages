package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.item.PItems;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PackageContainer implements Container {
	/**
	 * The number of internal slots in the Package. Packages hold eight stacks of items.
	 */
	public static final int SLOT_COUNT = 8;
	
	/**
	 * Packages containing more than this number of layers of recursion are too heavy to insert into other Packages.
	 * A recursion limit of 3 allows inserting packages of packages of packages of things,
	 * but not packages of packages of packages of packages of things.
	 */
	public static final int RECURSION_LIMIT = 3;
	
	/**
	 * The location that PackageContainer data is conventionally stored in the Package's NBT tag.
	 * A convention is required so that the PackageContainer can be read back from the ItemStack.
	 */
	public static final String KEY = "PackageContents";
	
	/**
	 * The internal slots.
	 */
	private final NonNullList<ItemStack> inv = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	
	/// Listeners
	
	//Things that will be alerted when this PackageContainer changes. (As in SimpleContainer.)
	private final List<ContainerListener> listeners = new ArrayList<>();
	
	public PackageContainer addListener(ContainerListener listener) {
		listeners.add(listener);
		return this;
	}
	
	public void removeListener(ContainerListener listener) {
		listeners.remove(listener);
	}
	
	/// Helpers
	
	//The item currently inside the Package.
	public ItemStack getFilterStack() {
		for(ItemStack stack : inv) {
			if(!stack.isEmpty()) return stack;
		}
		return ItemStack.EMPTY;
	}
	
	//The number of items inside the Package.
	public int getCount() {
		int count = 0;
		for(ItemStack stack : inv) count += stack.getCount();
		return count;
	}
	
	public boolean isFull() {
		return Mth.equal(fillPercentage(), 1); //Mth.equal uses a suitable epsilon
	}
	
	public float fillPercentage() {
		int maxCount = maxStackAmountAllowed(getFilterStack()) * SLOT_COUNT;
		if(maxCount == 0) return 1;
		return getCount() / (float) maxCount;
	}
	
	//Whether you're ever allowed to put this item into a Package.
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean allowedInPackageAtAll(ItemStack stack) {
		if(stack.is(PItemTags.BANNED_FROM_PACKAGE)) return false;
		
		boolean checkCanFitInsideContainerItems = true;
		if(stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
			checkCanFitInsideContainerItems = false;
			CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
			if(blockEntityTag != null && !blockEntityTag.getList("Items", 10).isEmpty()) return false;
		}
		
		if(stack.getItem() instanceof BundleItem) {
			checkCanFitInsideContainerItems = false;
			if(stack.isBarVisible()) return false;
		}
		
		//canFitInsideContainerItems is a little overbearing; e.g. it doesn't make accomodations for *empty*
		//shulker boxes, which are totally safe to put into containers. So the hardcoded exceptions will bypass this check.
		//canFitInsideContainerItems doesn'tpass the ItemStack btw.
		if(checkCanFitInsideContainerItems && !stack.getItem().canFitInsideContainerItems()) return false;
		
		PackageContainer cont = PackageContainer.fromItemStack(stack);
		if(cont == null) return true;
		return cont.calcRecursionLevel() < RECURSION_LIMIT;
	}
	
	//The amount of items per-internal-slot that a Package is allowed to hold, if it contained `stack`.
	//Packages normally hold eight stacks of items, but to nerf nesting a bit, packages can only hold eight nonempty packages.
	//TODO: leaky abstraction, see comment in canPlaceItem
	public int maxStackAmountAllowed(ItemStack stack) {
		PackageContainer recur = fromItemStack(stack);
		if(recur != null && recur.getCount() > 0) return 1;
		else return Math.min(stack.getMaxStackSize(), 64);
	}
	
	//The amount of layers of nested Packages.
	//TODO: This looks expensive, cache it maybe. Invalidation...
	private int calcRecursionLevel() {
		PackageContainer recur = fromItemStack(getFilterStack());
		if(recur == null) return 0;
		else return 1 + recur.calcRecursionLevel();
	}
	
	//"true" if the itemstack is suitable for insertion into the Package.
	//Doesn't check things like "is the Package full". So this method is kind of useless.
	//Based kinda off HopperBlockEntity#canMergeItems but it's different
	public boolean matches(ItemStack stack) {
		ItemStack filter = getFilterStack();
		
		if(filter.isEmpty() || stack.isEmpty()) return true; //Empty stacks coerce to everything
		else return ItemStack.isSameItemSameTags(filter, stack);
	}
	
	//<editor-fold desc="Interactions">
	/**
	 * Inserts the ItemStack into the PackageContainer, possibly spreading it across multiple internal slots.
	 * @param toInsert The ItemStack to insert. Will not be mutated.
	 * @param maxAmountToInsert The maximum amount of items that will be inserted.
	 *                          This is a convenience, to avoid copying when you want to insert less than the full toInsert stack.
	 * @param simulate If "true", perform a dry run. The PackageContainer will not be mutated.
	 * @return The leftover portion of the ItemStack that did not fit in the PackageContainer, or ItemStack.EMPTY if it all fit.
	 *         Always returns a fresh ItemStack instance.
	 */
	public ItemStack insert(ItemStack toInsert, int maxAmountToInsert, boolean simulate) {
		//Check that the item fits in the Package at all
		if(toInsert.isEmpty() || !matches(toInsert) || !allowedInPackageAtAll(toInsert)) return toInsert;
		
		int remainingAmountToInsert = Math.min(toInsert.getCount(), maxAmountToInsert);
		int amountInserted = 0;
		int maxStackAmountPerSlot = maxStackAmountAllowed(toInsert);
		for(int slot = 0; slot < SLOT_COUNT && remainingAmountToInsert > 0; slot++) { //Iterate forward through the slots, as long as we have more items to insert.
			int amountToInsertThisSlot = Math.min(remainingAmountToInsert, maxStackAmountPerSlot); //Insert at most this many items into this slot.
			
			ItemStack currentStack = getItem(slot), newStack;
			if(currentStack.isEmpty()) {
				//If this slot is vacant, populate it with a new ItemStack.
				newStack = toInsert.copy();
				newStack.setCount(amountToInsertThisSlot);
			} else {
				//If the slot is occupied, try to increase its count with items from toInsert.
				int remainingSpaceThisSlot = maxStackAmountPerSlot - currentStack.getCount();
				if(remainingSpaceThisSlot <= 0) continue;
				amountToInsertThisSlot = Math.min(amountToInsertThisSlot, remainingSpaceThisSlot);
				
				newStack = simulate ? currentStack.copy() : currentStack; //It's newStack = currentStack.copy() but avoiding the alloc when it isn't needed
				newStack.grow(amountToInsertThisSlot);
			}
			
			remainingAmountToInsert -= amountToInsertThisSlot;
			amountInserted += amountToInsertThisSlot;
			if(!simulate) setItem(slot, newStack);
		}
		
		ItemStack remaining = toInsert.copy();
		remaining.shrink(amountInserted);
		return remaining;
	}
	
	/**
	 * Removes and returns up to maxAmountToTake items from the PackageContainer.
	 * @param maxAmountToTake The maximum amount of items to remove from the PackageContainer.
	 * @param simulate If "true", perform a dry run. The PackageContainer will not be mutated.
	 * @return An ItemStack representing the items removed from the PackageContainer.
	 *         Always returns a fresh ItemStack.
	 *         May return an "overstack" if maxAmountToTake > getFilterStack().getMaxStackSize().
	 * @see PackageContainer#flattenOverstack(ItemStack)
	 */
	public ItemStack take(int maxAmountToTake, boolean simulate) {
		//First, peek at which item is in the Package.
		//Calling getFilterStack after actually removing items from the Package will return ItemStack.EMPTY if we removed the last one.
		ItemStack filter = getFilterStack();
		if(filter.isEmpty()) return ItemStack.EMPTY;
		
		//Next, perform the taking operation
		int remainingAmountToTake = maxAmountToTake, amountTook = 0;
		for(int slot = SLOT_COUNT - 1; slot >= 0 && remainingAmountToTake > 0; slot--) { //Iterate backward through the slots, as long as we have more items to take.
			ItemStack currentStack = getItem(slot);
			if(currentStack.isEmpty()) continue;
			
			ItemStack newStack;
			int amountToTakeThisSlot = Math.min(remainingAmountToTake, currentStack.getCount()); //Nonzero
			
			if(amountToTakeThisSlot == currentStack.getCount()) {
				//Take all the items from the slot.
				newStack = ItemStack.EMPTY;
			} else {
				//Take some of the items from the slot.
				newStack = simulate ? currentStack.copy() : currentStack; //It's newStack = currentStack.copy() but avoiding the alloc when it isn't needed
				newStack.shrink(amountToTakeThisSlot);
			}
			
			remainingAmountToTake -= amountToTakeThisSlot;
			amountTook += amountToTakeThisSlot;
			if(!simulate) setItem(slot, newStack);
		}
		
		//Finally, construct the itemstack to be returned
		filter = filter.copy();
		filter.setCount(amountTook);
		return filter;
	}
	
	//Eg. 160x cobblestone -> [64x cobblestone, 64x cobblestone, 32x cobblestone]. Mutates its argument.
	public static List<ItemStack> flattenOverstack(ItemStack mutOverstack) {
		List<ItemStack> result = new ArrayList<>();
		while(!mutOverstack.isEmpty()) {
			//split(amt) takes min(stack.getCount(), amt)
			result.add(mutOverstack.split(mutOverstack.getMaxStackSize()));
		}
		return result;
	}
	//</editor-fold>
	
	//<editor-fold desc="Container">
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
		//Doesn't have a GUI in the first place
		return true;
	}
	
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if(!allowedInPackageAtAll(stack)) return false;
		
		//Hoppers don't seem to check Container#getMaxStackSize? Like, at all? Ok whatever.
		//Bandaid fix for them eating packages-with-items, then, which have a custom maxStackAmountAllowed
		PackageContainer hereContainer = fromItemStack(getItem(slot));
		if(hereContainer != null && !hereContainer.isEmpty()) return false;
		
		return matches(stack);
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
	//</editor-fold>
	
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
		if(stack.isEmpty() || stack.getItem() != PItems.PACKAGE.get()) return null;
		CompoundTag tag = stack.getTag();
		if(tag == null) return null;
		else return fromTag(tag.getCompound("BlockEntityTag").getCompound(KEY));
	}
	
	//Use this instead of fromItemStack() != null if you don't actually need the PackageContainer afterwards, it's cheaper.
	public static boolean existsOnItemStack(ItemStack stack) {
		if(stack.isEmpty() || stack.getItem() != PItems.PACKAGE.get()) return false;
		CompoundTag tag = stack.getTag();
		if(tag == null) return false;
		
		CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
		if(blockEntityTag == null || blockEntityTag.isEmpty()) return false;
		
		CompoundTag key = blockEntityTag.getCompound(KEY);
		return key != null && !key.isEmpty();
	}
	
	public ItemStack writeToStackTag(ItemStack stack) {
		stack.getOrCreateTagElement("BlockEntityTag").put(KEY, toTag());
		return stack;
	}
	
	/**
	 * Ensures that the modified PackageContainer is written back to the itemstack.
	 */
	public static <T> T mutateItemStack(ItemStack stack, Function<PackageContainer, T> action, T ifNoContainer) {
		PackageContainer cont = fromItemStack(stack);
		if(cont == null) return ifNoContainer;
		try {
			return action.apply(cont);
		} finally {
			cont.writeToStackTag(stack);
		}
	}
}
