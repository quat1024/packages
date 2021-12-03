package agency.highlysuspect.packages.container;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

//ItemVariant, in transfer-api parlance, is "an immutable itemstack without count".
//This record pairs it with a count again, to make a complete immutable itemstack.
//The catch: the count is not guaranteed to fit within the safe bounds of one ItemStack's count.
//It may be much higher than the max stack size of the item.
@SuppressWarnings("UnstableApiUsage")
public record Overstack(ItemVariant variant, long count) {
	public static final Overstack EMPTY = new Overstack(ItemVariant.blank(), 0);
	
	//Better to use this than the record constructor.
	public static Overstack of(ItemVariant variant, long count) {
		if(variant.isBlank() || count == 0) return EMPTY;
		else return new Overstack(variant, count);
	}
	
	public static Overstack load(CompoundTag tag) {
		ItemVariant variant = ItemVariant.fromNbt(tag.getCompound("Variant"));
		long count = tag.getLong("Count");
		return new Overstack(variant, count);
	}
	
	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.put("Variant", variant.toNbt());
		tag.putLong("Count", count);
		return tag;
	}
	
	public boolean isEmpty() {
		return variant.isBlank() || count == 0;
	}
	
	public boolean matches(ItemVariant other) {
		return other.matches(variant.toStack());
	}
	
	public Overstack bumpedBy(long howMuch) {
		return Overstack.of(variant, count + howMuch);
	}
	
	public static Overstack loadFromLegacyPackageData(CompoundTag tag) {
		int count = tag.getInt("realCount");
		if(count == 0) return EMPTY;
		
		ItemStack stack = ItemStack.of(tag.getCompound("stack"));
		return new Overstack(ItemVariant.of(stack), count);
	}
}
