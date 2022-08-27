package agency.highlysuspect.packages.mixin.client;

import agency.highlysuspect.packages.client.PackageRenderer;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.TwelveDirection;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
	value = ItemRenderer.class,
	priority = 990 //Earlier than Indigo's MixinItemRenderer, which uses the default of 1000
)
public class ItemRendererMixin {
	@Inject(
		method = "render",
		at = @At("HEAD")
	)
	public void onRenderItemVeryEarly(ItemStack stack, ItemTransforms.TransformType transformMode, boolean invert, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if(stack.getItem() == PItems.PACKAGE) {
			PItems.PACKAGE.getContainedStack(stack).ifPresent(inner -> {
					matrixStack.pushPose();
					model.getTransforms().getTransform(transformMode).apply(invert, matrixStack);
					PackageRenderer.applyRotation(matrixStack, TwelveDirection.NORTH);
					PackageRenderer.drawItem(matrixStack, vertexConsumerProvider, inner, light);
					matrixStack.popPose();
				}
			);
		}
	}
}
