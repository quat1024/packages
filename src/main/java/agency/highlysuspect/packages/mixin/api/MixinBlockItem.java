package agency.highlysuspect.packages.mixin.api;

import agency.highlysuspect.packages.api.StackSensitiveContainerItemRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockItem.class)
public class MixinBlockItem implements StackSensitiveContainerItemRules {
	@Override
	public boolean canFitInsideContainerItems(ItemStack stack) {
		//Annoyingly, shulker boxes don't have their own item
		if(((BlockItem) (Object) this).getBlock() instanceof ShulkerBoxBlock) {
			CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
			if(blockEntityTag == null) return true;
			else return blockEntityTag.getList("Items", 10).isEmpty();
		}
		
		return ((BlockItem) (Object) this).canFitInsideContainerItems(); //delegate to super basically 
	}
}
