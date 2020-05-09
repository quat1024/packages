package agency.highlysuspect.packages.junk;

import agency.highlysuspect.packages.item.PItems;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.BlockPlacementDispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PDispenserBehaviors {
	public static void onInitialize() {
		DispenserBlock.registerBehavior(PItems.PACKAGE, new SimpleBlockPlacementDispenserBehavior());
	}
	
	//Copy-paste of blockplacementdispenserbehavior, but it doesn't try to be smart about placing "on the ground"
	public static class SimpleBlockPlacementDispenserBehavior extends FallibleItemDispenserBehavior {
		@Override
		protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			this.success = false;
			Item item = stack.getItem();
			if (item instanceof BlockItem) {
				Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
				BlockPos blockPos = pointer.getBlockPos().offset(direction);
				//Direction direction2 = pointer.getWorld().isAir(blockPos.down()) ? direction : Direction.UP;
				this.success = ((BlockItem)item).place(new AutomaticItemPlacementContext(pointer.getWorld(), blockPos, direction, stack, direction/*2*/)) == ActionResult.SUCCESS;
			}
			
			return stack;
		}
	}
}
