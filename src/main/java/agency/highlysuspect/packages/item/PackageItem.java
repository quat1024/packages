package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class PackageItem extends BlockItem {
	public PackageItem(Block block, Settings settings) {
		super(block, settings);
	}
	
	public ItemStack createCustomizedStack(Block frame, Block inner, DyeColor color) {
		ItemStack blah = new PackageStyle(frame, inner, color).writeToStackTag(new ItemStack(this));
		
		//Add a blank contents tag TODO this hack sucks ass, find a better way to make crafted pkgs and dropped empty pkgs stack
		CompoundTag bad = new CompoundTag();
		bad.putInt("realCount", 0);
		blah.getSubTag("BlockEntityTag").put("PackageContents", bad);
		
		return blah;
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		//TODO Show contents
		
		//Show frame
		Style outer = new Style().setColor(Formatting.DARK_PURPLE).setItalic(true);
		Style inner = new Style().setColor(Formatting.LIGHT_PURPLE).setItalic(false);
		PackageStyle packageStyle = PackageStyle.fromItemStack(stack);
		
		Block frameBlock = packageStyle.frameBlock;
		Block innerBlock = packageStyle.innerBlock;
		
		if(frameBlock.equals(innerBlock)) {
			tooltip.add(new TranslatableText("packages.style_tooltip.both", frameBlock.getName().setStyle(inner)).setStyle(outer));
		} else {
			tooltip.add(new TranslatableText("packages.style_tooltip.frame", frameBlock.getName().setStyle(inner)).setStyle(outer));
			tooltip.add(new TranslatableText("packages.style_tooltip.inner", innerBlock.getName().setStyle(inner)).setStyle(outer));
		}
	}
}
