package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.junk.PackageContainer;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PackageItem extends BlockItem {
	public PackageItem(Block block, Properties settings) {
		super(block, settings);
	}
	
	public ItemStack createCustomizedStack(Block frame, Block inner, DyeColor color) {
		return new PackageContainer().writeToStackTag(
			new PackageStyle(frame, inner, color).writeToStackTag(
				new ItemStack(this)));
	}
	
	private int nameReentrancy = 0;
	
	@Override
	public Component getName(ItemStack stack) {
		PackageContainer contents = PackageContainer.fromItemStack(stack);
		if(contents != null) {
			ItemStack contained = contents.getFilterStack();
			if(!contained.isEmpty()) {
				MutableComponent contentsComponent;
				try {
					nameReentrancy++;
					contentsComponent = new TranslatableComponent("block.packages.package.nonempty.contents", contents.getCount(), contained.getHoverName());
					contentsComponent = switch(nameReentrancy) {
						case 1  -> contentsComponent.withStyle(s -> s.withColor(0xD0D0D0));
						case 2  -> contentsComponent.withStyle(s -> s.withColor(0xA0A0A0));
						case 3  -> contentsComponent.withStyle(s -> s.withColor(0x858585));
						default -> contentsComponent.withStyle(s -> s.withColor(0x666666));
					};
					
					if(nameReentrancy == 1) {
						return new TranslatableComponent("block.packages.package.nonempty", super.getName(stack), contentsComponent);
					} else {
						return new TranslatableComponent("block.packages.package.nonempty.reentrant", contentsComponent);
					}
				} finally {
					nameReentrancy--;
				}
			}
		}
		
		return super.getName(stack);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag mistake) {
		PackageContainer contents = PackageContainer.fromItemStack(stack);
		if(contents != null) {
			//Find the item all the way at the bottom of the chain
			ItemStack root = contents.computeRootContents();
			if(!root.isEmpty()) {
				//Find how many of that item you would get if you unrolled all layers of package
				int fullyMultipliedCount = contents.computeFullyMultipliedCount();
				//If there's at least one layer of nesting going on: advertise how many items there ultimately are in the package
				if(contents.computeAmplificationStatus()) {
					tooltip.add(
						new TranslatableComponent("packages.contents_tooltip.utimately",
							new TranslatableComponent("block.packages.package.nonempty.contents", fullyMultipliedCount, root.getHoverName()).withStyle(ChatFormatting.DARK_RED)
						).withStyle(ChatFormatting.DARK_GRAY)
					);
				}
			}
		}
		
		PackageStyle style = PackageStyle.fromItemStack(stack);
		Block frameBlock = style.frameBlock();
		Block innerBlock = style.innerBlock();
		if(frameBlock == innerBlock) {
			tooltip.add(new TranslatableComponent("packages.style_tooltip.both", frameBlock.getName().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
		} else {
			tooltip.add(new TranslatableComponent("packages.style_tooltip.frame", frameBlock.getName().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
			tooltip.add(new TranslatableComponent("packages.style_tooltip.inner", innerBlock.getName().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
		}
		
		DyeColor color = style.color();
		tooltip.add(new TranslatableComponent("packages.style_tooltip.color",
			new TranslatableComponent("packages.style_tooltip.color." + color.getSerializedName()).withStyle(s -> s.withColor((color == DyeColor.BLACK ? DyeColor.GRAY : color).getTextColor()).withItalic(true))));
		
		super.appendHoverText(stack, level, tooltip, mistake);
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
		//Not toggleable by Packages.instance.config.interactionSounds because bundles aren't toggleable either;)
		player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.getLevel().getRandom().nextFloat() * 0.4f);
		return true;
	}
	
	private void doDeposit(Player player, PackageContainer container, Slot slot, int amountToTake) {
		slot.safeInsert(container.take(amountToTake, false));
		player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 0.8f + player.getLevel().getRandom().nextFloat() * 0.4f);
	}
}
