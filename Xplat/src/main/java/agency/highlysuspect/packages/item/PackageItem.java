package agency.highlysuspect.packages.item;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.PropsCommon;
import agency.highlysuspect.packages.junk.PackageContainer;
import agency.highlysuspect.packages.junk.PackageStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
		return new PackageContainer().writeToStackTag(new PackageStyle(frame, inner, color).writeToStackTag(new ItemStack(this)));
	}
	
	private int nameReentrancy = 0;
	
	@Override
	public Component getName(ItemStack stack) {
		PackageContainer contents = PackageContainer.fromItemStack(stack);
		if(contents == null) return super.getName(stack);
		
		ItemStack contained = contents.getFilterStack();
		if(contained.isEmpty()) return super.getName(stack);
		
		try {
			nameReentrancy++;
			MutableComponent contentsComponent = Component.translatable("block.packages.package.nonempty.contents", contents.getCount(), contained.getHoverName())
				.withStyle(s -> s.withColor(switch(nameReentrancy) {
					case 1 -> 0xD0D0D0;
					case 2 -> 0xA0A0A0;
					case 3 -> 0x858585;
					default -> 0x666666;
				}));
			
			if(nameReentrancy == 1) {
				return Component.translatable("block.packages.package.nonempty", super.getName(stack), contentsComponent);
			} else {
				return Component.translatable("block.packages.package.nonempty.reentrant", contentsComponent);
			}
		} finally {
			nameReentrancy--;
		}
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag mistake) {
		PackageContainer contents = PackageContainer.fromItemStack(stack);
		if(contents != null) {
			PackageContainer.TooltipStats stats = contents.computeTooltipStats();
			//If there's at least one layer of nontrivial nesting going on, advertise how many items there are if the entire package was unrolled.
			if(stats.amplified() && !stats.rootContents().isEmpty()) {
				tooltip.add(
					Component.translatable("packages.contents_tooltip.utimately",
						Component.translatable("block.packages.package.nonempty.contents",
							stats.fullyMultipliedCount(),
							stats.rootContents().getHoverName()
						).withStyle(ChatFormatting.DARK_RED)
					).withStyle(ChatFormatting.DARK_GRAY)
				);
			}
		}
		
		if(Packages.instance.proxy.hasShiftDownForTooltip()) {
			PackageStyle style = PackageStyle.fromItemStack(stack);
			Block frameBlock = style.frameBlock();
			Block innerBlock = style.innerBlock();
			if(frameBlock == innerBlock) {
				tooltip.add(Component.translatable("packages.style_tooltip.both", frameBlock.getName().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
			} else {
				tooltip.add(Component.translatable("packages.style_tooltip.frame", frameBlock.getName().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
				tooltip.add(Component.translatable("packages.style_tooltip.inner", innerBlock.getName().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
			}
			
			DyeColor color = style.color();
			tooltip.add(Component.translatable("packages.style_tooltip.color",
				Component.translatable("packages.style_tooltip.color." + color.getSerializedName())
					.withStyle(s -> s.withColor((color == DyeColor.BLACK ? DyeColor.GRAY : color).getTextColor()).withItalic(true))
			));
		} else {
			//Lifting this stylization from Create, not out of trying to steal their thunder, more so a modpack has fewer unique kinds of "hold shift for xx" tooltips lol
			tooltip.add(Component.translatable("packages.style_tooltip.hold_for_composition",
				Component.translatable("packages.style_tooltip.shift").withStyle(ChatFormatting.GRAY)
			).withStyle(ChatFormatting.DARK_GRAY));
		}
		
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
		if(clickAction != ClickAction.SECONDARY || !Packages.instance.config.get(PropsCommon.INVENTORY_INTERACTIONS)) {
			return super.overrideStackedOnOther(me, slot, clickAction, player);
		}
		
		if(me.getCount() != 1) return false;
		ItemStack other = slot.getItem();
		
		//These are two stackable packages. Presumably, the player wants to stack them,
		//instead of putting one inside the other. They can always be nested in-world.
		if(ItemStack.isSameItemSameTags(me, other)) return false;
		
		else return PackageContainer.mutateItemStack(me, container -> {
			if(other.isEmpty() && !container.isEmpty()) {
				//The package contains items, but the slot is empty. Take one stack of items from the package and deposit it into the slot.
				return dropIntoSlot(player, container, slot);
			} else if(!other.isEmpty()) {
				//The slot is not empty. Try to sponge up items from the slot into the package.
				boolean absorbSuccess = absorbFromSlot(player, container, slot);
				if(absorbSuccess) return true;
				else if(container.matches(other) && container.isFull()) {
					//If we're here, we're in a situation where the player clicked a slot that has an item matching the package's contents,
					//but we couldn't draw any of the items into the package because it was full.
					//In this case, we should replenish the slot's contents with more items from the package.
					int remainingSpaceInSlot = Math.max(0, other.getMaxStackSize() - other.getCount());
					if(remainingSpaceInSlot != 0) {
						return dropIntoSlot(player, container, slot);
					}
				}
			}
			return false;
		}, false);
	}
	
	@Override
	public boolean overrideOtherStackedOnMe(ItemStack me, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
		if(clickAction != ClickAction.SECONDARY || !Packages.instance.config.get(PropsCommon.INVENTORY_INTERACTIONS)) {
			return super.overrideOtherStackedOnMe(me, other, slot, clickAction, player, slotAccess);
		}
		
		if(me.getCount() != 1) return false;
		
		//These are two stackable packages. Presumably, the player wants to stack them,
		//instead of putting one inside the other. They can always be nested in-world.
		if(ItemStack.isSameItemSameTags(me, other)) return false;
		
		//Otherwise try to eat up those items.
		else return PackageContainer.mutateItemStack(me, container -> {
			if(container.matches(other)) {
				ItemStack insertionLeftover = container.insert(other, Integer.MAX_VALUE, false);
				if(insertionLeftover.getCount() != other.getCount()) { //If the amount of items changed
					//We can't directly set the ItemStack on the player's cursor, but we can leverage how `insertionLeftover` and
					//`other` both have the same item and nbt tags.
					other.setCount(insertionLeftover.getCount());
					player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.getLevel().getRandom().nextFloat() * 0.4f);
					return true;
				}
			}
			return false;
		}, false);
	}
	
	//Mop up items from a slot into the PackageContainer. Always grabs as much as it can.
	//Assumes that the contents of `slot` can stack with the contents of the PackageContainer.
	private boolean absorbFromSlot(Player player, PackageContainer container, Slot slot) {
		if(slot.getItem().isEmpty()) return false;
		if(!container.matches(slot.getItem())) return false;
		if(!container.allowedInPackageAtAll(slot.getItem())) return false; //todo this shouldn't be an ad-hoc check...... reh!
		
		int remainingSpaceInPackage = container.maxStackAmountAllowed(slot.getItem()) * 8 - container.getCount(); //todo break this out into a method on packagecontainer probably
		
		//pull it out of the slot... this happens for real, no take backsies past this point
		//for a slot with !allowModification (which, in practice, is crafting slots) the second argument is used as a threshold.
		//you can't take less than arg#2 items from an !allowModification slot. this covers cases like, there being one remaining
		//spot in the package, but the crafting output slot has 4 items in it - it just wont take at all
		//for all slots the smaller of the two numeric arguments is used as the maximum amount to take. overspecifying won't dupe
		ItemStack grabbedFromSlot = slot.safeTake(remainingSpaceInPackage, remainingSpaceInPackage, player);
		if(grabbedFromSlot.isEmpty()) return false;
		
		ItemStack insertionLeftover = container.insert(grabbedFromSlot, Integer.MAX_VALUE, false);
		
		if(!insertionLeftover.isEmpty()) {
			//TODO: what happens if `insert` returns nonempty stack? what cases might this come up in?
			Packages.LOGGER.warn("Non-empty stack (" + insertionLeftover + ") appeared in absorbFromSlot action from player " + player.getScoreboardName() + ". Can you file an issue about what caused this?");
		}
		
		player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.getLevel().getRandom().nextFloat() * 0.4f);
		return true;
	}
	
	//Drop a stack of items from a PackageContainer into a slot.
	private boolean dropIntoSlot(Player player, PackageContainer container, Slot slot) {
		//really checking if the *slot* matches the *container*, but slot doesnt have a handy method for that
		if(!container.matches(slot.getItem())) return false;
		
		int remainingSpaceInSlot = Math.max(0, slot.getMaxStackSize() - slot.getItem().getCount());
		if(remainingSpaceInSlot == 0) return false; //No room to add any more items.
		
		//Intentionally using getFilterStack().getMaxStackSize() here, instead of PackageContainer#getMaxStackSize(),
		//because for cases where you have a package of packages, I want to deposit the slot-side concept of "one stack" (all of them)
		//and not the package's idea of "one stack" (one of them).
		int oneStackFromPackage = container.getFilterStack().getMaxStackSize();
		
		int amountToDrop = Math.min(remainingSpaceInSlot, oneStackFromPackage);
		ItemStack toPlace = container.take(amountToDrop, true);
		if(slot.mayPlace(toPlace)) {
			toPlace = container.take(amountToDrop, false);
			ItemStack slotInsertionLeftover = slot.safeInsert(toPlace);
			
			if(!slotInsertionLeftover.isEmpty()) {
				//TODO: what happens if `safeInsert` returns nonempty stack? what cases might this come up in?
				Packages.LOGGER.warn("Non-empty stack (" + slotInsertionLeftover + ") appeared in dropIntoSlot action from player " + player.getScoreboardName() + ". Can you file an issue about what caused this? Thanks.");
			}
			
			player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 0.8f + player.getLevel().getRandom().nextFloat() * 0.4f);
			return true;
		}
		return false;
	}
}
