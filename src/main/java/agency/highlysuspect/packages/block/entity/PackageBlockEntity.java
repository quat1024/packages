package agency.highlysuspect.packages.block.entity;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.item.PackageItem;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class PackageBlockEntity extends BlockEntity implements WorldlyContainer, RenderAttachmentBlockEntity, Nameable {
	public PackageBlockEntity(BlockPos pos, BlockState state) {
		super(PBlockEntityTypes.PACKAGE, pos, state);
	}
	
	public static final String CONTENTS_KEY = "PackageContents";
	public static final int SLOT_COUNT = 8;
	public static final int RECURSION_LIMIT = 3;
	
	private static final int[] NO_SLOTS = {};
	private static final int[] ALL_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7};
	
	private final NonNullList<ItemStack> inv = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	private PackageStyle style = PackageStyle.ERROR_LOL;
	private Component customName;
	
	@Override
	public Object getRenderAttachmentData() {
		return style;
	}
	
	public void setStyle(PackageStyle style) {
		this.style = style;
	}
	
	public CompoundTag writeContents() {
		CompoundTag tag = new CompoundTag();
		
		ItemStack first = findFirstNonemptyStack();
		if(!first.isEmpty()) {
			CompoundTag stackTag = findFirstNonemptyStack().save(new CompoundTag());
			stackTag.putByte("Count", (byte) 1);
			
			tag.put("stack", stackTag);
			tag.putInt("realCount", countItems());
		} else {
			tag.putInt("realCount", 0);
		}
		
		return tag;
	}
	
	public void readContents(CompoundTag tag) {
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
	
	//<editor-fold desc="Name cruft">
	@Override
	public Component getName() {
		return hasCustomName() ? customName : new TranslatableComponent(PBlocks.PACKAGE.getDescriptionId());
	}
	
	@Override
	public boolean hasCustomName() {
		return customName != null;
	}
	
	@Override
	public Component getDisplayName() {
		return getName();
	}
	
	@Override
	public Component getCustomName() {
		return customName;
	}
	
	public void setCustomName(Component customName) {
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
			CompoundTag beTag = stack.getTagElement("BlockEntityTag");
			if(beTag == null) return 64;
			CompoundTag contentsTag = beTag.getCompound(CONTENTS_KEY);
			if(contentsTag.getInt("realCount") > 0) return 1;
			else return 64;
		}
		else return Math.min(stack.getMaxStackSize(), 64); //just in case
	}
	
	//custom package-specific inventory wrappers, for external use
	public boolean matches(ItemStack stack) {
		return !stack.isEmpty() && canPlaceItem(0, stack);
	}
	
	//<editor-fold desc="Interactions">
	//Does not mutate 'held', always returns a different item stack.
	//kinda like forge item handlers lol...
	public void insert(Player player, InteractionHand hand, boolean fullStack) {
		ItemStack held = player.getItemInHand(hand);
		
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
					stack.grow(increaseAmount);
					amountToInsert -= increaseAmount;
					insertedAmount += increaseAmount;
				}
			}
		}
		
		ItemStack leftover = held.copy();
		leftover.shrink(insertedAmount);
		
		player.setItemInHand(hand, leftover);
		
		setChanged();
	}
	
	public void take(Player player, boolean fullStack) {
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
			if(!player.getInventory().add(stack)) {
				player.drop(stack, false);
			}
		});
		
		setChanged();
	}
	//</editor-fold>
	
	//<editor-fold desc="WorldlyContainer">
	@Override
	public int[] getSlotsForFace(Direction side) {
		if(level == null) return NO_SLOTS;
		
		BlockState state = level.getBlockState(worldPosition);
		if(state.getBlock() instanceof PackageBlock) {
			return state.getValue(PackageBlock.FACING).primaryDirection == side ? NO_SLOTS : ALL_SLOTS;
		}
		
		return NO_SLOTS;
	}
	
	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return canPlaceItem(slot, stack);
	}
	
	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return true;
	}
	
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
	
	private int calcPackageRecursion(ItemStack stack) {
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
	
	@Override
	public void clearContent() {
		inv.clear();
	}
	//</editor-fold>
	
	//HopperBlockEntity.canMergeItems copy, with a modification
	private static boolean canMergeItems(ItemStack first, ItemStack second) {
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
	
	//Serialization
	@Override
	public void saveAdditional(CompoundTag tag) {
		//Contents
		tag.put(CONTENTS_KEY, writeContents());
		
		//Style
		tag.put(PackageStyle.KEY, style.toTag());
		
		//Custom name
		if(customName != null) {
			tag.putString("CustomName", Component.Serializer.toJson(customName));
		}
		
		super.saveAdditional(tag);
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		
		//Contents
		readContents(tag.getCompound(CONTENTS_KEY));
		
		//Style
		style = PackageStyle.fromTag(tag.getCompound(PackageStyle.KEY));
		
		//Custom name
		if(tag.contains("CustomName", 8)) {
			customName = Component.Serializer.fromJson(tag.getString("CustomName"));
		} else {
			customName = null;
		}
	}
	
	@Override
	public void setChanged() {
		super.setChanged();
		if(level != null && !level.isClientSide) {
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
		}
	}
	
	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}
}
