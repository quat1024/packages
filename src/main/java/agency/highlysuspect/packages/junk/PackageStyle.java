package agency.highlysuspect.packages.junk;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

public record PackageStyle(@NotNull Block frameBlock, @NotNull Block innerBlock, @NotNull DyeColor color) {
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
}
