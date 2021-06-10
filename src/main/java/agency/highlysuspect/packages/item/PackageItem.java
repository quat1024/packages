package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.junk.PackageStyle;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;

import java.util.Optional;

public class PackageItem extends BlockItem {
	public PackageItem(Block block, Settings settings) {
		super(block, settings);
	}
	
	public ItemStack createCustomizedStack(Block frame, Block inner, DyeColor color) {
		ItemStack blah = new PackageStyle(frame, inner, color).writeToStackTag(new ItemStack(this));
		addFakeContentsTagThisSucks(blah);
		return blah;
	}
	
	public static void addFakeContentsTagThisSucks(ItemStack stack) {
		//Add a blank contents tag TODO this hack sucks ass, find a better way to make crafted pkgs and dropped empty pkgs stack
		NbtCompound bad = new NbtCompound();
		bad.putInt("realCount", 0);
		stack.getSubTag("BlockEntityTag").put("PackageContents", bad);
	}
	
	@Override
	public Text getName(ItemStack stack) {
		//TODO FIX THIS LMAO THIS SUCKS
		NbtCompound beTag = stack.getSubTag("BlockEntityTag");
		if(beTag != null) {
			NbtCompound contentsTag = beTag.getCompound("PackageContents");
			if(!contentsTag.isEmpty()) {
				int count = contentsTag.getInt("realCount");
				ItemStack containedStack = ItemStack.fromNbt(contentsTag.getCompound("stack"));
				if(count != 0 && !containedStack.isEmpty()) {
					return new TranslatableText("block.packages.package.nonempty",
						super.getName(stack),
						count,
						containedStack.getName()
					);
				}
			}
		}
		
		return super.getName(stack);
	}
	
	public Optional<ItemStack> getContainedStack(ItemStack stack) {
		NbtCompound beTag = stack.getSubTag("BlockEntityTag");
		if(beTag != null) {
			NbtCompound contentsTag = beTag.getCompound("PackageContents");
			if (!contentsTag.isEmpty()) {
				return Optional.of(ItemStack.fromNbt(contentsTag.getCompound("stack"))).filter(s -> !s.isEmpty());
			}
		}
		return Optional.empty();
	}
}
