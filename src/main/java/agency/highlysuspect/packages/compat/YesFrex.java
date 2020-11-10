package agency.highlysuspect.packages.compat;

import grondag.frex.api.material.MaterialMap;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;

public class YesFrex implements FrexProxy {
	@Override
	public boolean isFrex() {
		return true;
	}
	
	@Override
	public RenderMaterial getMaterial(BlockState state, Sprite sprite) {
		return MaterialMap.get(state).getMapped(sprite);
	}
}
