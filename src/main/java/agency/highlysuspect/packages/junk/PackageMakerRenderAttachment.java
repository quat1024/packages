package agency.highlysuspect.packages.junk;

import net.minecraft.block.Block;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

//aka "nullable package style"
public record PackageMakerRenderAttachment(@Nullable Block frameBlock, @Nullable Block innerBlock, @Nullable DyeColor color) {}
