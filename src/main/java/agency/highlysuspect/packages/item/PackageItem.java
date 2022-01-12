package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.junk.PackageStyle;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class PackageItem extends BlockItem {
	public PackageItem(Block block, Properties settings) {
		super(block, settings);
	}
	
	public ItemStack createCustomizedStack(Block frame, Block inner, DyeColor color) {
		ItemStack blah = new PackageStyle(frame, inner, color).writeToStackTag(new ItemStack(this));
		addFakeContentsTagThisSucks(blah);
		return blah;
	}
	
	public static void addFakeContentsTagThisSucks(ItemStack stack) {
		//Add a blank contents tag TODO this hack sucks ass, find a better way to make crafted pkgs and dropped empty pkgs stack
		CompoundTag bad = new CompoundTag();
		bad.putInt("realCount", 0);
		stack.getOrCreateTagElement("BlockEntityTag").put("PackageContents", bad);
	}
	
	@Override
	public Component getName(ItemStack stack) {
		//TODO FIX THIS LMAO THIS SUCKS
		CompoundTag beTag = stack.getTagElement("BlockEntityTag");
		if(beTag != null) {
			CompoundTag contentsTag = beTag.getCompound("PackageContents");
			if(!contentsTag.isEmpty()) {
				int count = contentsTag.getInt("realCount");
				ItemStack containedStack = ItemStack.of(contentsTag.getCompound("stack"));
				if(count != 0 && !containedStack.isEmpty()) {
					return new TranslatableComponent("block.packages.package.nonempty",
						super.getName(stack),
						count,
						containedStack.getHoverName()
					);
				}
			}
		}
		
		return super.getName(stack);
	}
	
	public Optional<ItemStack> getContainedStack(ItemStack stack) {
		CompoundTag beTag = stack.getTagElement("BlockEntityTag");
		if(beTag != null) {
			CompoundTag contentsTag = beTag.getCompound("PackageContents");
			if (!contentsTag.isEmpty()) {
				return Optional.of(ItemStack.of(contentsTag.getCompound("stack"))).filter(s -> !s.isEmpty());
			}
		}
		return Optional.empty();
	}
}
