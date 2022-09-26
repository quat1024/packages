package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.junk.PackageContainer;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

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
	
	@Override
	public boolean isBarVisible(ItemStack stack) {
		if(stack.getCount() != 1) return false; //Clips with the number and looks bad, and stacked packages aren't interactable anyway.
		
		PackageContainer container = PackageContainer.fromItemStack(stack);
		return container != null && !container.isEmpty();
	}
	
	@Override
	public int getBarWidth(ItemStack stack) {
		PackageContainer container = PackageContainer.fromItemStack(stack);
		if(container == null) return 0;
		return Math.min((int) (1 + 12 * container.fillPercentage()), 13);
	}
	
	@Override
	public int getBarColor(ItemStack stack) {
		PackageContainer container = PackageContainer.fromItemStack(stack);
		if(container == null) return 0xFF00FF; //Shouldn't be viewable lol
		else if(container.isFull()) return 0xD5636A; //Nice tomato-ey red color
		else return 0x6666FF; //Same color as the bundle's bar
	}
	
	@Override
	public boolean overrideStackedOnOther(ItemStack me, Slot slot, ClickAction clickAction, Player player) {
		if(clickAction != ClickAction.SECONDARY || !Packages.instance.config.inventoryInteractions) return super.overrideStackedOnOther(me, slot, clickAction, player);
		if(me.getCount() != 1) return false;
		ItemStack other = slot.getItem();
		
		//These are two stackable packages. Presumably, the player wants to stack them,
		//instead of putting one inside the other. They can always be nested in-world.
		if(ItemStack.isSameItemSameTags(me, other)) return false;
		
		return PackageContainer.mutateItemStack(me, container -> {
			if(other.isEmpty() && !container.isEmpty()) {
				//The package contains items, but the slot is empty. Take one stack of items from the package and deposit it into the slot.
				doDeposit(player, container, slot, container.getFilterStack().getMaxStackSize());
				return true;
			} else if(!other.isEmpty()) {
				//The slot is not empty. Try to sponge up items from the slot into the package.
				boolean absorbSuccess = doAbsorb(player, container, other);
				if(absorbSuccess) return true;
				else if(container.matches(other) && container.isFull()) {
					//If we're here, we're in a situation where the player clicked a slot that has an item matching the package's contents,
					//but we couldn't draw any of the items into the package because it was full.
					//In this case, we should replenish the slot's contents with more items from the package. Confirmed, better than bundles lolol gottem
					int remainingSpaceInSlot = Math.max(0, other.getMaxStackSize() - other.getCount());
					if(remainingSpaceInSlot != 0) {
						doDeposit(player, container, slot, remainingSpaceInSlot);
						return true;
					}
				}
			}
			return false;
		}, false);
	}
	
	@Override
	public boolean overrideOtherStackedOnMe(ItemStack me, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
		if(clickAction != ClickAction.SECONDARY || !Packages.instance.config.inventoryInteractions) return super.overrideOtherStackedOnMe(me, other, slot, clickAction, player, slotAccess);
		if(me.getCount() != 1) return false;
		
		//These are two stackable packages. Presumably, the player wants to stack them,
		//instead of putting one inside the other. They can always be nested in-world.
		if(ItemStack.isSameItemSameTags(me, other)) return false;
		
		return PackageContainer.mutateItemStack(me, container -> {
			if(container.matches(other)) {
				doAbsorb(player, container, other);
				return true;
			}
			return false;
		}, false);
	}
	
	private boolean doAbsorb(Player player, PackageContainer container, ItemStack other) {
		ItemStack insertionLeftover = container.insert(other, Integer.MAX_VALUE, false);
		if(insertionLeftover.getCount() == other.getCount()) return false;
		
		other.setCount(insertionLeftover.getCount());
		player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.getLevel().getRandom().nextFloat() * 0.4f);
		return true;
	}
	
	private void doDeposit(Player player, PackageContainer container, Slot slot, int amountToTake) {
		slot.safeInsert(container.take(amountToTake, false));
		player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 0.8f + player.getLevel().getRandom().nextFloat() * 0.4f);
	}
}
