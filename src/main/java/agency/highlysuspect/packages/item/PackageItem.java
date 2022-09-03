package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.junk.PackageContainer;
import agency.highlysuspect.packages.junk.PackageStyle;
import java.util.Optional;
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
		return new PackageContainer().writeToStackTag(
			new PackageStyle(frame, inner, color).writeToStackTag(
				new ItemStack(this)));
	}
	
	@Override
	public Component getName(ItemStack stack) {
		PackageContainer container = PackageContainer.fromItemStack(stack);
		if(container != null) {
			ItemStack contents = container.getFilterStack();
			int count = container.getCount();
			if(!contents.isEmpty() && count != 0) {
				return new TranslatableComponent("block.packages.package.nonempty",
					super.getName(stack),
					count,
					contents.getHoverName()
				);
			}
		}
		
		return super.getName(stack);
	}
	
	public Optional<ItemStack> getContainedStack(ItemStack stack) {
		return Optional.ofNullable(PackageContainer.fromItemStack(stack))
			.map(PackageContainer::getFilterStack)
			.filter(s -> !s.isEmpty());
	}
}
