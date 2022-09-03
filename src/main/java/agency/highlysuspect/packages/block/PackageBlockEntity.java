package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.junk.PackageContainer;
import agency.highlysuspect.packages.junk.PackageStyle;
import agency.highlysuspect.packages.net.PackageAction;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Hi, you caught me in the middle of a big refactor.
 * So one of the big things with inventory mods is that inventories can live inside of real block entities, but
 * on ItemStacks they are ephemeral; the best you can do is read the inventory off the NBT tag, modify it however
 * you need, then write the result back to the NBT tag. This leads to blobs of ad-hoc NBT handling code gumming
 * up the codebase. It's already annoying, and for the features I want to add (like being able to click items into
 * the package from your inventory), it is untenable, and I need a new system.
 * 
 * Ideally I will be able to inintialize PackageContainer from a raw NBT tag, interface with it the same way I
 * can interface with the package block entity (all the interaction-heavy code should live there), then write back
 * to the item stack. And have the same API surface that I do with the block entity.
 * 
 * But it's late and I'm pretty tired, so I'm doing the refactor piecemeal.
 */
public class PackageBlockEntity extends BlockEntity implements Container, RenderAttachmentBlockEntity, Nameable {
	public PackageBlockEntity(BlockPos pos, BlockState state) {
		super(PBlockEntityTypes.PACKAGE, pos, state);
	}
	
	private PackageStyle style = PackageStyle.ERROR_LOL;
	private final PackageContainer container = new PackageContainer();
	{ container.addListener(c -> this.setChanged()); }
	
	private Component customName;
	
	@Override
	public Object getRenderAttachmentData() {
		return getStyle();
	}
	
	public PackageStyle getStyle() {
		return style;
	}
	
	public void setStyle(PackageStyle style) {
		this.style = style;
	}
	
	public PackageContainer getContainer() {
		return container;
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
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	public ItemStack findFirstNonemptyStack() {
		return container.getFilterStack();
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	public int countItems() {
		return container.getCount();
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	public int maxStackAmountAllowed(ItemStack stack) {
		return container.maxStackAmountAllowed(stack);
	}
	
	//custom package-specific inventory wrappers, for external use
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	public boolean matches(ItemStack stack) {
		return container.matches(stack);
	}
	
	//<editor-fold desc="Interactions">
	public void performAction(Player player, InteractionHand hand, PackageAction action) {
		if(action.isInsert()) insert(player, hand, action);
		else take(player, action);
	}
	
	//Does not mutate 'held', always returns a different item stack.
	//kinda like forge item handlers lol...
	public void insert(Player player, InteractionHand hand, PackageAction action) {
		ItemStack held = player.getItemInHand(hand);
		
		if(held.isEmpty() || !matches(held)) {
			int matchingSlot = player.getInventory().findSlotMatchingItem(findFirstNonemptyStack());
			if(matchingSlot == -1) {
				return;
			}
			held = player.getInventory().getItem(matchingSlot);
			ItemStack leftover = placeIntoPackage(held, action);
			player.getInventory().setItem(matchingSlot, leftover);
			return;
		}
		ItemStack leftover = placeIntoPackage(held, action);
		player.setItemInHand(hand, leftover);
	}

	private ItemStack placeIntoPackage(ItemStack insertStack, PackageAction action) {
		//Will never be more than one stack
		int amountToInsert = Math.min(maxStackAmountAllowed(insertStack), action == PackageAction.INSERT_STACK ? insertStack.getCount() : 1);
		int insertedAmount = 0;
		
		ListIterator<ItemStack> stackerator = container.inv.listIterator();
		while(amountToInsert > 0 && stackerator.hasNext()) {
			ItemStack stack = stackerator.next();
			
			if(stack.isEmpty()) {
				ItemStack newStack = insertStack.copy();
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
		setChanged();
		ItemStack leftover = insertStack.copy();
		leftover.shrink(insertedAmount);
		return leftover;
	}
	
	public void take(Player player, PackageAction action) {
		ItemStack contained = findFirstNonemptyStack();
		if(contained.isEmpty()) return;
		
		int removeTotal = action == PackageAction.TAKE_STACK ? maxStackAmountAllowed(contained) : 1;
		List<ItemStack> stacksToGive = new ArrayList<>();
		
		ListIterator<ItemStack> stackerator = container.inv.listIterator();
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
	
	//<editor-fold desc="Container">
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	@Override
	public int getContainerSize() {
		return container.getContainerSize();
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	@Override
	public boolean isEmpty() {
		return container.isEmpty();
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	@Override
	public ItemStack getItem(int slot) {
		return container.getItem(slot);
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	@Override
	public ItemStack removeItem(int slot, int amount) {
		return container.removeItem(slot, amount);
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return container.removeItemNoUpdate(slot);
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	@Override
	public void setItem(int slot, ItemStack stack) {
		container.setItem(slot, stack);
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	@Override
	public int getMaxStackSize() {
		return container.getMaxStackSize();
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return container.canPlaceItem(slot, stack);
	}
	
	/**
	 * @deprecated Migrate to PackageContainer instead
	 */
	@Deprecated
	@Override
	public void clearContent() {
		container.clearContent();
	}
	//</editor-fold>
	
	//Serialization
	@Override
	public void saveAdditional(CompoundTag tag) {
		tag.put(PackageContainer.KEY, container.toTag());
		tag.put(PackageStyle.KEY, style.toTag());
		if(customName != null) {
			tag.putString("CustomName", Component.Serializer.toJson(customName));
		}
		super.saveAdditional(tag);
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		container.readFromTag(tag.getCompound(PackageContainer.KEY));
		style = PackageStyle.fromTag(tag.getCompound(PackageStyle.KEY));
		if(tag.contains("CustomName", 8)) {
			customName = Component.Serializer.fromJson(tag.getString("CustomName"));
		} else customName = null;
	}
	
	@Override
	public void setChanged() {
		super.setChanged();
		if(level != null && !level.isClientSide) {
			//TODO: What does it do
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
