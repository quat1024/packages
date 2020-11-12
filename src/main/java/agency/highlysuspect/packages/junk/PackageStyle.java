package agency.highlysuspect.packages.junk;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PackageStyle {
	public PackageStyle(Block frameBlock, Block innerBlock, DyeColor color) {
		this.frameBlock = frameBlock;
		this.innerBlock = innerBlock;
		this.color = color;
	}
	
	public static PackageStyle fromTag(CompoundTag tag) {
		return new PackageStyle(
			Registry.BLOCK.getOrEmpty(Identifier.tryParse(tag.getString("frame"))).orElse(fallbackFrame),
			Registry.BLOCK.getOrEmpty(Identifier.tryParse(tag.getString("inner"))).orElse(fallbackInner),
			DyeColor.byId(tag.getInt("color"))
		);
	}
	
	public static PackageStyle fromItemStack(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if(tag == null) return FALLBACK;
		else return fromTag(tag.getCompound("BlockEntityTag").getCompound(KEY));
	}
	
	public final Block frameBlock;
	public final Block innerBlock;
	public final DyeColor color;
	
	private static final Block fallbackFrame = Blocks.PINK_CONCRETE;
	private static final Block fallbackInner = Blocks.BLACK_CONCRETE;
	private static final DyeColor fallbackColor = DyeColor.PINK;
	
	public static final String KEY = "PackageStyle";
	public static final PackageStyle FALLBACK = new PackageStyle(fallbackFrame, fallbackInner, fallbackColor);
	
	public CompoundTag toTag() {
		return toTag(new CompoundTag());
	}
	
	public CompoundTag toTag(CompoundTag writeTo) {
		writeTo.putString("frame", Registry.BLOCK.getId(frameBlock).toString());
		writeTo.putString("inner", Registry.BLOCK.getId(innerBlock).toString());
		writeTo.putInt("color", color.getId());
		return writeTo;
	}
	
	public ItemStack writeToStackTag(ItemStack stack) {
		stack.getOrCreateSubTag("BlockEntityTag").put(KEY, toTag());
		return stack;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		PackageStyle that = (PackageStyle) o;
		
		if(!frameBlock.equals(that.frameBlock)) return false;
		if(!innerBlock.equals(that.innerBlock)) return false;
		return color == that.color;
	}
	
	@Override
	public int hashCode() {
		int result = frameBlock.hashCode();
		result = 31 * result + innerBlock.hashCode();
		result = 31 * result + color.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		return "PackageStyle{frameBlock=" + frameBlock + ", innerBlock=" + innerBlock + ", color=" + color + '}';
	}
}
