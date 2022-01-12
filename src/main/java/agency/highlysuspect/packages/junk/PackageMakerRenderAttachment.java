package agency.highlysuspect.packages.junk;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

//aka "nullable package style"
public record PackageMakerRenderAttachment(@Nullable Block frameBlock, @Nullable Block innerBlock, @Nullable DyeColor color) {}
