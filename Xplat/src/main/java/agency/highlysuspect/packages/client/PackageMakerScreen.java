package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.menu.PackageMakerMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PackageMakerScreen extends AbstractContainerScreen<PackageMakerMenu> {
	private static final ResourceLocation TEXTURE = Packages.id("textures/gui/package_maker.png");
	
	public PackageMakerScreen(PackageMakerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}
	
	private static final Map<Integer, String> SLOTS_TO_TOOLTIPS = new HashMap<>();
	static {
		SLOTS_TO_TOOLTIPS.put(PackageMakerBlockEntity.FRAME_SLOT, Packages.MODID + ".package_maker.frame");
		SLOTS_TO_TOOLTIPS.put(PackageMakerBlockEntity.INNER_SLOT, Packages.MODID + ".package_maker.inner");
		SLOTS_TO_TOOLTIPS.put(PackageMakerBlockEntity.DYE_SLOT, Packages.MODID + ".package_maker.dye");
		SLOTS_TO_TOOLTIPS.put(PackageMakerBlockEntity.EXTRA_SLOT, Packages.MODID + ".package_maker.extra");
		SLOTS_TO_TOOLTIPS.put(PackageMakerBlockEntity.OUTPUT_SLOT, Packages.MODID + ".package_maker.output");
	}
	
	private Button craftButton;
	
	/**
	 * @see PackageMakerMenu#clickMenuButton(Player, int) 
	 */
	@Override
	protected void init() {
		super.init();
		
		craftButton = Button.builder(Component.translatable(Packages.MODID + ".package_maker.craft_button"), (__) -> {
			assert minecraft != null;
			assert minecraft.gameMode != null;
			minecraft.gameMode.handleInventoryButtonClick(menu.containerId, hasShiftDown() ? 1 : 0);
		}).bounds((width / 2) - 25, topPos + 33, 50, 20).build();
		
		addRenderableWidget(craftButton);
	}
	
	@Override
	public void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
		ItemStack currentOutput = menu.slots.get(PackageMakerBlockEntity.OUTPUT_SLOT).getItem();
		boolean full = currentOutput.getCount() == currentOutput.getMaxStackSize();
		boolean cantCraft = PackageMakerBlockEntity.whatWouldBeCrafted(menu.container).isEmpty();
		craftButton.active = !(full || cantCraft);
		
		//I dont know what im doing \:D/
		this.renderBackground(guiGraphics);
		
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
		
		this.renderTooltip(guiGraphics, mouseX, mouseY);
		
		if(hoveredSlot != null && !hoveredSlot.hasItem()) {
			String tooltip = SLOTS_TO_TOOLTIPS.get(hoveredSlot.index);
			if(tooltip != null) {
				List<Component> toot = new ArrayList<>();
				toot.add(Component.translatable(tooltip));
				
				for(int k = 1; true; k++) {
					String line = tooltip + "." + k;
					if(!Language.getInstance().has(line)) break;
					//this lets you make the tooltip have fewer lines using a resourcepack (which can't actually remove lang keys, only override them)
					if(Language.getInstance().getOrDefault(line).contains("ZZZ")) break;
					toot.add(Component.translatable(line).withStyle(ChatFormatting.DARK_GRAY));
				}
				
				guiGraphics.renderTooltip(font, toot, Optional.empty(), mouseX, mouseY);
			}
		}
	}
	
	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		super.renderLabels(graphics, mouseX, mouseY);
		
		PackageMakerMenu menu = getMenu();
		
		if(!menu.slots.get(PackageMakerBlockEntity.OUTPUT_SLOT).hasItem()) {
			//draw a preview of the crafted item behind a transparent overlay
			//I guess this makes sense ?? lmao
			ItemStack dryRun = PackageMakerBlockEntity.whatWouldBeCrafted(menu.container);
			if (!dryRun.isEmpty()) {
				int x = menu.slots.get(PackageMakerBlockEntity.OUTPUT_SLOT).x;
				int y = menu.slots.get(PackageMakerBlockEntity.OUTPUT_SLOT).y;
				
				graphics.renderFakeItem(dryRun, x, y);
				
				RenderSystem.disableDepthTest();
				RenderSystem.colorMask(true, true, true, false);
				graphics.fillGradient(x - 6, y - 6, x + 22, y + 22, 0x66b44b4b, 0x66b44b4b);
				RenderSystem.colorMask(true, true, true, true);
				RenderSystem.enableDepthTest();
			}
		}
	}
}
