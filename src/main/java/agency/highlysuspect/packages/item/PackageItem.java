package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.Packages;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.List;

public class PackageItem extends BlockItem {
	public PackageItem(Block block, Settings settings) {
		super(block, settings);
	}
	
	private static final String FRAME_BLOCK_KEY = "frame_block";
	private static final String INNER_BLOCK_KEY = "inner_block";
	
	private static final String CONTENTS_KEY = "contents"; //TODO
	
	private Block getBlock(ItemStack stack, String key) {
		if(stack.getTag() == null) return Blocks.STONE;
		else return Registry.BLOCK.getOrEmpty(Identifier.tryParse(stack.getTag().getCompound(Packages.MODID).getString(key))).orElse(Blocks.STONE);
	}
	
	public void setBlock(ItemStack stack, Block block, String key) {
		stack.getOrCreateSubTag(Packages.MODID).putString(key, Registry.BLOCK.getId(block).toString());
	}
	
	public Block getFrameBlock(ItemStack stack) {
		return getBlock(stack, FRAME_BLOCK_KEY);
	}
	
	public void setFrameBlock(ItemStack stack, Block block) {
		setBlock(stack, block, FRAME_BLOCK_KEY);
	}
	
	public Block getInnerBlock(ItemStack stack) {
		return getBlock(stack, INNER_BLOCK_KEY);
	}
	
	public void setInnerBlock(ItemStack stack, Block block) {
		setBlock(stack, block, INNER_BLOCK_KEY);
	}
	
	public ItemStack createCustomizedStack(Block frame, Block inner) {
		ItemStack poot = new ItemStack(this);
		
		setFrameBlock(poot, frame);
		setInnerBlock(poot, inner);
		
		return poot;
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		//TODO Show contents
		
		//Show frame
		Style outer = new Style().setColor(Formatting.DARK_PURPLE).setItalic(true);
		Style inner = new Style().setColor(Formatting.LIGHT_PURPLE).setItalic(false);
		
		tooltip.add(new TranslatableText("packages.frame", getFrameBlock(stack).getName().setStyle(inner)).setStyle(outer));
		tooltip.add(new TranslatableText("packages.inner", getInnerBlock(stack).getName().setStyle(inner)).setStyle(outer));
	}
}
