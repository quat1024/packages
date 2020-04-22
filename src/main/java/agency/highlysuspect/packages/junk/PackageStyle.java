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
		else return fromTag(tag.getCompound(KEY));
	}
	
	public final Block frameBlock;
	public final Block innerBlock;
	public final DyeColor color;
	
	private static final Block fallbackFrame = Blocks.STONE;
	private static final Block fallbackInner = Blocks.RED_CONCRETE;
	private static final DyeColor fallbackColor = DyeColor.BLUE;
	
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
		toTag(stack.getOrCreateSubTag(KEY));
		return stack;
	}
}
