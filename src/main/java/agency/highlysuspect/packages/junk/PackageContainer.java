package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.net.PackageAction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class PackageContainer implements Container {
	//The number of internal slots in the Package. Packages hold eight stacks of items, by design.
	public static final int SLOT_COUNT = 8;
	
	//Packages containing more than this number of layers of recursion are too heavy to insert into other Packages.
	//A recursion limit of 3 allows inserting packages of packages of packages of things,
	//but not packages of packages of packages of packages of things.
	public static final int RECURSION_LIMIT = 3;
	
	//The location that PackageContainer data is conventionally stored in the Package's NBT tag.
	//A convention is required so that the PackageContainer can be read back from the ItemStack.
	public static final String KEY = "PackageContents";
	
	@VisibleForTesting //"the visibility of this field is deprecated, and it will be made private". Just havent migrated the clicking-on-pkg logic yet
	public final NonNullList<ItemStack> inv = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	
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
	
	//Whether you're ever allowed to put this item into a Package.
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean allowedInPackageAtAll(ItemStack stack) {
		PackageContainer cont = PackageContainer.fromItemStack(stack);
		if(cont != null && cont.calcRecursionLevel() >= RECURSION_LIMIT) return false;
		else return !stack.is(PItemTags.BANNED_FROM_PACKAGE);
	}
	
	//The amount of items per-internal-slot that a Package is allowed to hold, if it contained `stack`.
	//Packages normally hold eight stacks of items, but to nerf nesting a bit, packages can only hold eight packages.
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
	//Based off HopperBlockEntity#canMergeItems or something
	public boolean matches(ItemStack stack) {
		ItemStack filter = getFilterStack();
		
		if(filter.isEmpty() || stack.isEmpty()) return true; //My modification: empty stacks coerce to everything
		else if(filter.getItem() != stack.getItem()) return false;
		else if(filter.getDamageValue() != stack.getDamageValue()) return false;
		else return ItemStack.tagMatches(filter, stack);
	}
	
	/// Interactions
	
	/**
	 * Inserts the ItemStack into the PackageContainer, possibly spreading it across multiple internal slots.
	 * @param toInsert The ItemStack to insert. Will not be mutated.
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
	 * The Player inserts items into the PackageContainer.
	 * @return true if at least one item was inserted into the inventory
	 */
	public boolean insert(Player player, InteractionHand hand, PackageAction action, boolean simulate) {
		int handSlot = handToSlotId(player, hand);
		
		if(action == PackageAction.INSERT_ALL) {
			//Insert the stack of items that the player is holding, followed by stacks from the rest of the player's inventory.
			//If the player and package is not holding anything, look for the item type the player has the most of, and choose that.
			int favoriteSlot;
			if(player.getItemInHand(hand).isEmpty() && isEmpty()) favoriteSlot = slotWithALot(player).orElse(handSlot);
			else favoriteSlot = handSlot;
			
			boolean didAnything = false;
			for(int slotToTry : handSlotFirst(player, favoriteSlot).boxed().toList()) {
				int inserted = insert0(player, slotToTry, Integer.MAX_VALUE, simulate);
				if(inserted != 0) didAnything = true;
			}
			return didAnything;
		}
		
		int x = action == PackageAction.INSERT_ONE ? 1 : Integer.MAX_VALUE;
		if(isEmpty()) {
			//Only insert items from the player's hand slot, to avoid surprises.
			return insert0(player, handSlot, x, simulate) > 0;
		} else {
			//Start with the player's hand slot, then iterate through the rest of the inventory.
			for(int slotToTry : handSlotFirst(player, handSlot).boxed().toList()) {
				int inserted = insert0(player, slotToTry, x, simulate);
				if(inserted != 0) return true;
			}
			return false;
		}
	}
	
	private int insert0(Player player, int insertionSlot, int maxAmountToInsert, boolean simulate) {
		ItemStack toInsert = player.getInventory().getItem(insertionSlot).copy();
		ItemStack leftover = insert(toInsert, maxAmountToInsert, simulate);
		if(!simulate) player.getInventory().setItem(insertionSlot, leftover);
		return toInsert.getCount() - (leftover.isEmpty() ? 0 : leftover.getCount());
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
	
	/**
	 * The Player takes items from the PackageContainer.
	 */
	public PlayerTakeResult take(Player player, InteractionHand hand, PackageAction action, boolean simulate) {
		int maxAmountToTake = switch(action) {
			case TAKE_ONE -> 1;
			case TAKE_STACK -> {
				ItemStack held = player.getItemInHand(hand);
				if(matches(held)) {
					//First, try to complete the stack in the player's hand without going over.
					int completionAmount = held.getMaxStackSize() - held.getCount();
					if(completionAmount > 0) yield completionAmount;
				}
				yield maxStackAmountAllowed(getFilterStack());
			}
			case TAKE_ALL -> Integer.MAX_VALUE;
			default -> throw new IllegalArgumentException();
		};
		
		ItemStack toGiveOverstack = take(maxAmountToTake, simulate);
		if(toGiveOverstack.isEmpty()) return new PlayerTakeResult(false, Collections.emptyList());
		
		//TODO: Simulate adding items to player inventories and returning an accurate leftovers count, instead of cheesing it
		if(simulate) return new PlayerTakeResult(true, Collections.emptyList());
		
		List<ItemStack> toGive = flattenOverstack(toGiveOverstack);
		List<ItemStack> leftovers = new ArrayList<>();
		for(ItemStack stack : toGive) {
			if(!player.getInventory().add(stack)) {
				leftovers.add(stack);
			}
		}
		return new PlayerTakeResult(true, leftovers);
	}
	public record PlayerTakeResult(boolean successful, List<ItemStack> leftovers) {}
	
	/**
	 * @param mutOverstack An ItemStack where getCount() is potentially greater than getMaxStackSize(). It is mutated and will be isEmpty by the end of the call
	 * @return A list of ItemStacks where each individual stack obeys getCount() <= getMaxStackSize().
	 */
	public static List<ItemStack> flattenOverstack(ItemStack mutOverstack) {
		List<ItemStack> result = new ArrayList<>();
		while(!mutOverstack.isEmpty()) {
			result.add(mutOverstack.split(Math.min(mutOverstack.getCount(), mutOverstack.getMaxStackSize())));
		}
		return result;
	}
	
	private static int handToSlotId(Player player, InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
	}
	
	//Star ward refrence???
	private static IntStream handSlotFirst(Player player, int handSlot) {
		int size = player.getInventory().getContainerSize();
		if(handSlot == 0) {
			//The hand slot is already first, that's pretty convenient.
			return IntStream.range(0, size);
		} else if(handSlot == size) {
			//2 segments: size, then [0..size-1)
			return IntStream.concat(IntStream.of(handSlot), IntStream.range(0, size));
		} else {
			//3 segments: handSlot, [0..handSlot), [handSlot + 1..size)
			return IntStream.concat(IntStream.of(handSlot), IntStream.concat(IntStream.range(0, handSlot), IntStream.range(handSlot + 1, size)));
		}
	}
	
	//Pick one of the slots containing the item type that the player has the most of.
	private Optional<Integer> slotWithALot(Player player) {
		//Make a frequency table of items
		Map<Item, MutableInt> runningTotal = new HashMap<>();
		for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack here = player.getInventory().getItem(i);
			if(here.isEmpty()) continue;
			if(!allowedInPackageAtAll(here)) continue;
			runningTotal.computeIfAbsent(here.getItem(), __ -> new MutableInt(0)).add(here.getCount());
		}
		
		return runningTotal.entrySet().stream()
			.max(Map.Entry.comparingByValue())
			.map(Map.Entry::getKey)
			.flatMap(item -> {
				//I forgot which slot the item belonged to, could you remind me?
				for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
					if(player.getInventory().getItem(i).getItem() == item) return Optional.of(i); //Thanks
				}
				return Optional.empty();
			});
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
