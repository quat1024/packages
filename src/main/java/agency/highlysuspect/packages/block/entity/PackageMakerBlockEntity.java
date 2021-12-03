package agency.highlysuspect.packages.block.entity;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.container.PackageMakerScreenHandler;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PSoundEvents;
import agency.highlysuspect.packages.junk.PackageMakerRenderAttachment;
import agency.highlysuspect.packages.junk.PItemTags;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PackageMakerBlockEntity extends BlockEntity implements Nameable, WorldlyContainer, ExtendedScreenHandlerFactory, RenderAttachmentBlockEntity {
	public PackageMakerBlockEntity(BlockPos pos, BlockState state) {
		super(PBlockEntityTypes.PACKAGE_MAKER, pos, state);
	}
	
	//region Crafting logic
	public static final int FRAME_SLOT = 0;
	public static final int INNER_SLOT = 1;
	public static final int DYE_SLOT = 2;
	public static final int OUTPUT_SLOT = 3;
	
	private final NonNullList<ItemStack> inv = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
	
	public static boolean matchesFrameSlot(ItemStack stack) {
		Item item = stack.getItem();
		if(!(item instanceof BlockItem)) return false;
		if(PItemTags.BANNED_FROM_PACKAGE_MAKER.contains(item)) return false;
		
		Block b = ((BlockItem) item).getBlock();
		BlockState state = b.defaultBlockState();
		return state.canOcclude();
	}
	
	public static boolean matchesInnerSlot(ItemStack stack) {
		return matchesFrameSlot(stack);
	}
	
	public static boolean matchesDyeSlot(ItemStack stack) {
		return stack.getItem() instanceof DyeItem && !PItemTags.BANNED_FROM_PACKAGE_MAKER.contains(stack.getItem());
	}
	
	public ItemStack whatWouldBeCrafted() {
		if(!(hasFrame() && hasInner() && hasDye())) return ItemStack.EMPTY;
		
		Block frameBlock = ((BlockItem) inv.get(FRAME_SLOT).getItem()).getBlock();
		Block innerBlock = ((BlockItem) inv.get(INNER_SLOT).getItem()).getBlock();
		DyeColor dye = ((DyeItem) inv.get(DYE_SLOT).getItem()).getDyeColor();
		
		return PItems.PACKAGE.createCustomizedStack(frameBlock, innerBlock, dye);
	}
	
	public void performCraft() {
		ItemStack wouldCraft = whatWouldBeCrafted();
		if(wouldCraft.isEmpty()) return;
		
		ItemStack currentOutputStack = inv.get(OUTPUT_SLOT);
		if(currentOutputStack.isEmpty()) {
			inv.set(OUTPUT_SLOT, wouldCraft);
		} else {
			if(currentOutputStack.getCount() != currentOutputStack.getMaxStackSize() &&
				 currentOutputStack.sameItemStackIgnoreDurability(wouldCraft) &&
				 ItemStack.tagMatches(currentOutputStack, wouldCraft)) {
				currentOutputStack.grow(1);
			} else return;
		}
		
		inv.get(FRAME_SLOT).shrink(1);
		inv.get(INNER_SLOT).shrink(1);
		inv.get(DYE_SLOT).shrink(1);
		
		setChanged();
		
		//doubt it's null, lol
		if(level != null)	{
			level.playSound(null, worldPosition, PSoundEvents.PACKAGE_MAKER_CRAFT, SoundSource.BLOCKS, 0.5f, 1f);
		}
	}
	
	public boolean hasFrame() {
		return !inv.get(FRAME_SLOT).isEmpty();
	}
	
	public boolean hasInner() {
		return !inv.get(INNER_SLOT).isEmpty();
	}
	
	public boolean hasDye() {
		return !inv.get(DYE_SLOT).isEmpty();
	}
	
	public boolean hasOutput() {
		return !inv.get(OUTPUT_SLOT).isEmpty();
	}
	//endregion
	
	//region RenderAttachmentBlockEntity
	@Override
	public @Nullable Object getRenderAttachmentData() {
		ItemStack frameStack = inv.get(FRAME_SLOT);
		ItemStack innerStack = inv.get(INNER_SLOT);
		ItemStack dyeStack = inv.get(DYE_SLOT);
		
		Block frameBlock = !frameStack.isEmpty() && frameStack.getItem() instanceof BlockItem frameItem ? frameItem.getBlock() : null;
		Block innerBlock = !innerStack.isEmpty() && innerStack.getItem() instanceof BlockItem innerItem ? innerItem.getBlock() : null;
		DyeColor dyeColor = !dyeStack.isEmpty() && dyeStack.getItem() instanceof DyeItem dyeItem ? dyeItem.getDyeColor() : null;
		
		return new PackageMakerRenderAttachment(frameBlock, innerBlock, dyeColor);
	}
	//endregion
	
	//region ExtendedScreenHandlerFactory
	@Override
	public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
		buf.writeBlockPos(worldPosition);
	}
	
	@Override
	public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
		return new PackageMakerScreenHandler(syncId, inv, this);
	}
	//endregion
	
	//region WorldlyContainer (f.k.a. SidedInventory)
	public static final int[] FRAME_AND_DYE = new int[] {FRAME_SLOT, DYE_SLOT};
	public static final int[] INNER_AND_DYE = new int[] {INNER_SLOT, DYE_SLOT};
	public static final int[] OUTPUT = new int[] {OUTPUT_SLOT};
	
	@Override
	public int[] getSlotsForFace(Direction side) {
		if(side == Direction.DOWN) return OUTPUT;
		else if(side == Direction.UP) return FRAME_AND_DYE;
		else return INNER_AND_DYE;
	}
	
	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return canPlaceItem(slot, stack);
	}
	
	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return slot == OUTPUT_SLOT;
	}
	
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return switch(slot) {
			case FRAME_SLOT -> matchesFrameSlot(stack);
			case INNER_SLOT -> matchesInnerSlot(stack);
			case DYE_SLOT -> matchesDyeSlot(stack);
			default -> false;
		};
	}
	
	@Override
	public int getContainerSize() {
		return 4;
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
		return ContainerHelper.removeItem(inv, slot, amount);
	}
	
	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(inv, slot);
	}
	
	@Override
	public void setItem(int slot, ItemStack stack) {
		inv.set(slot, stack);
	}
	
	@Override
	public boolean stillValid(Player player) {
		return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64;
	}
	
	@Override
	public void clearContent() {
		inv.clear();
	}
	//endregion
	
	//region Custom name cruft
	private Component customName;
	
	@Override
	public Component getName() {
		return hasCustomName() ? customName : new TranslatableComponent(PBlocks.PACKAGE_MAKER.getDescriptionId());
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
	//endregion
	
	//region Serialization
	@Override
	public void saveAdditional(CompoundTag tag) {
		if(hasCustomName()) {
			tag.putString("CustomName", Component.Serializer.toJson(customName));
		}
		
		ContainerHelper.saveAllItems(tag, inv);
	}
	
	@Override
	public void load(CompoundTag tag) {
		if(tag.contains("CustomName", 8)) {
			customName = Component.Serializer.fromJson(tag.getString("CustomName"));
		} else {
			customName = null;
		}
		
		ContainerHelper.loadAllItems(tag, inv);
	}
	
	//TODO kind of a hack
	@Override
	public void setChanged() {
		super.setChanged();
		if(level != null) {
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
	//endregion
}
