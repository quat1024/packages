package agency.highlysuspect.packages.mixin.api;

import agency.highlysuspect.packages.api.StackSensitiveContainerItemRules;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BundleItem.class)
public abstract class MixinBundleItem implements StackSensitiveContainerItemRules {
	@Override
	public boolean canFitInsideContainerItems(ItemStack stack) {
		return !stack.isBarVisible();
	}
}
