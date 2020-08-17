package agency.highlysuspect.packages.client.screen;

import agency.highlysuspect.packages.PackagesInit;
import agency.highlysuspect.packages.block.entity.PackageMakerBlockEntity;
import agency.highlysuspect.packages.container.PackageMakerScreenHandler;
import agency.highlysuspect.packages.net.PNetClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class PackageMakerScreen extends HandledScreen<PackageMakerScreenHandler> {
	private static final Identifier TEXTURE = new Identifier(PackagesInit.MODID, "textures/gui/package_maker.png");
	
	public PackageMakerScreen(PackageMakerScreenHandler sh, PlayerInventory inventory, Text title) {
		super(sh, inventory, title);
	}
	
	private static final String[] SLOTS_TO_TOOLTIPS = Util.make(new String[3], (m) -> {
		m[PackageMakerBlockEntity.FRAME_SLOT] = PackagesInit.MODID + ".package_maker.frame";
		m[PackageMakerBlockEntity.INNER_SLOT] = PackagesInit.MODID + ".package_maker.inner";
		m[PackageMakerBlockEntity.DYE_SLOT] = PackagesInit.MODID + ".package_maker.dye";
	});
	
	@Override
	protected void init() {
		super.init();
		addButton(new ButtonWidget((width / 2) - 25, y + 33, 50, 20, new TranslatableText(PackagesInit.MODID + ".package_maker.craft_button"), (button) -> PNetClient.requestPackageMakerCraft(hasShiftDown())));
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		//Begin copy paste
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
		//End copy paste
		
		if(focusedSlot != null && !focusedSlot.hasStack()) {
			//This is jank as hell but it works good enough
			//TODO what the hec
			for(int i = PackageMakerBlockEntity.FRAME_SLOT; i <= PackageMakerBlockEntity.DYE_SLOT; i++) {
				if(focusedSlot.id == i + 1) {
					renderTooltip(matrices, new TranslatableText(SLOTS_TO_TOOLTIPS[i]), mouseX, mouseY);
					break;
				}
			}
		}
	}
	
	protected void drawForeground(MatrixStack matrixStack, int i, int j) {
		//Begin copy paste
		this.textRenderer.draw(matrixStack, this.title, (float)(this.backgroundWidth / 2 - this.textRenderer.getWidth(this.title) / 2), 6.0F, 4210752);
		this.textRenderer.draw(matrixStack, this.playerInventory.getDisplayName(), 8.0F, (float)(this.backgroundHeight - 96 + 2), 4210752);
		//End copy paste
		
		PackageMakerScreenHandler screenHandler = getScreenHandler();
		
		if(!getScreenHandler().slots.get(0).hasStack()) {
			//draw a preview of the crafted item behind a transparent overlay
			//I guess this makes sense ?? lmao
			ItemStack dryRun = screenHandler.be.whatWouldBeCrafted();
			if (!dryRun.isEmpty()) {
				int x = screenHandler.slots.get(0).x;
				int y = screenHandler.slots.get(0).y;
				itemRenderer.renderInGui(dryRun, x, y);
				
				RenderSystem.disableDepthTest();
				RenderSystem.colorMask(true, true, true, false);
				this.fillGradient(matrixStack, x - 6, y - 6, x + 22, y + 22, 0x66b44b4b, 0x66b44b4b);
				RenderSystem.colorMask(true, true, true, true);
				RenderSystem.enableDepthTest();
			}
		}
	}
	
	//Copy paste from Generic3x3ContainerScreen
	@SuppressWarnings("ConstantConditions")
	@Override
	protected void drawBackground(MatrixStack matrixStack, float f, int mouseY, int i) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bindTexture(TEXTURE);
		int j = (this.width - this.backgroundWidth) / 2;
		int k = (this.height - this.backgroundHeight) / 2;
		this.drawTexture(matrixStack, j, k, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}
}
