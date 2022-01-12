package agency.highlysuspect.packages.client.screen;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.entity.PackageMakerBlockEntity;
import agency.highlysuspect.packages.container.PackageMakerMenu;
import agency.highlysuspect.packages.net.PNetClient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PackageMakerScreen extends AbstractContainerScreen<PackageMakerMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(PackagesInit.MODID, "textures/gui/package_maker.png");
	
	public PackageMakerScreen(PackageMakerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}
	
	private Button buddon;
	
	private static final String[] SLOTS_TO_TOOLTIPS = Util.make(new String[3], (m) -> {
		m[PackageMakerBlockEntity.FRAME_SLOT] = PackagesInit.MODID + ".package_maker.frame";
		m[PackageMakerBlockEntity.INNER_SLOT] = PackagesInit.MODID + ".package_maker.inner";
		m[PackageMakerBlockEntity.DYE_SLOT] = PackagesInit.MODID + ".package_maker.dye";
	});
	
	@Override
	protected void init() {
		super.init();
		buddon = new Button((width / 2) - 25, topPos + 33, 50, 20, new TranslatableComponent(PackagesInit.MODID + ".package_maker.craft_button"), (button) -> PNetClient.requestPackageMakerCraft(hasShiftDown()));
		addWidget(buddon);
	}
	
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		//Begin copy paste
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.renderTooltip(matrices, mouseX, mouseY);
		//End copy paste
		
		if(hoveredSlot != null && !hoveredSlot.hasItem()) {
			//This is jank as hell but it works good enough
			//TODO what the hec
			for(int i = PackageMakerBlockEntity.FRAME_SLOT; i <= PackageMakerBlockEntity.DYE_SLOT; i++) {
				if(hoveredSlot.index == i + 1) {
					renderTooltip(matrices, new TranslatableComponent(SLOTS_TO_TOOLTIPS[i]), mouseX, mouseY);
					break;
				}
			}
		}
		
		buddon.render(matrices, mouseX, mouseY, delta);
	}
	
	protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
		super.renderLabels(matrices, mouseX, mouseY);
		
		PackageMakerMenu menu = getMenu();
		
		if(!getMenu().slots.get(0).hasItem()) {
			//draw a preview of the crafted item behind a transparent overlay
			//I guess this makes sense ?? lmao
			ItemStack dryRun = menu.be.whatWouldBeCrafted();
			if (!dryRun.isEmpty()) {
				int x = menu.slots.get(0).x;
				int y = menu.slots.get(0).y;
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
	
	public static void onInitializeClient() {
		ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register((tex, reg) -> {
			reg.register(PackageMakerMenu.FRAME_BG);
			reg.register(PackageMakerMenu.INNER_BG);
			reg.register(PackageMakerMenu.DYE_BG);
		});
	}
}
