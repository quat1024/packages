package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Init;
import agency.highlysuspect.packages.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.container.PackageMakerMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PackageMakerScreen extends AbstractContainerScreen<PackageMakerMenu> {
	private static final ResourceLocation TEXTURE = Init.id("textures/gui/package_maker.png");
	
	public PackageMakerScreen(PackageMakerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}
	
	private static final Map<Integer, String> SLOTS_TO_TOOLTIPS = new HashMap<>();
	static {
		SLOTS_TO_TOOLTIPS.put(PackageMakerBlockEntity.FRAME_SLOT, Init.MODID + ".package_maker.frame");
		SLOTS_TO_TOOLTIPS.put(PackageMakerBlockEntity.INNER_SLOT, Init.MODID + ".package_maker.inner");
		SLOTS_TO_TOOLTIPS.put(PackageMakerBlockEntity.DYE_SLOT, Init.MODID + ".package_maker.dye");
		SLOTS_TO_TOOLTIPS.put(PackageMakerBlockEntity.OUTPUT_SLOT, Init.MODID + ".package_maker.output");
	}
	
	@Override
	protected void init() {
		super.init();
		addRenderableWidget(new Button((width / 2) - 25, topPos + 33, 50, 20, new TranslatableComponent(Init.MODID + ".package_maker.craft_button"), (button) -> {
			if(hasShiftDown()) sendButtonClick(1);
			else sendButtonClick(0);
		}));
	}
	
	private void sendButtonClick(int id) {
		assert this.minecraft != null;
		assert this.minecraft.gameMode != null;
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
	}
	
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		//Begin copy paste
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.renderTooltip(matrices, mouseX, mouseY);
		//End copy paste
		
		if(hoveredSlot != null && !hoveredSlot.hasItem()) {
			String tooltip = SLOTS_TO_TOOLTIPS.get(hoveredSlot.index);
			if(tooltip != null) renderTooltip(matrices, new TranslatableComponent(tooltip), mouseX, mouseY);
		}
	}
	
	protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
		super.renderLabels(matrices, mouseX, mouseY);
		
		PackageMakerMenu menu = getMenu();
		
		if(!menu.slots.get(PackageMakerBlockEntity.OUTPUT_SLOT).hasItem()) {
			//draw a preview of the crafted item behind a transparent overlay
			//I guess this makes sense ?? lmao
			ItemStack dryRun = PackageMakerBlockEntity.whatWouldBeCrafted(
				menu.slots.get(PackageMakerBlockEntity.FRAME_SLOT).getItem(),
				menu.slots.get(PackageMakerBlockEntity.INNER_SLOT).getItem(),
				menu.slots.get(PackageMakerBlockEntity.DYE_SLOT).getItem()
			);
			if (!dryRun.isEmpty()) {
				int x = menu.slots.get(PackageMakerBlockEntity.OUTPUT_SLOT).x;
				int y = menu.slots.get(PackageMakerBlockEntity.OUTPUT_SLOT).y;
				itemRenderer.renderAndDecorateFakeItem(dryRun, x, y);
				
				RenderSystem.disableDepthTest();
				RenderSystem.colorMask(true, true, true, false);
				this.fillGradient(matrices, x - 6, y - 6, x + 22, y + 22, 0x66b44b4b, 0x66b44b4b);
				RenderSystem.colorMask(true, true, true, true);
				RenderSystem.enableDepthTest();
			}
		}
	}
	
	//Copy paste from Generic3x3ContainerScreen
	@Override
	protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(matrices, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}
	
	public static void initIcons() {
		ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register((tex, reg) -> {
			reg.register(PackageMakerMenu.FRAME_BG);
			reg.register(PackageMakerMenu.INNER_BG);
			reg.register(PackageMakerMenu.DYE_BG);
		});
	}
}
