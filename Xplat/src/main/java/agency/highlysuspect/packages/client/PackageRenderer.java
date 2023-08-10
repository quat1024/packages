package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.PackageBlockEntity;
import agency.highlysuspect.packages.junk.PackageContainer;
import agency.highlysuspect.packages.junk.TwelveDirection;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.EnumMap;

public class PackageRenderer implements BlockEntityRenderer<PackageBlockEntity> {
	public PackageRenderer(BlockEntityRendererProvider.Context context) {
		textRenderer = context.getFont();
	}
	
	private final Font textRenderer;
	private static final Quaternionf YP_90 = Axis.YP.rotationDegrees(90);
	
	@Override
	public void render(PackageBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		/// Setup
		Entity player = Minecraft.getInstance().getCameraEntity();
		if(blockEntity.getLevel() == null || player == null) return;
		
		Level world = blockEntity.getLevel();
		BlockState packageState = blockEntity.getBlockState();
		if(!(packageState.getBlock() instanceof PackageBlock)) return;
		TwelveDirection packageTwelveDir = packageState.getValue(PackageBlock.FACING);
		
		//The block is solid, so has no light inside; use the light of whatever's in front instead.
		//TODO: This seems to cause issues with Create contraptions, maybe have some fallback to the provided "light"
		light = LevelRenderer.getLightColor(world, blockEntity.getBlockPos().relative(packageTwelveDir.primaryDirection));
		
		/// Prepare
		matrices.pushPose();
		matrices.translate(0.5, 0.5, 0.5);
		applyRotation(matrices, packageTwelveDir);
		
		/// Item
		PackageContainer container = blockEntity.getContainer();
		ItemStack stack = container.getFilterStack();
		if(stack.isEmpty()) stack = blockEntity.getStickyStack();
		
		if(!stack.isEmpty()) drawItem(matrices, vertexConsumers, stack, light);
		
		/// Text
		int detailLevel = player.isShiftKeyDown() ? 1 : 0;
		double distanceSq = player.getEyePosition(1).distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos()));
		
		//First check if you're near the block and only then perform the raycast; raycasts are expensive, even with limited range.
		// (BLANKETCON) Important because Portal Cubed, for obvious reasons, seems to increase the cost of raycasts.
		if(distanceSq <= 64 &&
			player.pick(8, 0, false) instanceof BlockHitResult blockHit && 
			blockEntity.getBlockPos().equals(blockHit.getBlockPos()))
		{
			detailLevel++;
		}
		
		if(detailLevel > 0) drawText(matrices, vertexConsumers, light, container, detailLevel, Math.sqrt(distanceSq));
		
		matrices.popPose();
	}
	
	//Rotation that moves rendering to the face of the Package, oriented in the correct way. Or something like that.
	//The actual math dates from Worse Barrels, I don't remember how it works but it seems to work.
	private static final Quaternionf[] MAGIC_QUATS = new Quaternionf[TwelveDirection.values().length]; //wow i wonder how many directions are in TwelveDirection
	static {
		for(TwelveDirection dir : TwelveDirection.values()) {
			if(dir.primaryDirection.get2DDataValue() == -1) { //up/down
				Quaternionf magic1 = Axis.YP.rotationDegrees(-dir.secondaryDirection.toYRot() + 90);
				Quaternionf magic2 = Axis.ZP.rotationDegrees(dir.primaryDirection == Direction.UP ? 90 : -90);
				MAGIC_QUATS[dir.ordinal()] = magic1.mul(magic2);
			} else {
				MAGIC_QUATS[dir.ordinal()] = Axis.YP.rotationDegrees(-dir.primaryDirection.toYRot() - 90);
			}
		}
	}
	
	//static for MixinItemRenderer
	public static void applyRotation(PoseStack ps, TwelveDirection dir) {
		//We change the pose matrix only, and not the normal matrix, so that items appear lit from the same direction.
		ps.last().pose().rotate(MAGIC_QUATS[dir.ordinal()]);
	}
	
	//+1 because depth is increased before rendering the item
	//+1 again because RECURSION_LIMIT doesnt count the final item at the very bottom (i dont think)
	private static final int MAX_ITEM_RENDER_DEPTH = PackageContainer.RECURSION_LIMIT + 2;
	private static final Matrix4f[] ITEM_TRANSFORMATIONS = new Matrix4f[MAX_ITEM_RENDER_DEPTH];
	static {
		for(int depth = 0; depth < ITEM_TRANSFORMATIONS.length; depth++) {
			Matrix4f pose = new Matrix4f(); //identity matrix
			
			if(depth == 0) {
				pose.translate(6 / 16f + 0.006f, 0, 0);
				pose.rotate(YP_90);
				pose.scale(0.75f, 0.75f, 0.005f); //it's flat fuck friday!!!!!
			} else {
				//Don't think about this too hard, just a workaround to slightly space out deeply-nested items.
				//If I don't do this, situations like packages-inside-packages-inside-packages start zfighting pretty hard.
				pose.translate(6 / 16f + 0.07f, 0, 0); //Lift it out more
				pose.rotate(YP_90);
				pose.scale(0.75f, 0.75f, depth * 0.06f); //Scale it down less (and even less, for further depths)
			}
			
			ITEM_TRANSFORMATIONS[depth] = pose;
		}
	}
	
	private static int depth = 0; //TODO: ThreadLocal?
	//static for MixinItemRenderer
	public static void drawItem(PoseStack ps, MultiBufferSource bufs, ItemStack stack, int light) {
		ps.pushPose();
		
		//We change the pose matrix only, and not the normal matrix, so that items appear lit as if they weren't flattened.
		Matrix4f transformation = ITEM_TRANSFORMATIONS[Mth.clamp(depth, 0, MAX_ITEM_RENDER_DEPTH - 1)];
		ps.last().pose().mul(transformation);
		
		try {
			depth++;
			if(depth < MAX_ITEM_RENDER_DEPTH) {
				Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, light, OverlayTexture.NO_OVERLAY, ps, bufs, null, 0);
			}
		} finally {
			depth--;
			ps.popPose();
		}
	}
	
	private void drawText(PoseStack matrices, MultiBufferSource vertexConsumers, int light, PackageContainer container, int detailLevel, double distance) {
		if(Minecraft.getInstance().gameMode == null) return;
		
		int count = container.getCount();
		int max = container.maxStackAmountAllowed(container.getFilterStack());
		
		String text;
		if(detailLevel == 2) {
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
		if(detailLevel == 2 && max == 1) scale = 1/30f;
		else if(detailLevel == 2) scale = 1/70f;
		else if(count < 10) scale = 1/15f;
		else if(count < 100) scale = 1/23f;
		else scale = 1/30f;
		
		matrices.pushPose();
		
		matrices.translate(6 / 16d + 0.05, 0, 0);
		matrices.scale(-1, -scale, scale);
		matrices.translate(0, -4, 0);
		matrices.mulPose(YP_90);
		
		int minusHalfWidth = -textRenderer.width(text) / 2;
		textRenderer.drawInBatch(text, minusHalfWidth + 1, 1, shadowColor, false, matrices.last().pose(), vertexConsumers, Font.DisplayMode.NORMAL, 0, light); //Background
		matrices.translate(0, 0, -0.001);
		textRenderer.drawInBatch(text, minusHalfWidth,     0, color      , false, matrices.last().pose(), vertexConsumers, Font.DisplayMode.NORMAL, 0, light); //Foreground
		
		matrices.popPose();
	}
}
