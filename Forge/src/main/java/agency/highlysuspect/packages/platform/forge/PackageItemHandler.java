package agency.highlysuspect.packages.platform.forge;

import agency.highlysuspect.packages.junk.PackageContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

public class PackageItemHandler extends InvWrapper {
	public PackageItemHandler(PackageContainer container) {
		super(container);
		this.container = container;
	}
	
	private final PackageContainer container;
	
	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		//using the custom package insertion method, instead of going slot-by-slot
		return container.insert(stack, stack.getCount(), simulate);
	}
	
	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		//using the custom package removal method, instead of going slot-by-slot
		return container.take(amount, simulate);
	}
}
