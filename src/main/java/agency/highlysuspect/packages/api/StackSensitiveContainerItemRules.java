package agency.highlysuspect.packages.api;

import net.minecraft.world.item.ItemStack;

/**
 * Stack-sensitive version of Item#canFitInsideContainerItems.
 *
 * If you want to use this api, uh, lmk so i can implement making the vanilla shulkerbox and bundle respect it too.
 * Currently it's just used for Packages stuff.
 */
public interface StackSensitiveContainerItemRules {
	boolean canFitInsideContainerItems(ItemStack stack);
}
