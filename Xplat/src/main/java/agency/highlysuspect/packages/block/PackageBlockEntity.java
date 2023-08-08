package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.PropsCommon;
import agency.highlysuspect.packages.junk.PTags;
import agency.highlysuspect.packages.junk.PackageContainer;
import agency.highlysuspect.packages.junk.PackageStyle;
import agency.highlysuspect.packages.net.PackageAction;
import agency.highlysuspect.packages.platform.SoftImplement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class PackageBlockEntity extends BlockEntity implements Container, Nameable {
	public PackageBlockEntity(BlockPos pos, BlockState state) {
		super(PBlockEntityTypes.PACKAGE.get(), pos, state);
	}
	
	private PackageStyle style = PackageStyle.ERROR_LOL;
	private final PackageContainer container = new PackageContainer().addListener(c -> this.setChanged());
	private ItemStack stickyStack = ItemStack.EMPTY;
	
	private Component customName;
	
	//BLANKETCON
	private boolean bcLocked;
	
	public PackageStyle getStyle() {
		return style;
	}
	
	public void setStyle(PackageStyle style) {
		this.style = style;
	}
	
	public PackageContainer getContainer() {
		return container;
	}
	
	//Stickiness
	public boolean canBeSticky() {
		if(level == null) return false;
		
		for(Direction dir : Direction.values()) {
			if(level.getBlockState(getBlockPos().relative(dir)).is(PTags.STICKY)) return true;
		}
		return false;
	}

	public boolean isSticky() {
		return canBeSticky() && !stickyStack.isEmpty();
	}
	
	public void updateStickyStack() {
		boolean anythingChanged = updateStickyStack0();
		if(anythingChanged) setChanged();
	}
	
	//Returns whether the sticky stack changed. A little caution is needed because updateStickyStack0 is called from setChanged...
	//this is because containers call setChanged when they change the contents, and if the stickystack is empty i need to watch for
	//items to be added to the container.
	private boolean updateStickyStack0() {
		//If the package is not allowed to be sticky, clear the sticky stack
		if(!canBeSticky()) {
			stickyStack = ItemStack.EMPTY;
			return true;
		}
		
		//If the package is allowed to be sticky, but doesn't have a filter stack, pick one.
		//Empty containers will return ItemStack.EMPTY here
		if(stickyStack.isEmpty() || (!stickyStack.isEmpty() && !container.isEmpty())) {
			stickyStack = container.getFilterStack().copy();
			return !stickyStack.isEmpty(); //return whether i picked up a new sticky stack
		}
		return false;
	}
	
	public ItemStack getStickyStack() {
		return stickyStack;
	}
	
	//<editor-fold desc="Interactions">
	public boolean performAction(Player player, InteractionHand hand, PackageAction action, boolean simulate) {
		if(bcLocked) { //BLANKETCON
			player.displayClientMessage(Component.translatable("container.isLocked", getDisplayName()), true);
			player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1f, 1f);
			return false;
		}
		
		boolean didAnything;
		if(action.isInsert()) didAnything = playerInsert(player, hand, action, simulate);
		else {
			didAnything = playerTakeDropLeftovers(player, hand, action, simulate);
			//If no real items were taken, try clearing the sticky stack
			if(!didAnything && !stickyStack.isEmpty()) {
				didAnything = true;
				stickyStack = ItemStack.EMPTY;
			}
		}
		
		updateStickyStack0();
		
		if(didAnything && level != null && Packages.instance.config.get(PropsCommon.INTERACTION_SOUNDS) && !player.hasEffect(MobEffects.INVISIBILITY)) { //hehe
			SoundEvent event = action.getSoundEvent();
			if(event != null) level.playSound(null, getBlockPos(), event, SoundSource.BLOCKS, action.getSoundVolume(), action.getSoundPitch(level));
		}
		
		return didAnything;
	}
	
	/**
	 * The Player inserts items from their inventory into the Package.
	 * @param player The Player who is inserting items.
	 * @param hand Which hand the player is interacting with.
	 * @param action Which inserting action the player is performing. Must not be a taking action.
	 * @param simulate If "true", a dry-run is performed.
	 * @return "true" if any items changed.
	 */
	private boolean playerInsert(Player player, InteractionHand hand, PackageAction action, boolean simulate) {
		int handSlot = handToSlotId(player, hand);
		if(!action.isInsert()) throw new IllegalArgumentException("playerInsert only supports insertion actions, not " + action);
		
		if(action == PackageAction.INSERT_ALL) {
			//Insert the stack of items that the player is holding, followed by stacks from the rest of the player's inventory.
			//If the player and package is not holding anything, look for the item type the player has the most of, and choose that.
			int favoriteSlot = player.getItemInHand(hand).isEmpty() && container.isEmpty() ? slotWithALot(player).orElse(handSlot) : handSlot;
			
			boolean didAnything = false;
			IntIterator iterator = handSlotFirst(player, favoriteSlot).intIterator();
			while(iterator.hasNext()) {
				int inserted = insert0(player, iterator.nextInt(), Integer.MAX_VALUE, simulate);
				if(inserted != 0) didAnything = true;
			}
			return didAnything;
		}
		
		int x = action == PackageAction.INSERT_ONE ? 1 : Integer.MAX_VALUE;
		if(container.isEmpty()) {
			//Only insert items from the player's hand slot, to avoid surprises.
			return insert0(player, handSlot, x, simulate) > 0;
		} else {
			//Start with the player's hand slot, but iterate through the rest of the inventory, to look for more similar items.
			IntIterator iterator = handSlotFirst(player, handSlot).intIterator();
			while(iterator.hasNext()) {
				int inserted = insert0(player, iterator.nextInt(), x, simulate);
				if(inserted != 0) return true;
			}
			return false;
		}
	}
	
	//Inserts items from the player's slot into the package, then places any leftovers back into the slot.
	private int insert0(Player player, int slot, int maxAmountToInsert, boolean simulate) {
		ItemStack toInsert = player.getInventory().getItem(slot).copy();
		ItemStack leftover = container.insert(toInsert, maxAmountToInsert, simulate);
		if(!simulate) player.getInventory().setItem(slot, leftover);
		return toInsert.getCount() - (leftover.isEmpty() ? 0 : leftover.getCount());
	}
	
	//Picks one of the slots containing the item type that the player has the most of.
	private Optional<Integer> slotWithALot(Player player) {
		//Make a frequency table of items
		Map<Item, MutableInt> runningTotal = new HashMap<>();
		for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack here = player.getInventory().getItem(i);
			if(here.isEmpty()) continue;
			if(!container.allowedInPackageAtAll(here)) continue;
			runningTotal.computeIfAbsent(here.getItem(), __ -> new MutableInt(0)).add(here.getCount());
		}
		
		return runningTotal.entrySet().stream()
			.max(Map.Entry.comparingByValue()) //Optional.Empty if runningTotal is empty
			.map(Map.Entry::getKey)
			.flatMap(item -> {
				//I forgot which slot the item belonged to, could you remind me?
				for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
					if(player.getInventory().getItem(i).getItem() == item) return Optional.of(i); //Thanks
				}
				return Optional.empty(); //Unreachable under normal circumstances
			});
	}
	
	/**
	 * The Player takes items from the Package.
	 * @param player The Player who is taking items.
	 * @param hand Which hand the player is interacting with.
	 * @param action Which taking action the player is performing. Must not be an insertion action.
	 * @param simulate If "true", a dry-run is performed.
	 * @return A tuple: whether the action was successful, and all leftover itemstacks that the player wanted to take but couldn't fit in their inventory.
	 */
	private PlayerTakeResult playerTake(Player player, InteractionHand hand, PackageAction action, boolean simulate) {
		int maxAmountToTake = switch(action) {
			case TAKE_ONE -> 1;
			case TAKE_STACK -> {
				ItemStack held = player.getItemInHand(hand);
				if(!held.isEmpty() && container.matches(held)) {
					//First, try to complete the stack in the player's hand without going over.
					int completionAmount = held.getMaxStackSize() - held.getCount();
					if(completionAmount > 0) yield completionAmount;
				}
				yield container.maxStackAmountAllowed(container.getFilterStack());
			}
			case TAKE_ALL -> Integer.MAX_VALUE;
			default -> throw new IllegalArgumentException("take() only supports taking actions, not " + action);
		};
		
		ItemStack toGiveOverstack = container.take(maxAmountToTake, simulate);
		if(toGiveOverstack.isEmpty()) return new PlayerTakeResult(false, Collections.emptyList());
		
		//TODO: Simulate adding items to player inventories and returning an accurate leftovers list, instead of faking it
		// This isn't very important
		if(simulate) return new PlayerTakeResult(true, Collections.emptyList());
		
		List<ItemStack> toGive = PackageContainer.flattenOverstack(toGiveOverstack);
		List<ItemStack> leftovers = new ArrayList<>();
		for(ItemStack stack : toGive) {
			if(!player.getInventory().add(stack)) leftovers.add(stack);
		}
		return new PlayerTakeResult(true, leftovers);
	}
	public record PlayerTakeResult(boolean successful, List<ItemStack> leftovers) {}
	
	@SuppressWarnings("SameParameterValue") //simulate == false, see above to-do comment
	private boolean playerTakeDropLeftovers(Player player, InteractionHand hand, PackageAction action, boolean simulate) {
		PlayerTakeResult result = playerTake(player, hand, action, simulate);
		if(simulate || !result.successful() || result.leftovers().isEmpty() || level == null) return result.successful();
		
		Vec3 spawnPos = Vec3.atCenterOf(getBlockPos()).add(new Vec3(getBlockState().getValue(PackageBlock.FACING).primaryDirection.step()).scale(0.8d));
		for(ItemStack stack : result.leftovers()) {
			ItemEntity e = new ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, stack, 0, 0.01, 0);
			e.setPickUpDelay(10);
			level.addFreshEntity(e);
		}
		return true;
	}
	
	//MAIN_HAND returns the slot ID of the current hotbar slot, OFF_HAND returns the offhand slot ID. Designed for player.getInventory().getItem().
	private static int handToSlotId(Player player, InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
	}
	
	//Returns the sequence [0..player.getInventory().getContainerSize()), but with the passed argument moved to the front.
	//Star ward refrence???
	private static IntList handSlotFirst(Player player, int handSlot) {
		int size = player.getInventory().getContainerSize();
		IntStream stream;
		if(handSlot == 0) {
			//The hand slot is already first, that's pretty convenient.
			stream = IntStream.range(0, size);
		} else if(handSlot == size) {
			//2 segments: size, then [0..size-1)
			stream = IntStream.concat(IntStream.of(handSlot), IntStream.range(0, size));
		} else {
			//3 segments: handSlot, [0..handSlot), [handSlot + 1..size)
			stream = IntStream.concat(IntStream.of(handSlot), IntStream.concat(IntStream.range(0, handSlot), IntStream.range(handSlot + 1, size)));
		}
		
		return stream.collect(IntArrayList::new, IntArrayList::add, IntArrayList::addAll);
	}
	//</editor-fold>
	
	//<editor-fold desc="Container">
	@Override
	public int getContainerSize() {
		return container.getContainerSize();
	}
	
	@Override
	public boolean isEmpty() {
		return container.isEmpty();
	}
	
	@Override
	public ItemStack getItem(int slot) {
		return container.getItem(slot);
	}
	
	@Override
	public ItemStack removeItem(int slot, int amount) {
		return container.removeItem(slot, amount);
	}
	
	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return container.removeItemNoUpdate(slot);
	}
	
	@Override
	public void setItem(int slot, ItemStack stack) {
		container.setItem(slot, stack);
	}
	
	@Override
	public int getMaxStackSize() {
		return container.getMaxStackSize();
	}
	
	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
	}
	
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if(!stickyStack.isEmpty() && !ItemStack.isSameItemSameTags(stickyStack, stack)) return false;
		return container.canPlaceItem(slot, stack);
	}
	
	@Override
	public void clearContent() {
		container.clearContent();
	}
	
	@Override
	public void setChanged() {
		updateStickyStack0();
		super.setChanged();
		if(level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
	}
	//</editor-fold>
	
	//<editor-fold desc="Nameable">
	@Override
	public Component getName() {
		return hasCustomName() ? customName : Component.translatable(PBlocks.PACKAGE.get().getDescriptionId());
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
	
	//<editor-fold desc="RenderAttachmentBlockEntity">
	@SuppressWarnings("unused")
	@SoftImplement("net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity")
	public Object getRenderAttachmentData() {
		return getStyle();
	}
	//</editor-fold>
	
	//Serialization
	@Override
	public void saveAdditional(CompoundTag tag) {
		tag.put(PackageContainer.KEY, container.toTag());
		tag.put(PackageStyle.KEY, style.toTag());
		
		if(customName != null) tag.putString("CustomName", Component.Serializer.toJson(customName));
		
		tag.put("StickyStack", stickyStack.save(new CompoundTag()));
		
		if(bcLocked) tag.putBoolean("bcLocked", true); //BLANKETCON
		
		super.saveAdditional(tag);
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		container.readFromTag(tag.getCompound(PackageContainer.KEY));
		style = PackageStyle.fromTag(tag.getCompound(PackageStyle.KEY));
		
		if(tag.contains("CustomName", 8)) customName = Component.Serializer.fromJson(tag.getString("CustomName"));
		else customName = null;
		
		stickyStack = ItemStack.of(tag.getCompound("StickyStack"));
		
		bcLocked = tag.contains("bcLocked") && tag.getBoolean("bcLocked"); //BLANKETCON
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
