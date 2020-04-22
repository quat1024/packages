package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.block.PackageBlock;
import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.junk.TwelveDirection;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.ItemRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;

import java.util.Random;

public class PackageBlockEntityRenderer extends BlockEntityRenderer<PackageBlockEntity> {
	public PackageBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}
	
	@Override
	public void render(PackageBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if(blockEntity == null || blockEntity.getWorld() == null) return;
		
		//Gather some data
		MinecraftClient client = MinecraftClient.getInstance();
		BlockState packageState = blockEntity.getCachedState();
		if(!(packageState.getBlock() instanceof PackageBlock)) return;
		TwelveDirection packageTwelveDir = packageState.get(PackageBlock.FACING);
		Direction packageDir = packageTwelveDir.primaryDirection;
		
		int count = blockEntity.countItems();
		ItemStack icon = blockEntity.findFirstNonemptyStack();
		
		//Use the light level of whatever's in front
		//Quick fix for my block being solid so it has no light insside...
		light = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().offset(packageDir));
		
		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		
		//Rotate into position.
		//This might be jank, just blindly copied from Worse Barrels really
		if(packageDir.getHorizontal() == -1) { //up/down
			matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-packageTwelveDir.secondaryDirection.asRotation() - 90));
			matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(packageDir == Direction.UP ? 90 : -90));
		} else {
			matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-packageDir.asRotation() - 90));
		}
		
		//Draw the item on the front.
		if(count > 0) {
			matrices.push();
			
			matrices.translate(6 / 16d + 0.001, 0, 0); //just outside the front of the package
			matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90)); //Just keep rotating until it looks right ayy lmao
			//Just scale the world matrix, not the normals
			//This is the magic sauce that makes item lighting work
			matrices.peek().getModel().multiply(Matrix4f.scale(0.75f, 0.75f, 0.001f));
			
			ItemRenderer renderer = client.getItemRenderer();
			
			//DiffuseLighting.enableGuiDepthLighting();
			renderer.renderItem(icon, ModelTransformation.Mode.GUI, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers);
			//DiffuseLighting.disableGuiDepthLighting();
			
			matrices.pop();
		}
		
		//See if we need to show text.
		boolean showText = false, showDetailedText = false;
		
		if(client.getCameraEntity() == null) return;
		HitResult ray = client.getCameraEntity().rayTrace(7, 0, false);
		if(ray.getType() == HitResult.Type.BLOCK && blockEntity.getPos().equals(((BlockHitResult) ray).getBlockPos())) {
			showText = true;
		}
		
		if(client.getCameraEntity().isSneaking()) {
			if(showText) showDetailedText = true;
			if(client.getCameraEntity().getPos().squaredDistanceTo(
				blockEntity.getPos().getX(),
				blockEntity.getPos().getY(),
				blockEntity.getPos().getZ()
			) < 5 * 5) {
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
			int color = completelyFull ? 0xFFFF6600 : 0xFFFFFFFF;
			
			float scale;
			if(showDetailedText) scale = 1/70f;
			else if(count < 10) scale = 1/15f;
			else if(count < 100) scale = 1/23f;
			else scale = 1/30f;
			
			matrices.push();
			
			TextRenderer textRenderer = client.textRenderer;
			
			matrices.translate(6 / 16d + 0.005, 0, 0);
			matrices.scale(-1, -scale, scale);
			matrices.translate(0, -4, 0);
			//todo figure out what that normal call does in the original
			matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
			
			int minusHalfWidth = -textRenderer.getStringWidth(text) / 2;
			textRenderer.draw(text, minusHalfWidth + 1, 1, (color & 0xFCFCFC) >> 2, false, matrices.peek().getModel(), vertexConsumers, false, 0, light);
			matrices.translate(0, 0, -0.001);
			textRenderer.draw(text, minusHalfWidth, 0, color, false, matrices.peek().getModel(), vertexConsumers, false, 0, light);
			
			matrices.pop();
		}
		
		matrices.pop();
	}
}
