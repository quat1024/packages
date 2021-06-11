package agency.highlysuspect.packages.block.entity;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.container.PackageMakerScreenHandler;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PSoundEvents;
import agency.highlysuspect.packages.junk.PackageMakerRenderAttachment;
import agency.highlysuspect.packages.junk.PItemTags;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class PackageMakerBlockEntity extends BlockEntity implements Nameable, SidedInventory, BlockEntityClientSerializable, ExtendedScreenHandlerFactory, RenderAttachmentBlockEntity {
	public PackageMakerBlockEntity(BlockPos pos, BlockState state) {
		super(PBlockEntityTypes.PACKAGE_MAKER, pos, state);
	}
	
	//region Crafting logic
	public static final int FRAME_SLOT = 0;
	public static final int INNER_SLOT = 1;
	public static final int DYE_SLOT = 2;
	public static final int OUTPUT_SLOT = 3;
	
	private final DefaultedList<ItemStack> inv = DefaultedList.ofSize(size(), ItemStack.EMPTY);
	
	public static boolean matchesFrameSlot(ItemStack stack) {
		Item item = stack.getItem();
		if(!(item instanceof BlockItem)) return false;
		if(PItemTags.BANNED_FROM_PACKAGE_MAKER.contains(item)) return false;
		
		Block b = ((BlockItem) item).getBlock();
		BlockState state = b.getDefaultState();
		return state.isOpaque();
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
		DyeColor dye = ((DyeItem) inv.get(DYE_SLOT).getItem()).getColor();
		
		return PItems.PACKAGE.createCustomizedStack(frameBlock, innerBlock, dye);
	}
	
	public void performCraft() {
		ItemStack wouldCraft = whatWouldBeCrafted();
		if(wouldCraft.isEmpty()) return;
		
		ItemStack currentOutputStack = inv.get(OUTPUT_SLOT);
		if(currentOutputStack.isEmpty()) {
			inv.set(OUTPUT_SLOT, wouldCraft);
		} else {
			if(currentOutputStack.getCount() != currentOutputStack.getMaxCount() &&
				 currentOutputStack.isItemEqual(wouldCraft) &&
				 ItemStack.areTagsEqual(currentOutputStack, wouldCraft)) {
				currentOutputStack.increment(1);
			} else return;
		}
		
		inv.get(FRAME_SLOT).decrement(1);
		inv.get(INNER_SLOT).decrement(1);
		inv.get(DYE_SLOT).decrement(1);
		
		markDirty();
		
		//doubt it's null, lol
		if(world != null)	{
			world.playSound(null, pos, PSoundEvents.PACKAGE_MAKER_CRAFT, SoundCategory.BLOCKS, 0.5f, 1f);
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
		DyeColor dyeColor = !dyeStack.isEmpty() && dyeStack.getItem() instanceof DyeItem dyeItem ? dyeItem.getColor() : null;
		
		return new PackageMakerRenderAttachment(frameBlock, innerBlock, dyeColor);
	}
	//endregion
	
	//region ExtendedScreenHandlerFactory
	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		buf.writeBlockPos(pos);
	}
	
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new PackageMakerScreenHandler(syncId, inv, this);
	}
	//endregion
	
	//region SidedInventory
	public static final int[] FRAME_AND_DYE = new int[] {FRAME_SLOT, DYE_SLOT};
	public static final int[] INNER_AND_DYE = new int[] {INNER_SLOT, DYE_SLOT};
	public static final int[] OUTPUT = new int[] {OUTPUT_SLOT};
	
	@Override
	public int[] getAvailableSlots(Direction side) {
		if(side == Direction.DOWN) return OUTPUT;
		else if(side == Direction.UP) return FRAME_AND_DYE;
		else return INNER_AND_DYE;
	}
	
	@Override
	public boolean canInsert(int slot, ItemStack stack, Direction dir) {
		return isValid(slot, stack);
	}
	
	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return slot == OUTPUT_SLOT;
	}
	
	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return switch(slot) {
			case FRAME_SLOT -> matchesFrameSlot(stack);
			case INNER_SLOT -> matchesInnerSlot(stack);
			case DYE_SLOT -> matchesDyeSlot(stack);
			default -> false;
		};
	}
	
	@Override
	public int size() {
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
	public ItemStack getStack(int slot) {
		return inv.get(slot);
	}
	
	@Override
	public ItemStack removeStack(int slot, int amount) {
		return Inventories.splitStack(inv, slot, amount);
	}
	
	@Override
	public ItemStack removeStack(int slot) {
		return Inventories.removeStack(inv, slot);
	}
	
	@Override
	public void setStack(int slot, ItemStack stack) {
		inv.set(slot, stack);
	}
	
	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
	}
	
	@Override
	public void clear() {
		inv.clear();
	}
	//endregion
	
	//region Custom name cruft
	private Text customName;
	
	@Override
	public Text getName() {
		return hasCustomName() ? customName : new TranslatableText(PBlocks.PACKAGE_MAKER.getTranslationKey());
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
	//endregion
	
	//region Serialization
	@Override
	public NbtCompound toClientTag(NbtCompound tag) {
		if(customName != null) {
			tag.putString("CustomName", Text.Serializer.toJson(customName));
		}
		
		Inventories.writeNbt(tag, inv);
		return tag;
	}
	
	@Override
	public void fromClientTag(NbtCompound tag) {
		if(tag.contains("CustomName", 8)) {
			customName = Text.Serializer.fromJson(tag.getString("CustomName"));
		} else {
			customName = null;
		}
		
		Inventories.readNbt(tag, inv);
		
		//Im really sorry for this. How do you actually trigger a chunk rebuild from a blockentity sync these days?
		if(world != null && world.isClient) {
			BlockState state = world.getBlockState(pos);
			world.scheduleBlockRerenderIfNeeded(pos, Blocks.AIR.getDefaultState(), state);
		}
	}
	
	@Override
	public void markDirty() {
		if(world != null && !world.isClient) sync();
		super.markDirty();
	}
	
	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		return super.writeNbt(toClientTag(tag));
	}
	
	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		fromClientTag(tag);
	}
	//endregion
}
