package agency.highlysuspect.packages.compat;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;

public class NoFrex implements FrexProxy {
	@Override
	public boolean isFrex() {
		return false;
	}
	
	@Override
	public RenderMaterial getMaterial(BlockState state, Sprite sprite) {
		return RendererAccess.INSTANCE.getRenderer().materialFinder().find();
	}
}
