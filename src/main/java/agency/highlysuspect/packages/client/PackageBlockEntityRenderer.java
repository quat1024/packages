package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.junk.TwelveDirection;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

public class PackageBlockEntityRenderer implements BlockEntityRenderer<PackageBlockEntity> {
	public PackageBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		textRenderer = context.getTextRenderer();
	}
	
	private final TextRenderer textRenderer;
	
	@Override
	public void render(PackageBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if(blockEntity == null || blockEntity.getWorld() == null) return;
		
		//Gather some data
		World world = blockEntity.getWorld();
		MinecraftClient client = MinecraftClient.getInstance();
		
		BlockState packageState = blockEntity.getCachedState();
		if(!(packageState.getBlock() instanceof PackageBlock)) return;
		
		TwelveDirection packageTwelveDir = packageState.get(PackageBlock.FACING);
		
		//get the light level of whatever's in front
		//Quick fix for my block being solid so it has no light inside...
		light = WorldRenderer.getLightmapCoordinates(world, blockEntity.getPos().offset(packageTwelveDir.primaryDirection));
		
		int count = blockEntity.countItems();
		ItemStack icon = blockEntity.findFirstNonemptyStack();
		
		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		applyRotation(matrices, packageTwelveDir);
		
		//draw the item on the front
		if(count > 0) {
			drawItem(matrices, vertexConsumers, icon, light);
		}
		
		//See if we need to show text.
		boolean showText = false, showDetailedText = false;
		
		if(client.getCameraEntity() == null) return;
		HitResult ray = client.getCameraEntity().raycast(8, 0, false);
		
		if(ray.getType() == HitResult.Type.BLOCK && blockEntity.getPos().equals(((BlockHitResult) ray).getBlockPos())) {
			showText = true;
		}
		
		double distance = client.getCameraEntity().getCameraPosVec(1).distanceTo(Vec3d.ofCenter(blockEntity.getPos()));
		
		//This isn't a perfectly accurate distance estimator, but works pretty well
		//The intention is to grey out the text a bit when you're too far away to actually click
		@SuppressWarnings("ConstantConditions")
		boolean aBitFar = distance - 0.5 >= client.interactionManager.getReachDistance();
		
		if(client.getCameraEntity().isSneaking()) {
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
			
			matrices.push();
			
			matrices.translate(6 / 16d + 0.05, 0, 0);
			matrices.scale(-1, -scale, scale);
			matrices.translate(0, -4, 0);
			//todo figure out what that normal call does in the original
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
			
			int minusHalfWidth = -textRenderer.getWidth(text) / 2;
			textRenderer.draw(text, minusHalfWidth + 1, 1, (color & 0xFCFCFC) >> 2, false, matrices.peek().getModel(), vertexConsumers, false, 0, light);
			matrices.translate(0, 0, -0.001);
			textRenderer.draw(text, minusHalfWidth, 0, color, false, matrices.peek().getModel(), vertexConsumers, false, 0, light);
			
			matrices.pop();
		}
		
		matrices.pop();
	}
	
	private static int depth = 0;
	
	public static void applyRotation(MatrixStack matrices, TwelveDirection dir) {
		//Rotate into position.
		//This might be jank, just blindly copied from Worse Barrels really
		//Only move the model matrix not the normal matrix, to ensure items are lit uniformly
		Matrix4f modelMatrix = matrices.peek().getModel();
		
		if(dir.primaryDirection.getHorizontal() == -1) { //up/down
			modelMatrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-dir.secondaryDirection.asRotation() + 90));
			modelMatrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(dir.primaryDirection == Direction.UP ? 90 : -90));
		} else {
			modelMatrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-dir.primaryDirection.asRotation() - 90));
		}
	}
	
	public static void drawItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, int light) {
		MinecraftClient client = MinecraftClient.getInstance();
		
		matrices.push();
		
		Matrix4f modelMatrix2 = matrices.peek().getModel();
		
		if(depth == 0) {
			modelMatrix2.multiply(Matrix4f.translate(6 / 16f + 0.006f, 0, 0));
			modelMatrix2.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
			modelMatrix2.multiply(Matrix4f.scale(0.75f, 0.75f, 0.005f)); //it's flat fuck friday!!!!!
		} else {
			//Don't think about this too hard, just a workaround to slightly space out deeply-nested items.
			//If I don't do this, situations like packages-inside-packages-inside-packages start zfighting pretty hard.
			modelMatrix2.multiply(Matrix4f.translate(6 / 16f + 0.07f, 0, 0)); //Lift it out more
			modelMatrix2.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
			modelMatrix2.multiply(Matrix4f.scale(0.75f, 0.75f, depth * 0.06f)); //Scale it down less (and even less, for further depths)
		}
		
		depth++;
		if(depth < 5) {
			client.getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);
		}
		depth--;
		
		matrices.pop();
	}
}
