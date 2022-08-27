package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
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
		if(blockEntity == null || blockEntity.getLevel() == null) return;
		
		//Gather some data
		Level world = blockEntity.getLevel();
		Minecraft client = Minecraft.getInstance();
		
		BlockState packageState = blockEntity.getBlockState();
		if(!(packageState.getBlock() instanceof PackageBlock)) return;
		
		TwelveDirection packageTwelveDir = packageState.getValue(PackageBlock.FACING);
		
		//get the light level of whatever's in front
		//Quick fix for my block being solid so it has no light inside...
		light = LevelRenderer.getLightColor(world, blockEntity.getBlockPos().relative(packageTwelveDir.primaryDirection));
		
		int count = blockEntity.countItems();
		ItemStack icon = blockEntity.findFirstNonemptyStack();
		
		matrices.pushPose();
		matrices.translate(0.5, 0.5, 0.5);
		applyRotation(matrices, packageTwelveDir);
		
		//draw the item on the front
		if(count > 0) {
			drawItem(matrices, vertexConsumers, icon, light);
		}
		
		//See if we need to show text.
		boolean showText = false, showDetailedText = false;
		
		if(client.getCameraEntity() == null) return;
		HitResult ray = client.getCameraEntity().pick(8, 0, false);
		
		if(ray.getType() == HitResult.Type.BLOCK && blockEntity.getBlockPos().equals(((BlockHitResult) ray).getBlockPos())) {
			showText = true;
		}
		
		double distance = client.getCameraEntity().getEyePosition(1).distanceTo(Vec3.atCenterOf(blockEntity.getBlockPos()));
		
		//This isn't a perfectly accurate distance estimator, but works pretty well
		//The intention is to grey out the text a bit when you're too far away to actually click
		@SuppressWarnings("ConstantConditions")
		boolean aBitFar = distance - 0.5 >= client.gameMode.getPickRange();
		
		if(client.getCameraEntity().isShiftKeyDown()) {
			if(showText) showDetailedText = true;
			
			if(!showText && distance <= 8) {
				showText = true;
			}
		}
		
		if(showText) {
			String text;
			int max = PackageBlockEntity.maxStackAmountAllowed(icon);
			
			if(showDetailedText) {
				int stacks = count / max;
				int leftover = count % max;
				text = stacks + "x" + max + " + " + leftover;
			} else {
				text = String.valueOf(count);
			}
			
			boolean completelyFull = max * PackageBlockEntity.SLOT_COUNT == count;
			int color = completelyFull ? 0x00FF6600 : 0x00FFFFFF;
			color |= aBitFar ? 0x55000000 : 0xFF000000;
			
			float scale;
			if(showDetailedText) scale = 1/70f;
			else if(count < 10) scale = 1/15f;
			else if(count < 100) scale = 1/23f;
			else scale = 1/30f;
			
			matrices.pushPose();
			
			matrices.translate(6 / 16d + 0.05, 0, 0);
			matrices.scale(-1, -scale, scale);
			matrices.translate(0, -4, 0);
			//todo figure out what that normal call does in the original
			matrices.mulPose(Vector3f.YP.rotationDegrees(90));
			
			int minusHalfWidth = -textRenderer.width(text) / 2;
			textRenderer.drawInBatch(text, minusHalfWidth + 1, 1, (color & 0xFCFCFC) >> 2, false, matrices.last().pose(), vertexConsumers, false, 0, light);
			matrices.translate(0, 0, -0.001);
			textRenderer.drawInBatch(text, minusHalfWidth, 0, color, false, matrices.last().pose(), vertexConsumers, false, 0, light);
			
			matrices.popPose();
		}
		
		matrices.popPose();
	}
	
	private static int depth = 0;
	
	public static void applyRotation(PoseStack matrices, TwelveDirection dir) {
		//Rotate into position.
		//This might be jank, just blindly copied from Worse Barrels really
		//Only move the model matrix not the normal matrix, to ensure items are lit uniformly
		Matrix4f modelMatrix = matrices.last().pose();
		
		if(dir.primaryDirection.get2DDataValue() == -1) { //up/down
			modelMatrix.multiply(Vector3f.YP.rotationDegrees(-dir.secondaryDirection.toYRot() + 90));
			modelMatrix.multiply(Vector3f.ZP.rotationDegrees(dir.primaryDirection == Direction.UP ? 90 : -90));
		} else {
			modelMatrix.multiply(Vector3f.YP.rotationDegrees(-dir.primaryDirection.toYRot() - 90));
		}
	}
	
	public static void drawItem(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack stack, int light) {
		Minecraft client = Minecraft.getInstance();
		
		matrices.pushPose();
		
		Matrix4f modelMatrix2 = matrices.last().pose();
		
		if(depth == 0) {
			modelMatrix2.multiply(Matrix4f.createTranslateMatrix(6 / 16f + 0.006f, 0, 0));
			modelMatrix2.multiply(Vector3f.YP.rotationDegrees(90));
			modelMatrix2.multiply(Matrix4f.createScaleMatrix(0.75f, 0.75f, 0.005f)); //it's flat fuck friday!!!!!
		} else {
			//Don't think about this too hard, just a workaround to slightly space out deeply-nested items.
			//If I don't do this, situations like packages-inside-packages-inside-packages start zfighting pretty hard.
			modelMatrix2.multiply(Matrix4f.createTranslateMatrix(6 / 16f + 0.07f, 0, 0)); //Lift it out more
			modelMatrix2.multiply(Vector3f.YP.rotationDegrees(90));
			modelMatrix2.multiply(Matrix4f.createScaleMatrix(0.75f, 0.75f, depth * 0.06f)); //Scale it down less (and even less, for further depths)
		}
		
		depth++;
		if(depth < 5) {
			client.getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GUI, light, OverlayTexture.NO_OVERLAY, matrices, vertexConsumers, 0);
		}
		depth--;
		
		matrices.popPose();
	}
}
