package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.container.PackageMakerMenu;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PSoundEvents;
import agency.highlysuspect.packages.junk.PTags;
import agency.highlysuspect.packages.junk.PackageMakerStyle;
import agency.highlysuspect.packages.platform.SoftImplement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
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

public class PackageMakerBlockEntity extends BlockEntity implements Nameable, WorldlyContainer, MenuProvider {
	public PackageMakerBlockEntity(BlockPos pos, BlockState state) {
		super(PBlockEntityTypes.PACKAGE_MAKER.get(), pos, state);
	}
	
	//region Crafting logic
	public static final int FRAME_SLOT = 0;
	public static final int INNER_SLOT = 1;
	public static final int DYE_SLOT = 2;
	public static final int EXTRA_SLOT = 3;
	public static final int OUTPUT_SLOT = 4;
	public static final int SIZE = OUTPUT_SLOT + 1;
	
	private final NonNullList<ItemStack> inv = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
	
	@SuppressWarnings("RedundantIfStatement")
	public static boolean matchesFrameSlot(ItemStack stack) {
		if(!matchesFrameOrInnerSlotLogic(stack)) return false;
		if(Packages.instance.config.packageMakerAllowlistMode && !stack.is(PTags.ALLOWLIST_PACKAGE_MAKER_FRAME)) return false;
		
		return true;
	}
	
	@SuppressWarnings("RedundantIfStatement")
	public static boolean matchesInnerSlot(ItemStack stack) {
		if(!matchesFrameOrInnerSlotLogic(stack)) return false;
		if(Packages.instance.config.packageMakerAllowlistMode && !stack.is(PTags.ALLOWLIST_PACKAGE_MAKER_INNER)) return false;
		return true;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted") //shush
	private static boolean matchesFrameOrInnerSlotLogic(ItemStack stack) {
		if(stack.isEmpty()) return false;
		
		Item item = stack.getItem();
		if(!(item instanceof BlockItem)) return false;
		if(stack.is(PTags.BANNED_FROM_PACKAGE_MAKER)) return false;
		
		Block b = ((BlockItem) item).getBlock();
		BlockState state = b.defaultBlockState();
		return state.canOcclude();
	}
	
	public static boolean matchesDyeSlot(ItemStack stack) {
		if(stack.isEmpty()) return false;
		if(stack.is(PTags.BANNED_FROM_PACKAGE_MAKER)) return false;
		return stack.getItem() instanceof DyeItem;
	}
	
	public static boolean matchesExtraSlot(ItemStack stack) {
		if(stack.isEmpty()) return false;
		else return stack.is(PTags.THINGS_YOU_NEED_FOR_PACKAGE_CRAFTING);
	}
	
	//Static because it's called from PackageMakerScreen, which doesn't have a blockentity available, to show the preview slot
	
	public static ItemStack whatWouldBeCrafted(Container container) {
		ItemStack frame = container.getItem(FRAME_SLOT);
		ItemStack inner = container.getItem(INNER_SLOT);
		ItemStack dye = container.getItem(DYE_SLOT);
		ItemStack extra = container.getItem(EXTRA_SLOT);
		
		if(!matchesFrameSlot(frame)) return ItemStack.EMPTY;
		if(!matchesInnerSlot(inner)) return ItemStack.EMPTY;
		if(!matchesDyeSlot(dye)) return ItemStack.EMPTY;
		if(!matchesExtraSlot(extra)) return ItemStack.EMPTY;
		
		Block frameBlock = ((BlockItem) frame.getItem()).getBlock();
		Block innerBlock = ((BlockItem) inner.getItem()).getBlock();
		DyeColor dyeColor = ((DyeItem) dye.getItem()).getDyeColor();
		
		return PItems.PACKAGE.get().createCustomizedStack(frameBlock, innerBlock, dyeColor);
	}
	
	///
	
	public ItemStack whatWouldBeCrafted() {
		return whatWouldBeCrafted(this);
	}
	
	public void performCraft() {
		performCraft(1);
	}
	
	public void performCraft(int max) {
		boolean playedSound = false;
		
		for(int i = 0; i < max; i++) {
			ItemStack wouldCraft = whatWouldBeCrafted();
			if(wouldCraft.isEmpty()) return;
			
			ItemStack currentOutputStack = inv.get(OUTPUT_SLOT);
			if(currentOutputStack.isEmpty()) {
				inv.set(OUTPUT_SLOT, wouldCraft);
			} else {
				if(currentOutputStack.getCount() != currentOutputStack.getMaxStackSize() && ItemStack.isSameItemSameTags(currentOutputStack, wouldCraft)) {
					currentOutputStack.grow(1);
				} else return; //doesn't fit!
			}
			
			inv.get(FRAME_SLOT).shrink(1);
			inv.get(INNER_SLOT).shrink(1);
			inv.get(DYE_SLOT).shrink(1);
			inv.get(EXTRA_SLOT).shrink(1);
			
			setChanged();
			
			//doubt it's null, lol
			if(level != null && !playedSound)	{
				level.playSound(null, worldPosition, PSoundEvents.PACKAGE_MAKER_CRAFT, SoundSource.BLOCKS, 1f, 1f);
				playedSound = true;
			}
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
	
	public boolean hasExtra() {
		return !inv.get(EXTRA_SLOT).isEmpty();
	}
	
	public boolean hasOutput() {
		return !inv.get(OUTPUT_SLOT).isEmpty();
	}
	//endregion
	
	//region RenderAttachmentBlockEntity
	@SuppressWarnings("unused") //Fabric implements RenderAttachmentBlockEntity on all BlockEntities.
	@SoftImplement("net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity")
	public @Nullable Object getRenderAttachmentData() {
		return getStyle();
	}
	
	public PackageMakerStyle getStyle() {
		ItemStack frameStack = inv.get(FRAME_SLOT);
		ItemStack innerStack = inv.get(INNER_SLOT);
		ItemStack dyeStack = inv.get(DYE_SLOT);
		
		Block frameBlock = !frameStack.isEmpty() && frameStack.getItem() instanceof BlockItem frameItem ? frameItem.getBlock() : null;
		Block innerBlock = !innerStack.isEmpty() && innerStack.getItem() instanceof BlockItem innerItem ? innerItem.getBlock() : null;
		DyeColor dyeColor = !dyeStack.isEmpty() && dyeStack.getItem() instanceof DyeItem dyeItem ? dyeItem.getDyeColor() : null;
		
		return new PackageMakerStyle(frameBlock, innerBlock, dyeColor);
	}
	//endregion
	
	//region WorldlyContainer (f.k.a. SidedInventory)
	public static final int[] FRAME_AND_MISC = new int[] {FRAME_SLOT, DYE_SLOT, EXTRA_SLOT};
	public static final int[] INNER_AND_MISC = new int[] {INNER_SLOT, DYE_SLOT, EXTRA_SLOT};
	public static final int[] OUTPUT = new int[] {OUTPUT_SLOT};
	
	@Override
	public int[] getSlotsForFace(Direction side) {
		if(side == Direction.DOWN) return OUTPUT;
		else if(side == Direction.UP) return FRAME_AND_MISC;
		else return INNER_AND_MISC;
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
			case EXTRA_SLOT -> matchesExtraSlot(stack);
			default -> false;
		};
	}
	
	@Override
	public int getContainerSize() {
		return SIZE;
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
	
	public void setChanged() {
		super.setChanged();
		if(level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
	}
	//endregion
	
	//region MenuProvider
	@Override
	public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
		return new PackageMakerMenu(syncId, inv, this);
	}
	
	private Component customName;
	
	@Override
	public Component getName() {
		return hasCustomName() ? customName : Component.translatable(PBlocks.PACKAGE_MAKER.get().getDescriptionId(), new Object[]{});
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
		tag.putInt("packages$dataVersion", 1);
		
		if(hasCustomName()) {
			tag.putString("CustomName", Component.Serializer.toJson(customName));
		}
		
		ContainerHelper.saveAllItems(tag, inv);
	}
	
	@Override
	public void load(CompoundTag tag) {
		int dataVersion = tag.getInt("packages$dataVersion");
		if(tag.contains("CustomName", 8)) {
			customName = Component.Serializer.fromJson(tag.getString("CustomName"));
		} else {
			customName = null;
		}
		
		inv.clear();
		ContainerHelper.loadAllItems(tag, inv);
		
		if(dataVersion == 0) {
			//the "extra" slot didn't exist yet; need to move slot 3 (old output slot) to slot 4 (new output slot)
			inv.set(4, inv.get(3));
			inv.set(3, ItemStack.EMPTY);
		}
		
		//Force a chunk rerender when the contents of the container change.
		//Or, yknow, really when any nbt changes. It's a bit sloppy.
		//The "sided proxy" thing is overkill - compare this:
		// https://github.com/QuiltMC/quilt-standard-libraries/blob/6a3d728ee74d158170c6b23efb3297625d2b266a/library/block/block_entity/src/testmod/java/org/quiltmc/qsl/block/entity/test/ColorfulBlockEntity.java#L71
		//I still feel like it's weird that nonexistent classes can be referenced on the *interior* of a method as long as it's never called?
		//Idk i don't trust it lol. (Is this defined behavior in the jvm spec or does it just happen to work on hotspot)
		if(level != null && level.isClientSide()) {
			Packages.instance.proxy.forceChunkRerender(level, getBlockPos());
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
