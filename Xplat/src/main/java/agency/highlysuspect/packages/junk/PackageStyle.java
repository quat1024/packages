package agency.highlysuspect.packages.junk;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public record PackageStyle(@NotNull Block frameBlock, @NotNull Block innerBlock, @NotNull DyeColor color) {
	public static final PackageStyle ERROR_LOL = new PackageStyle(Blocks.PINK_CONCRETE, Blocks.BLACK_CONCRETE, DyeColor.RED);
	public static final String KEY = "PackageStyle";
	
	public static PackageStyle fromTag(CompoundTag tag) {
		return new PackageStyle(
			Registry.BLOCK.getOptional(ResourceLocation.tryParse(tag.getString("frame"))).orElse(Blocks.AIR),
			Registry.BLOCK.getOptional(ResourceLocation.tryParse(tag.getString("inner"))).orElse(Blocks.AIR),
			tag.contains("color") ? DyeColor.byId(tag.getInt("color")) : DyeColor.WHITE
		);
	}
	
	public static PackageStyle fromItemStack(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if(tag == null) return ERROR_LOL;
		else return fromTag(tag.getCompound("BlockEntityTag").getCompound(KEY));
	}
	
	public PackageStyle(@NotNull Block frameBlock, @NotNull Block innerBlock, @NotNull DyeColor color) {
		this.frameBlock = frameBlock;
		this.innerBlock = innerBlock;
		this.color = color;
	}
	
	public CompoundTag toTag() {
		return toTag(new CompoundTag());
	}
	
	public CompoundTag toTag(CompoundTag writeTo) {
		writeTo.putString("frame", Registry.BLOCK.getKey(frameBlock).toString());
		writeTo.putString("inner", Registry.BLOCK.getKey(innerBlock).toString());
		writeTo.putInt("color", color.getId());
		return writeTo;
	}
	
	public ItemStack writeToStackTag(ItemStack stack) {
		stack.getOrCreateTagElement("BlockEntityTag").put(KEY, toTag());
		return stack;
	}
}
