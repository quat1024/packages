package agency.highlysuspect.packages.client.screen;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.entity.PackageMakerBlockEntity;
import agency.highlysuspect.packages.client.RenderItemButBetter;
import agency.highlysuspect.packages.container.PackageMakerContainer;
import agency.highlysuspect.packages.net.PNetClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class PackageMakerContainerScreen extends ContainerScreen<PackageMakerContainer> {
	private static final Identifier TEXTURE = new Identifier(PackagesInit.MODID, "textures/gui/package_maker.png");
	
	public PackageMakerContainerScreen(PackageMakerContainer container) {
		super(container, container.playerInventory, container.be.getDisplayName());
	}
	
	private static final String[] SLOTS_TO_TOOLTIPS = Util.make(new String[3], (m) -> {
		m[PackageMakerBlockEntity.FRAME_SLOT] = PackagesInit.MODID + ".package_maker.frame";
		m[PackageMakerBlockEntity.INNER_SLOT] = PackagesInit.MODID + ".package_maker.inner";
		m[PackageMakerBlockEntity.DYE_SLOT] = PackagesInit.MODID + ".package_maker.dye";
	});
	
	@Override
	protected void init() {
		super.init();
		addButton(new ButtonWidget(x + containerWidth / 2 - 25, y + 33, 50, 20, I18n.translate(PackagesInit.MODID + ".package_maker.craft_button"), (button) -> PNetClient.requestPackageMakerCraft(hasShiftDown())));
	}
	
	public void render(int mouseX, int mouseY, float delta) {
		this.renderBackground();
		super.render(mouseX, mouseY, delta);
		this.drawMouseoverTooltip(mouseX, mouseY);
		
		if(focusedSlot != null && !focusedSlot.hasStack()) {
			//This is jank as hell but it works good enough
			//TODO what the hec
			for(int i = PackageMakerBlockEntity.FRAME_SLOT; i <= PackageMakerBlockEntity.DYE_SLOT; i++) {
				if(focusedSlot.id == i + 1) {
					renderTooltip(I18n.translate(SLOTS_TO_TOOLTIPS[i]), mouseX, mouseY);
					break;
				}
			}
		}
	}
	
	protected void drawForeground(int mouseX, int mouseY) {
		String string = this.title.asFormattedString();
		this.font.draw(string, (float)(this.containerWidth / 2 - this.font.getStringWidth(string) / 2), 6.0F, 4210752);
		this.font.draw(this.playerInventory.getDisplayName().asFormattedString(), 8.0F, (float)(this.containerHeight - 96 + 2), 4210752);
		
		if(!container.slots.get(0).hasStack()) {
			//draw a preview of the crafted item behind a transparent overlay
			//I guess this makes sense ?? lmao
			ItemStack dryRun = container.be.whatWouldBeCrafted();
			if (!dryRun.isEmpty()) {
				int x = container.slots.get(0).xPosition;
				int y = container.slots.get(0).yPosition;
				//noinspection ConstantConditions
				itemRenderer.renderGuiItem(this.minecraft.player, dryRun, x, y);
				
				RenderSystem.disableDepthTest();
				RenderSystem.colorMask(true, true, true, false);
				this.fillGradient(x - 6, y - 6, x + 22, y + 22, 0x66b44b4b, 0x66b44b4b);
				RenderSystem.colorMask(true, true, true, true);
				RenderSystem.enableDepthTest();
			}
		}
	}
	
	protected void drawBackground(float delta, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.containerWidth) / 2;
		int j = (this.height - this.containerHeight) / 2;
		this.blit(i, j, 0, 0, this.containerWidth, this.containerHeight);
	}
}
