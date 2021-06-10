package agency.highlysuspect.packages.junk;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record PackageStyle(@Nullable Block frameBlock,
													 @Nullable Block innerBlock,
													 @Nullable DyeColor color) {
	public PackageStyle(@Nullable Block frameBlock, @Nullable Block innerBlock, @Nullable DyeColor color) {
		this.frameBlock = frameBlock;
		this.innerBlock = innerBlock;
		this.color = color;
	}
	
	public static PackageStyle fromTag(NbtCompound tag) {
		return new PackageStyle(
			Registry.BLOCK.get(Identifier.tryParse(tag.getString("frame"))),
			Registry.BLOCK.get(Identifier.tryParse(tag.getString("inner"))),
			tag.contains("color") ? DyeColor.byId(tag.getInt("color")) : null
		);
	}
	
	public static final PackageStyle EMPTY = new PackageStyle(null, null, null);
	public static final PackageStyle ERROR_LOL = new PackageStyle(Blocks.PINK_CONCRETE, Blocks.BLACK_CONCRETE, DyeColor.RED);
	
	public static PackageStyle fromItemStack(ItemStack stack) {
		NbtCompound tag = stack.getTag();
		if(tag == null) return ERROR_LOL;
		else return fromTag(tag.getCompound("BlockEntityTag").getCompound(KEY));
	}
	
	public static final String KEY = "PackageStyle";
	
	public NbtCompound toTag() {
		return toTag(new NbtCompound());
	}
	
	public NbtCompound toTag(NbtCompound writeTo) {
		if(frameBlock != null) writeTo.putString("frame", Registry.BLOCK.getId(frameBlock).toString());
		if(innerBlock != null) writeTo.putString("inner", Registry.BLOCK.getId(innerBlock).toString());
		if(color != null) writeTo.putInt("color", color.getId());
		return writeTo;
	}
	
	public ItemStack writeToStackTag(ItemStack stack) {
		stack.getOrCreateSubTag("BlockEntityTag").put(KEY, toTag());
		return stack;
	}
	
	public boolean hasFrame() {
		return frameBlock != null;
	}
	
	public boolean hasInner() {
		return innerBlock != null;
	}
	
	public boolean hasColor() {
		return color != null;
	}
	
	public Block getFrame() {
		return frameBlock;
	}
	
	public Block getInner() {
		return innerBlock;
	}
	
	public @Nullable
	DyeColor getColor() {
		return color;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		PackageStyle that = (PackageStyle) o;
		
		if(!Objects.equals(frameBlock, that.frameBlock)) return false;
		if(!Objects.equals(innerBlock, that.innerBlock)) return false;
		return color == that.color;
	}
	
	@Override
	public int hashCode() {
		int result = frameBlock != null ? frameBlock.hashCode() : 0;
		result = 31 * result + (innerBlock != null ? innerBlock.hashCode() : 0);
		result = 31 * result + (color != null ? color.hashCode() : 0);
		return result;
	}
	
	@Override
	public String toString() {
		return "PackageStyle{" +
			"frameBlock=" + frameBlock +
			", innerBlock=" + innerBlock +
			", color=" + color +
			'}';
	}
}
