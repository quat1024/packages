package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.item.PItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.DispenserBlock;

public class PDispenserBehaviors {
	public static void onInitialize() {
		Packages.instance.registerDispenserBehavior(PItems.PACKAGE, new SimpleBlockPlacementDispenserBehavior());
	}
	
	//Copy-paste of blockplacementdispenserbehavior, but it doesn't try to be smart about placing "on the ground"
	public static class SimpleBlockPlacementDispenserBehavior extends OptionalDispenseItemBehavior {
		@Override
		protected ItemStack execute(BlockSource pointer, ItemStack stack) {
			this.setSuccess(false);
			Item item = stack.getItem();
			if (item instanceof BlockItem) {
				Direction direction = pointer.getBlockState().getValue(DispenserBlock.FACING);
				BlockPos blockPos = pointer.getPos().relative(direction);
				//Direction direction2 = pointer.getWorld().isAir(blockPos.down()) ? direction : Direction.UP;
				this.setSuccess(((BlockItem)item).place(new DirectionalPlaceContext(pointer.getLevel(), blockPos, direction, stack, direction/*2*/)) == InteractionResult.SUCCESS);
			}
			
			return stack;
		}
	}
}
