package agency.highlysuspect.packages.mixin.particleslol;

import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextureSheetParticle.class)
public interface AccessorTextureSheetParticle {
	@Invoker("setSprite") void packages$setSprite(TextureAtlasSprite real);
}
