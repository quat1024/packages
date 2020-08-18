package agency.highlysuspect.packages.mixin.client;

import agency.highlysuspect.packages.client.PackageBlockEntityRenderer;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.TwelveDirection;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
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
		method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
		at = @At("HEAD")
	)
	public void onRenderItemVeryEarly(ItemStack stack, ModelTransformation.Mode transformMode, boolean invert, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if(stack.getItem() == PItems.PACKAGE) {
			PItems.PACKAGE.getContainedStack(stack).ifPresent(inner -> {
					matrixStack.push();
					model.getTransformation().getTransformation(transformMode).apply(invert, matrixStack);
					PackageBlockEntityRenderer.drawItem(matrixStack, vertexConsumerProvider, TwelveDirection.NORTH, inner, light);
					matrixStack.pop();
				}
			);
		}
	}
}
