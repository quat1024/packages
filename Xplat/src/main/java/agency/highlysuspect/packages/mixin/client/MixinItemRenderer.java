package agency.highlysuspect.packages.mixin.client;

import agency.highlysuspect.packages.client.PackageRenderer;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PackageContainer;
import agency.highlysuspect.packages.junk.TwelveDirection;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * I'm aware that using a BlockEntityWithoutLevelRenderer would be more correct than hacking into item renderer guts -
 * I'm having skill issue implementing that though lol
 */
@Mixin(value = ItemRenderer.class, priority = 990) //Earlier than Indigo's MixinItemRenderer, which uses the default of 1000
public class MixinItemRenderer {
	@Inject(method = "render", at = @At("HEAD"))
	public void packages$renderItemVeryEarly(ItemStack stack, ItemDisplayContext ctx, boolean invert, PoseStack pose, MultiBufferSource bufs, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if(stack.getItem() == PItems.PACKAGE.get()) {
			PackageContainer container = PackageContainer.fromItemStack(stack, true);
			if(container == null) return;
			
			ItemStack inner = container.getFilterStack();
			if(inner.isEmpty()) return;
			
			pose.pushPose();
			model.getTransforms().getTransform(ctx).apply(invert, pose);
			PackageRenderer.applyRotation(pose, TwelveDirection.NORTH);
			PackageRenderer.drawItem(pose, bufs, inner, light);
			pose.popPose();
		}
	}
}
