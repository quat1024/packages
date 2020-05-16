package agency.highlysuspect.packages.block.entity;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.item.PItems;
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
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class PackageBlockEntity extends BlockEntity implements SidedInventory, RenderAttachmentBlockEntity, BlockEntityClientSerializable, Nameable {
	public PackageBlockEntity(BlockEntityType<?> type) {
		super(type);
	}
	
	public PackageBlockEntity() {
		this(PBlockEntityTypes.PACKAGE);
	}
	
	public static final String CONTENTS_KEY = "PackageContents";
	public static final int SLOT_COUNT = 8;
	public static final int RECURSION_LIMIT = 3;
	
	private static final int[] NO_SLOTS = {};
	private static final int[] ALL_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7};
	
	private DefaultedList<ItemStack> inv = DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY);
	private PackageStyle style = PackageStyle.FALLBACK;
	private Text customName;
	
	@Override
	public Object getRenderAttachmentData() {
		return style;
	}
	
	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		tag.put(PackageStyle.KEY, style.toTag());
		tag.put(CONTENTS_KEY, writeContents());
		return tag;
	}
	
	@Override
	public void fromClientTag(CompoundTag tag) {
		style = PackageStyle.fromTag(tag.getCompound(PackageStyle.KEY));
		readContents(tag.getCompound(CONTENTS_KEY));
		
		if(world != null) { //Which it probably never is, but IntelliJ is having a fit
			BlockState help = world.getBlockState(pos);
			world.updateListeners(pos, help, help, 8);
		}
	}
	
	public void setStyle(PackageStyle style) {
		this.style = style;
	}
	
	public CompoundTag writeContents() {
		CompoundTag tag = new CompoundTag();
		
		ItemStack first = findFirstNonemptyStack();
		if(!first.isEmpty()) {
			CompoundTag stackTag = findFirstNonemptyStack().toTag(new CompoundTag());
			stackTag.putByte("Count", (byte) 1);
			
			tag.put("stack", stackTag);
			tag.putInt("realCount", countItems());
		} else {
			tag.putInt("realCount", 0);
		}
		
		return tag;
	}
	
	public void readContents(CompoundTag tag) {
		clear();
		int count = tag.getInt("realCount");
		if(count != 0) {
			ItemStack stack = ItemStack.fromTag(tag.getCompound("stack"));
			int maxPerSlot = maxStackAmountAllowed(stack);
			
			for(int remaining = count, slot = 0; remaining > 0 && slot < SLOT_COUNT; remaining -= maxPerSlot, slot++) {
				ItemStack toInsert = stack.copy();
				toInsert.setCount(Math.min(remaining, maxPerSlot));
				setStack(slot, toInsert);
			}
		}
	}
	
	//<editor-fold desc="Name cruft">
	@Override
	public Text getName() {
		return hasCustomName() ? customName : new TranslatableText(PBlocks.PACKAGE.getTranslationKey());
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
	
	//Inventory stuff.
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
		else if(stack.getItem() instanceof PackageItem) {
			//TODO clean this up
			if(!stack.hasTag()) return 64;
			CompoundTag beTag = stack.getSubTag("BlockEntityTag");
			if(beTag == null) return 64;
			CompoundTag contentsTag = beTag.getCompound(CONTENTS_KEY);
			if(contentsTag.getInt("realCount") > 0) return 1;
			else return 64;
		}
		else return Math.min(stack.getMaxCount(), 64); //just in case
	}
	
	//custom package-specific inventory wrappers, for external use
	public boolean matches(ItemStack stack) {
		return !stack.isEmpty() && isValid(0, stack);
	}
	
	//<editor-fold desc="Interactions">
	//Does not mutate 'held', always returns a different item stack.
	//kinda like forge item handlers lol...
	public void insert(PlayerEntity player, Hand hand, boolean fullStack) {
		ItemStack held = player.getStackInHand(hand);
		
		if(held.isEmpty() || !matches(held)) return;
		
		//Will never be more than one stack
		int amountToInsert = Math.min(maxStackAmountAllowed(held), fullStack ? held.getCount() : 1);
		int insertedAmount = 0;
		
		ListIterator<ItemStack> stackerator = inv.listIterator();
		while(amountToInsert > 0 && stackerator.hasNext()) {
			ItemStack stack = stackerator.next();
			
			if(stack.isEmpty()) {
				ItemStack newStack = held.copy();
				newStack.setCount(amountToInsert);
				insertedAmount += amountToInsert;
				stackerator.set(newStack);
				break;
			} else {
				int increaseAmount = Math.min(maxStackAmountAllowed(stack) - stack.getCount(), amountToInsert);
				if(increaseAmount > 0) {
					stack.increment(increaseAmount);
					amountToInsert -= increaseAmount;
					insertedAmount += increaseAmount;
				}
			}
		}
		
		ItemStack leftover = held.copy();
		leftover.decrement(insertedAmount);
		
		player.setStackInHand(hand, leftover);
		
		markDirty();
	}
	
	public void take(PlayerEntity player, boolean fullStack) {
		ItemStack contained = findFirstNonemptyStack();
		if(contained.isEmpty()) return;
		
		int removeTotal = fullStack ? maxStackAmountAllowed(contained) : 1;
		List<ItemStack> stacksToGive = new ArrayList<>();
		
		ListIterator<ItemStack> stackerator = inv.listIterator();
		while(removeTotal > 0 && stackerator.hasNext()) {
			ItemStack stack = stackerator.next();
			if(stack.isEmpty()) continue;
			
			int remove = Math.min(stack.getCount(), removeTotal);
			stacksToGive.add(stack.split(remove));
			removeTotal -= remove;
		}
		
		stacksToGive.forEach(stack -> {
			if(!player.inventory.insertStack(stack)) {
				player.dropItem(stack, false);
			}
		});
		
		markDirty();
	}
	//</editor-fold>
	
	//<editor-fold desc="SidedInventory interface">
	@Override
	public void markDirty() {
		if(world != null && !world.isClient) sync();
		super.markDirty();
	}
	
	//More inventory bullshit
	@Override
	public int[] getAvailableSlots(Direction side) {
		if(world == null) return NO_SLOTS;
		
		BlockState state = world.getBlockState(pos);
		if(state.getBlock() instanceof PackageBlock) {
			return state.get(PackageBlock.FACING).primaryDirection == side ? NO_SLOTS : ALL_SLOTS;
		}
		
		return NO_SLOTS;
	}
	
	@Override
	public boolean canInsert(int slot, ItemStack stack, Direction dir) {
		return isValid(slot, stack);
	}
	
	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return true;
	}
	
	@Override
	public int size() {
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
	public ItemStack getStack(int slot) {
		return inv.get(slot);
	}
	
	@Override
	public ItemStack removeStack(int slot, int amount) {
		markDirty();
		return Inventories.splitStack(inv, slot, amount);
	}
	
	@Override
	public ItemStack removeStack(int slot) {
		markDirty();
		return Inventories.removeStack(inv, slot);
	}
	
	@Override
	public void setStack(int slot, ItemStack stack) {
		inv.set(slot, stack);
		markDirty();
	}
	
	@Override
	public int getMaxCountPerStack() {
		return maxStackAmountAllowed(findFirstNonemptyStack());
	}
	
	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return true;
	}
	
	@Override
	public boolean isValid(int slot, ItemStack stack) {
		if(stack.getItem() == PItems.PACKAGE && calcPackageRecursion(stack) > RECURSION_LIMIT) return false;
		
		return canMergeItems(findFirstNonemptyStack(), stack);
	}
	
	private int calcPackageRecursion(ItemStack stack) {
		CompoundTag beTag = stack.getSubTag("BlockEntityTag");
		if(beTag != null) {
			CompoundTag contentsTag = beTag.getCompound("PackageContents");
			if(!contentsTag.isEmpty()) {
				int count = contentsTag.getInt("realCount");
				ItemStack containedStack = ItemStack.fromTag(contentsTag.getCompound("stack"));
				if(count != 0 && !containedStack.isEmpty()) {
					return 1 + calcPackageRecursion(containedStack);
				}
			}
		}
		
		return 0;
	}
	
	@Override
	public void clear() {
		inv.clear();
	}
	//</editor-fold>
	
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
		tag.put(CONTENTS_KEY, writeContents());
		
		//Style
		tag.put(PackageStyle.KEY, style.toTag());
		
		//Custom name
		if(customName != null) {
			tag.putString("CustomName", Text.Serializer.toJson(customName));
		}
		
		return super.toTag(tag);
	}
	
	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);
		
		//Contents
		readContents(tag.getCompound(CONTENTS_KEY));
		
		//Style
		style = PackageStyle.fromTag(tag.getCompound(PackageStyle.KEY));
		
		//Custom name
		if(tag.contains("CustomName", 8)) {
			customName = Text.Serializer.fromJson(tag.getString("CustomName"));
		} else {
			customName = null;
		}
	}
}
