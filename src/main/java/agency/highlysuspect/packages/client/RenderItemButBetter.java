package agency.highlysuspect.packages.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;

public class RenderItemButBetter {
	@Environment(EnvType.CLIENT)
	public static void renderItemTransparent(LivingEntity entity, ItemStack itemStack, int x, int y, float alpha) {
		ItemRenderer delegate = MinecraftClient.getInstance().getItemRenderer();
		
		if (!itemStack.isEmpty()) {
			delegate.zOffset += 50.0F;
			
			try {
				poggers(itemStack, x, y, delegate.getHeldItemModel(itemStack, (World)null, entity), alpha);
			} catch (Throwable var8) {
				CrashReport crashReport = CrashReport.create(var8, "Rendering item");
				CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
				crashReportSection.add("Item Type", () -> {
					return String.valueOf(itemStack.getItem());
				});
				crashReportSection.add("Item Damage", () -> {
					return String.valueOf(itemStack.getDamage());
				});
				crashReportSection.add("Item NBT", () -> {
					return String.valueOf(itemStack.getTag());
				});
				crashReportSection.add("Item Foil", () -> {
					return String.valueOf(itemStack.hasEnchantmentGlint());
				});
				throw new CrashException(crashReport);
			}
			
			delegate.zOffset -= 50.0F;
		}
	}
	
	@Environment(EnvType.CLIENT)
	protected static void poggers(ItemStack stack, int x, int y, BakedModel model, float alpha) {
		ItemRenderer delegate = MinecraftClient.getInstance().getItemRenderer();
		
		RenderSystem.pushMatrix();
		MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
		MinecraftClient.getInstance().getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).setFilter(false, false);
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(0.0F, 1.0F, 1.0F, alpha); //This 1 line is what I copied this whole class for
		RenderSystem.translatef((float)x, (float)y, 100.0F + delegate.zOffset);
		RenderSystem.translatef(8.0F, 8.0F, 0.0F);
		RenderSystem.scalef(1.0F, -1.0F, 1.0F);
		RenderSystem.scalef(16.0F, 16.0F, 16.0F);
		MatrixStack matrixStack = new MatrixStack();
		VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
		boolean bl = !model.isSideLit();
		if (bl) {
			DiffuseLighting.disableGuiDepthLighting();
		}
		
		delegate.renderItem(stack, ModelTransformation.Mode.GUI, false, matrixStack, immediate, 15728880, OverlayTexture.DEFAULT_UV, model);
		immediate.draw();
		RenderSystem.enableDepthTest();
		if (bl) {
			DiffuseLighting.enableGuiDepthLighting();
		}
		
		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();
		RenderSystem.popMatrix();
	}
}
