package agency.highlysuspect.packages.client.compat.frex;

import agency.highlysuspect.packages.junk.PackageStyle;
import grondag.frex.api.material.MaterialMap;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;

public class YesFrex implements FrexProxy {
	@Override
	public boolean isFrex() {
		return true;
	}
	
	@Override
	public void fancifyPackageQuad(QuadEmitter emitter, PackageStyle style) {
		SpriteFinder spriteFinder = SpriteFinder.get(MinecraftClient.getInstance().getBakedModelManager().method_24153(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)); //oof
		
		if(emitter.tag() == 1 || emitter.tag() == 2) {
			BlockState state = (emitter.tag() == 1 ? style.frameBlock : style.innerBlock).getDefaultState();
			Sprite sprite = spriteFinder.find(emitter, 0);
			RenderMaterial mat = MaterialMap.get(state).getMapped(sprite);
			emitter.material(mat);
		}
	}
}
