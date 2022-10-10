package agency.highlysuspect.packages.junk;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public record PackageMakerStyle(@Nullable Block frameBlock, @Nullable Block innerBlock, @Nullable DyeColor color) {
	public static PackageMakerStyle NIL = new PackageMakerStyle(null, null, null);
}
