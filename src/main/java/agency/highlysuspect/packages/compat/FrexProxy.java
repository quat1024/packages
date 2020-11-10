package agency.highlysuspect.packages.compat;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;

public interface FrexProxy {
	boolean isFrex();
	RenderMaterial getMaterial(BlockState state, Sprite sprite);
}
