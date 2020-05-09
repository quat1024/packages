package agency.highlysuspect.packages.mixin.client;

import agency.highlysuspect.packages.junk.BakedQuadExt;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements BakedQuadExt {
	@Shadow @Final protected Sprite sprite;
	
	@Override
	public Sprite pkgs$getSprite() {
		return sprite;
	}
}
