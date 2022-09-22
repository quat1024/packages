package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.junk.PackageContainer;
import agency.highlysuspect.packages.junk.TwelveDirection;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PackageRenderer implements BlockEntityRenderer<PackageBlockEntity> {
	public PackageRenderer(BlockEntityRendererProvider.Context context) {
		textRenderer = context.getFont();
	}
	
	private final Font textRenderer;
	
	@Override
	public void render(PackageBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		/// Setup
		Entity player = Minecraft.getInstance().getCameraEntity();
		if(blockEntity.getLevel() == null || player == null) return;
		
		Level world = blockEntity.getLevel();
		BlockState packageState = blockEntity.getBlockState();
		if(!(packageState.getBlock() instanceof PackageBlock)) return;
		TwelveDirection packageTwelveDir = packageState.getValue(PackageBlock.FACING);
		PackageContainer container = blockEntity.getContainer();
		int count = container.getCount();
		ItemStack stack = container.getFilterStack();
		//The block is solid, so has no light inside; use the light of whatever's in front instead.
		light = LevelRenderer.getLightColor(world, blockEntity.getBlockPos().relative(packageTwelveDir.primaryDirection));
		
		/// Prepare
		matrices.pushPose();
		matrices.translate(0.5, 0.5, 0.5);
		applyRotation(matrices, packageTwelveDir);
		
		/// Item
		if(!stack.isEmpty()) drawItem(matrices, vertexConsumers, stack, light);
		
		/// Text
		HitResult hit = player.pick(8, 0, false);
		boolean showText = hit instanceof BlockHitResult blockHit && blockEntity.getBlockPos().equals(blockHit.getBlockPos());
		boolean detailed = false;
		double distance = player.getEyePosition(1).distanceTo(Vec3.atCenterOf(blockEntity.getBlockPos()));
		if(player.isShiftKeyDown()) {
			if(showText) detailed = true;
			if(!showText && distance <= 8) showText = true;
		}
		
		if(showText) drawText(matrices, vertexConsumers, light, container, detailed, distance);
		
		matrices.popPose();
	}
	
	public static void applyRotation(PoseStack ps, TwelveDirection dir) {
		//Rotate into position. This might be jank, just blindly copied from Worse Barrels really.
		//Only moves the pose matrix, not the normal matrix, so item lighting comes from the correct direction
		Matrix4f pose = ps.last().pose();
		if(dir.primaryDirection.get2DDataValue() == -1) { //up/down
			pose.multiply(Vector3f.YP.rotationDegrees(-dir.secondaryDirection.toYRot() + 90));
			pose.multiply(Vector3f.ZP.rotationDegrees(dir.primaryDirection == Direction.UP ? 90 : -90));
		} else {
			pose.multiply(Vector3f.YP.rotationDegrees(-dir.primaryDirection.toYRot() - 90));
		}
	}
	
	private static int depth = 0;
	public static void drawItem(PoseStack ps, MultiBufferSource bufs, ItemStack stack, int light) {
		ps.pushPose();
		
		//Only moves the pose matrix, not the normal matrix, so items appear flat but are shaded as if they're not flat.
		Matrix4f pose = ps.last().pose();
		if(depth == 0) {
			pose.multiply(Matrix4f.createTranslateMatrix(6 / 16f + 0.006f, 0, 0));
			pose.multiply(Vector3f.YP.rotationDegrees(90));
			pose.multiply(Matrix4f.createScaleMatrix(0.75f, 0.75f, 0.005f)); //it's flat fuck friday!!!!!
		} else {
			//Don't think about this too hard, just a workaround to slightly space out deeply-nested items.
			//If I don't do this, situations like packages-inside-packages-inside-packages start zfighting pretty hard.
			pose.multiply(Matrix4f.createTranslateMatrix(6 / 16f + 0.07f, 0, 0)); //Lift it out more
			pose.multiply(Vector3f.YP.rotationDegrees(90));
			pose.multiply(Matrix4f.createScaleMatrix(0.75f, 0.75f, depth * 0.06f)); //Scale it down less (and even less, for further depths)
		}
		
		try {
			depth++;
			if(depth < 5) {
				Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GUI, light, OverlayTexture.NO_OVERLAY, ps, bufs, 0);
			}
		} finally {
			depth--;
			ps.popPose();
		}
	}
	
	private void drawText(PoseStack matrices, MultiBufferSource vertexConsumers, int light, PackageContainer container, boolean detailed, double distance) {
		if(Minecraft.getInstance().gameMode == null) return;
		
		int count = container.getCount();
		int max = container.maxStackAmountAllowed(container.getFilterStack());
		
		String text;
		if(detailed) {
			if(max == 1) text = count + "x1";
			else {
				int stacks = count / max;
				int leftover = count % max;
				text = stacks + "x" + max + " + " + leftover;
			}
		} else text = String.valueOf(count);
		
		int color = (container.isFull() ? 0x00FF6600 : 0x00FFFFFF) | (distance - 0.5 >= Minecraft.getInstance().gameMode.getPickRange() ? 0x55000000 : 0xFF000000);
		int shadowColor = (color & 0xFCFCFC) >> 2; //I um, okay, so, this is kind of a weird color algorithm. Wrote this like 2yrs ago lmao
		
		float scale;
		if(detailed && max == 1) scale = 1/30f;
		else if(detailed) scale = 1/70f;
		else if(count < 10) scale = 1/15f;
		else if(count < 100) scale = 1/23f;
		else scale = 1/30f;
		
		matrices.pushPose();
		
		matrices.translate(6 / 16d + 0.05, Packages.config.fontVerticalShift, 0);
		matrices.scale(-1, -scale, scale);
		matrices.translate(0, -4, 0);
		matrices.mulPose(Vector3f.YP.rotationDegrees(90));
		
		int minusHalfWidth = -textRenderer.width(text) / 2;
		textRenderer.drawInBatch(text, minusHalfWidth + 1, 1, shadowColor, false, matrices.last().pose(), vertexConsumers, false, 0, light); //Background
		matrices.translate(0, 0, -0.001);
		textRenderer.drawInBatch(text, minusHalfWidth,     0, color      , false, matrices.last().pose(), vertexConsumers, false, 0, light); //Foreground
		
		matrices.popPose();
	}
}
