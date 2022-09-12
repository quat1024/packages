package agency.highlysuspect.packages.junk;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

//aka "nullable package style"
public record PackageMakerRenderAttachment(@Nullable Block frameBlock, @Nullable Block innerBlock, @Nullable DyeColor color) {}
