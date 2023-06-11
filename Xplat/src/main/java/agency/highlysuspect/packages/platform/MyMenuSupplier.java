package agency.highlysuspect.packages.platform;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface MyMenuSupplier<T extends AbstractContainerMenu> {
	T create(int var1, Inventory var2);
}
