package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.junk.PackageStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

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
		CompoundTag bad = new CompoundTag();
		bad.putInt("realCount", 0);
		stack.getSubTag("BlockEntityTag").put("PackageContents", bad);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		//TODO This makes me sad but im not sure what a better way would be...
		CompoundTag beTag = stack.getSubTag("BlockEntityTag");
		if(beTag != null) {
			CompoundTag contentsTag = beTag.getCompound("PackageContents");
			if(!contentsTag.isEmpty()) {
				int count = contentsTag.getInt("realCount");
				ItemStack containedStack = ItemStack.fromTag(contentsTag.getCompound("stack"));
				
				if(!containedStack.isEmpty()) {
					Text uwu = new TranslatableText("packages.contents_tooltip", count, containedStack.getName());
					if(context.isAdvanced()) {
						uwu.append(" ").append(new LiteralText(Registry.ITEM.getId(containedStack.getItem()).toString()).styled(s -> s.setColor(Formatting.DARK_GRAY)));
					}
					tooltip.add(uwu);
					
					List<Text> containedTooltip = new ArrayList<>();
					containedStack.getItem().appendTooltip(containedStack, world, containedTooltip, context);
					for (Text containedLine : containedTooltip) {
						tooltip.add(new LiteralText("   ").append(containedLine));
					}
					
					return;
				}
			}
			
			tooltip.add(new TranslatableText("packages.contents_tooltip.empty"));
		}
	}
}
