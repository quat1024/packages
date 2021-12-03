package agency.highlysuspect.packages.block.entity;

import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.container.Overstack;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PackageBlockEntity extends BlockEntity implements RenderAttachmentBlockEntity, Nameable {
	public PackageBlockEntity(BlockPos pos, BlockState state) {
		super(PBlockEntityTypes.PACKAGE, pos, state);
	}
	
	public static final String STORAGE_KEY = "Storage";
	public static final int RECURSION_LIMIT = 3;
	
	private PackageStyle style = PackageStyle.ERROR_LOL;
	private final PackageStorage storage = new PackageStorage();
	private Component customName;
	
	//Nbt tag used in 1.17 versions that didn't use fabric transfer API.
	public static final String LEGACY_CONTENTS_KEY = "PackageContents";
	
	@SuppressWarnings("UnstableApiUsage")
	public class PackageStorage extends SnapshotParticipant<Overstack> implements SingleSlotStorage<ItemVariant> {
		private Overstack contents = Overstack.EMPTY;
		
		private CompoundTag save() {
			return contents.save();
		}
		
		private void load(CompoundTag tag) {
			contents = Overstack.load(tag);
		}
		
		private void loadLegacy(CompoundTag tag) {
			contents = Overstack.loadFromLegacyPackageData(tag);
		}
		
		private void clear() {
			contents = Overstack.EMPTY;
			PackageBlockEntity.this.setChanged();
		}
		
		public ItemStack icon() {
			return contents.variant().toStack();
		}
		
		public int count() {
			return (int) contents.count();
		}
		
		// Modeled after SingleStackStorage, a little. I don't know what I'm doing lol
		@Override
		public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);
			
			if(contents.isEmpty() || contents.matches(insertedVariant)) {
				long insertedAmount = Math.min(maxAmount, getCapacityFor(insertedVariant) - contents.count());
				if(insertedAmount > 0) {
					updateSnapshots(transaction);
					contents = contents.isEmpty() ? Overstack.of(insertedVariant, maxAmount) : contents.bumpedBy(insertedAmount);
				}
				return insertedAmount;
			}
			
			return 0;
		}
		
		@Override
		public long extract(ItemVariant extractedVariant, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(extractedVariant, maxAmount);
			
			if(contents.isEmpty()) return 0; //uhh
			
			if(contents.matches(extractedVariant)) {
				long extractedAmount = Math.min(contents.count(), maxAmount);
				if(extractedAmount > 0) {
					updateSnapshots(transaction);
					contents = contents.bumpedBy(-extractedAmount);
				}
				return extractedAmount;
			}
			
			return 0;
		}
		
		@Override
		public boolean isResourceBlank() {
			return contents.isEmpty();
		}
		
		@Override
		public ItemVariant getResource() {
			return contents.variant();
		}
		
		@Override
		public long getAmount() {
			return contents.count();
		}
		
		@Override
		public long getCapacity() {
			return getCapacityFor(contents.variant());
		}
		
		private long getCapacityFor(ItemVariant variant) {
			//Packages hold up to 8 stacks of an item.
			//TODO: Reimpl recursion restriction
			//	public static int maxStackAmountAllowed(ItemStack stack) {
//		if(stack.isEmpty()) return 64;
//		else if(stack.getItem() instanceof PackageItem) {
//			//TODO clean this up
//			if(!stack.hasTag()) return 64;
//			CompoundTag beTag = stack.getTagElement("BlockEntityTag");
//			if(beTag == null) return 64;
//			CompoundTag contentsTag = beTag.getCompound(LEGACY_CONTENTS_KEY);
//			if(contentsTag.getInt("realCount") > 0) return 1;
//			else return 64;
//		}
//		else return Math.min(stack.getMaxStackSize(), 64); //just in case
//	}
			return variant.getItem().getMaxStackSize() * 8L;
		}
		
		@Override
		protected Overstack createSnapshot() {
			return contents; //Immutable object
		}
		
		@Override
		protected void readSnapshot(Overstack snapshot) {
			this.contents = snapshot;
		}
		
		@Override
		protected void onFinalCommit() {
			PackageBlockEntity.this.setChanged();
		}
	}
	
	@SuppressWarnings("UnstableApiUsage")
	public @Nullable Storage<ItemVariant> getSidedItemStorage(Direction dir) {
		Direction packageFacing = getBlockState().getValue(PackageBlock.FACING).primaryDirection;
		if(dir == packageFacing) return null;
		else return storage;
	}
	
	public PackageStorage getItemStorage() {
		return storage;
	}
	
	@Override
	public Object getRenderAttachmentData() {
		return style;
	}
	
	public void setStyle(PackageStyle style) {
		this.style = style;
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
	
	//Serialization
	@Override
	public void saveAdditional(CompoundTag tag) {
		//Contents
		tag.put(STORAGE_KEY, storage.save());
		
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
		if(tag.contains(LEGACY_CONTENTS_KEY)) {
			storage.loadLegacy(tag.getCompound(LEGACY_CONTENTS_KEY));
		} else {
			storage.load(tag.getCompound(STORAGE_KEY));
		}
		
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
