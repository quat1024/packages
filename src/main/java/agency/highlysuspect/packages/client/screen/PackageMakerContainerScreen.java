package agency.highlysuspect.packages.client.screen;

import agency.highlysuspect.packages.container.PackageMakerContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.util.Identifier;

public class PackageMakerContainerScreen extends ContainerScreen<PackageMakerContainer> {
	private static final Identifier TEXTURE = new Identifier("textures/gui/container/dispenser.png");
	
	public PackageMakerContainerScreen(PackageMakerContainer container) {
		super(container, container.playerInventory, container.be.getDisplayName());
	}
	
	public void render(int mouseX, int mouseY, float delta) {
		this.renderBackground();
		super.render(mouseX, mouseY, delta);
		this.drawMouseoverTooltip(mouseX, mouseY);
	}
	
	protected void drawForeground(int mouseX, int mouseY) {
		String string = this.title.asFormattedString();
		this.font.draw(string, (float)(this.containerWidth / 2 - this.font.getStringWidth(string) / 2), 6.0F, 4210752);
		this.font.draw(this.playerInventory.getDisplayName().asFormattedString(), 8.0F, (float)(this.containerHeight - 96 + 2), 4210752);
	}
	
	protected void drawBackground(float delta, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.containerWidth) / 2;
		int j = (this.height - this.containerHeight) / 2;
		this.blit(i, j, 0, 0, this.containerWidth, this.containerHeight);
	}
}
