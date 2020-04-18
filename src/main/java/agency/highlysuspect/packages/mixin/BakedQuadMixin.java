package agency.highlysuspect.packages.mixin;

import agency.highlysuspect.packages.junk.BakedQuadExt;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements BakedQuadExt {
	@Shadow @Final @Mutable protected Sprite sprite;
	@Shadow @Final @Mutable protected int colorIndex;
	
	@Override
	public Sprite pkgs$getSprite() {
		return sprite;
	}
	
	@Override
	public void pkgs$setSprite(Sprite sprite) {
		this.sprite = sprite;
	}
	
	@Override
	public void pkgs$setColorIndex(int colorIndex) {
		this.colorIndex = colorIndex;
	}
}
