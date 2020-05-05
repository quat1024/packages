package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.block.entity.PackageMakerBlockEntity;
import agency.highlysuspect.packages.container.PContainerTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class PackageMakerBlock extends Block implements BlockEntityProvider {
	public PackageMakerBlock(Settings settings) {
		super(settings);
		
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false));
	}
	
	public static final EnumProperty<Direction> FACING = Properties.HOPPER_FACING;
	public static final BooleanProperty POWERED = Properties.POWERED;
	
	@Override
	public BlockEntity createBlockEntity(BlockView view) {
		return new PackageMakerBlockEntity();
	}
	
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
		BlockEntity be = world.getBlockEntity(pos);
		if(be instanceof PackageMakerBlockEntity) {
			boolean wasPowered = state.get(POWERED);
			boolean isPowered = world.isReceivingRedstonePower(pos);
			if(wasPowered != isPowered) {
				world.setBlockState(pos, state.with(POWERED, isPowered));
				if(isPowered) ((PackageMakerBlockEntity) be).performCraft();
			}
		}
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if(!world.isClient) {
			PContainerTypes.openPackageMaker(player, pos);
		}
		
		return ActionResult.SUCCESS;
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if(stack.hasCustomName()) {
			BlockEntity be = world.getBlockEntity(pos);
			if(be instanceof PackageMakerBlockEntity) {
				((PackageMakerBlockEntity) be).setCustomName(stack.getName());
			}
		}
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder.add(FACING, POWERED));
	}
}
