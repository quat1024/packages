package agency.highlysuspect.packages.junk;

import net.minecraft.block.Block;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

//aka "nullable package style"
public final class PackageMakerRenderAttachment {
	private final @Nullable Block frameBlock;
	private final @Nullable Block innerBlock;
	private final @Nullable DyeColor color;
	
	public PackageMakerRenderAttachment(@Nullable Block frameBlock, @Nullable Block innerBlock, @Nullable DyeColor color) {
		this.frameBlock = frameBlock;
		this.innerBlock = innerBlock;
		this.color = color;
	}
	
	public @Nullable Block frameBlock() {return frameBlock;}
	
	public @Nullable Block innerBlock() {return innerBlock;}
	
	public @Nullable DyeColor color() {return color;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (PackageMakerRenderAttachment) obj;
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
		return "PackageMakerRenderAttachment[" +
			"frameBlock=" + frameBlock + ", " +
			"innerBlock=" + innerBlock + ", " +
			"color=" + color + ']';
	}
}
