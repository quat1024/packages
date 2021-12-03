package agency.highlysuspect.packages.junk;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class PackageStyle {
	public static PackageStyle fromTag(NbtCompound tag) {
		return new PackageStyle(
			Registry.BLOCK.getOrEmpty(Identifier.tryParse(tag.getString("frame"))).orElse(Blocks.AIR),
			Registry.BLOCK.getOrEmpty(Identifier.tryParse(tag.getString("inner"))).orElse(Blocks.AIR),
			tag.contains("color") ? DyeColor.byId(tag.getInt("color")) : DyeColor.WHITE
		);
	}
	
	public static final PackageStyle ERROR_LOL = new PackageStyle(Blocks.PINK_CONCRETE, Blocks.BLACK_CONCRETE, DyeColor.RED);
	
	public static PackageStyle fromItemStack(ItemStack stack) {
		NbtCompound tag = stack.getTag();
		if(tag == null) return ERROR_LOL;
		else return fromTag(tag.getCompound("BlockEntityTag").getCompound(KEY));
	}
	
	public static final String KEY = "PackageStyle";
	private final @NotNull Block frameBlock;
	private final @NotNull Block innerBlock;
	private final @NotNull DyeColor color;
	
	public PackageStyle(@NotNull Block frameBlock, @NotNull Block innerBlock, @NotNull DyeColor color) {
		this.frameBlock = frameBlock;
		this.innerBlock = innerBlock;
		this.color = color;
	}
	
	public NbtCompound toTag() {
		return toTag(new NbtCompound());
	}
	
	public NbtCompound toTag(NbtCompound writeTo) {
		writeTo.putString("frame", Registry.BLOCK.getId(frameBlock).toString());
		writeTo.putString("inner", Registry.BLOCK.getId(innerBlock).toString());
		writeTo.putInt("color", color.getId());
		return writeTo;
	}
	
	public ItemStack writeToStackTag(ItemStack stack) {
		stack.getOrCreateSubTag("BlockEntityTag").put(KEY, toTag());
		return stack;
	}
	
	public @NotNull Block frameBlock() {return frameBlock;}
	
	public @NotNull Block innerBlock() {return innerBlock;}
	
	public @NotNull DyeColor color() {return color;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (PackageStyle) obj;
		return Objects.equals(this.frameBlock, that.frameBlock) &&
			Objects.equals(this.innerBlock, that.innerBlock) &&
			Objects.equals(this.color, that.color);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(frameBlock, innerBlock, color);
	}
	
	@Override
	public String toString() {
		return "PackageStyle[" +
			"frameBlock=" + frameBlock + ", " +
			"innerBlock=" + innerBlock + ", " +
			"color=" + color + ']';
	}
	
}
