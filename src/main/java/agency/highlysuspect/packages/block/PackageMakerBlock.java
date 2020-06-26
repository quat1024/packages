package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.block.entity.PackageMakerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;

public class PackageMakerBlock extends Block implements BlockEntityProvider {
	public PackageMakerBlock(Settings settings) {
		super(settings);
		
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false));
	}
	
	public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty POWERED = Properties.POWERED;
	
	public static final VoxelShape LEG1 = createCuboidShape(1, 0, 1, 3, 6, 3);
	public static final VoxelShape LEG2 = createCuboidShape(13, 0, 1, 15, 6, 3);
	public static final VoxelShape LEG3 = createCuboidShape(13, 0, 13, 15, 6, 15);
	public static final VoxelShape LEG4 = createCuboidShape(1, 0, 13, 3, 9, 15);
	public static final VoxelShape TRAY = createCuboidShape(1, 2, 1, 15, 3, 15);
	public static final VoxelShape TOP = createCuboidShape(1, 6, 1, 15, 9, 15);
	
	public static final VoxelShape ALL = VoxelShapes.union(LEG1, LEG2, LEG3, LEG4, TRAY, TOP);
	public static final VoxelShape SIMPLE = createCuboidShape(1, 0, 1, 15, 9, 15);
	
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
		BlockEntity be = world.getBlockEntity(pos);
		if(be instanceof PackageMakerBlockEntity) {
			player.openHandledScreen((PackageMakerBlockEntity) be);
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
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		BlockEntity be = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
		if(be instanceof PackageMakerBlockEntity) {
			PackageMakerBlockEntity pkgMaker = (PackageMakerBlockEntity) be;
			builder.putDrop(new Identifier("minecraft", "contents"), (ctx, cons) -> {
				for(int i = 0; i < pkgMaker.size(); i++) {
					cons.accept(pkgMaker.getStack(i));
				}
			});
		}
		
		return super.getDroppedStacks(state, builder);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder.add(FACING, POWERED));
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		return ALL;
	}
	
	@Override
	public VoxelShape getRayTraceShape(BlockState state, BlockView view, BlockPos pos) {
		return SIMPLE;
	}
}
