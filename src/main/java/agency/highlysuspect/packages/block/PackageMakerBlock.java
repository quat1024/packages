package agency.highlysuspect.packages.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PackageMakerBlock extends Block implements EntityBlock {
	public PackageMakerBlock(Properties settings) {
		super(settings);
		
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
	}
	
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	
	public static final VoxelShape LEG1 = box(1, 0, 1, 3, 6, 3);
	public static final VoxelShape LEG2 = box(13, 0, 1, 15, 6, 3);
	public static final VoxelShape LEG3 = box(13, 0, 13, 15, 6, 15);
	public static final VoxelShape LEG4 = box(1, 0, 13, 3, 9, 15);
	public static final VoxelShape TRAY = box(1, 2, 1, 15, 3, 15);
	public static final VoxelShape TOP = box(1, 6, 1, 15, 9, 15);
	
	public static final VoxelShape ALL = Shapes.or(LEG1, LEG2, LEG3, LEG4, TRAY, TOP);
	public static final VoxelShape SIMPLE = box(1, 0, 1, 15, 9, 15);
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return PBlockEntityTypes.PACKAGE_MAKER.create(pos, state);
	}
	
	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
		if(world.getBlockEntity(pos) instanceof PackageMakerBlockEntity maker) {
			boolean wasPowered = state.getValue(POWERED);
			boolean isPowered = world.hasNeighborSignal(pos);
			if(wasPowered != isPowered) {
				world.setBlockAndUpdate(pos, state.setValue(POWERED, isPowered));
				if(isPowered) maker.performCraft();
			}
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(world.getBlockEntity(pos) instanceof PackageMakerBlockEntity maker) {
			player.openMenu(maker);
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if(stack.hasCustomHoverName() && world.getBlockEntity(pos) instanceof PackageMakerBlockEntity maker) {
			maker.setCustomName(stack.getHoverName());
		}
	}
	
	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}
	
	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		if(world.getBlockEntity(pos) instanceof PackageMakerBlockEntity maker) {
			int level = 0;
			if(maker.hasFrame()) level += 1;
			if(maker.hasInner()) level += 2;
			if(maker.hasDye()) level += 4;
			if(maker.hasOutput()) level += 8;
			return level;
		} else return 0;
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		if(builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof PackageMakerBlockEntity maker) {
			builder.withDynamicDrop(new ResourceLocation("minecraft", "contents"), (ctx, cons) -> {
				for(int i = 0; i < maker.getContainerSize(); i++) {
					cons.accept(maker.getItem(i));
				}
			});
		}
		
		return super.getDrops(state, builder);
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING, POWERED));
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
		return ALL;
	}
	
	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
		return SIMPLE;
	}
}
