package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.item.PackageItem;
import agency.highlysuspect.packages.junk.PackageStyle;
import agency.highlysuspect.packages.junk.TwelveDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class PackageBlock extends Block implements BlockEntityProvider {
	public PackageBlock(Settings settings) {
		super(settings);
		
		setDefaultState(getDefaultState().with(FACING, TwelveDirection.NORTH));
	}
	
	//States, materials, etc.
	public static final Property<TwelveDirection> FACING = EnumProperty.of("facing", TwelveDirection.class);
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		TwelveDirection facing;
		
		if(ctx.getPlayer() == null) facing = TwelveDirection.fromDirection(ctx.getSide());
		else facing = TwelveDirection.fromEntity(ctx.getPlayer()).getOpposite();
		
		return getDefaultState().with(FACING, facing);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder.add(FACING));
	}
	
	//Block entities.
	@Override
	public BlockEntity createBlockEntity(BlockView view) {
		return new PackageBlockEntity();
	}
	
	//Behaviors.
	@Override
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.DESTROY;
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if(!(blockEntity instanceof PackageBlockEntity)) return;
		PackageBlockEntity pkg = (PackageBlockEntity) blockEntity;
		
		if(stack.hasCustomName()) {
			pkg.setCustomName(stack.getName());
		}
		
		if(world.isClient) {
			//This is not *strictly* needed because the sync will take care of it, but this prevents packages flickering the default style after placed.
			//Client knows the tag, might as well make use of the information
			pkg.setStyle(PackageStyle.fromItemStack(stack));
		} else {
			pkg.sync();
		}
	}
	
	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
	}
	
	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if(!world.isClient && player.isCreative()) {
			getDroppedStacks(state, (ServerWorld) world, pos, world.getBlockEntity(pos)).forEach(s -> {
				ItemEntity ent = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, s);
				ent.setToDefaultPickupDelay();
				ent.setVelocity(0, 0, 0);
				world.spawnEntity(ent);
			});
		}
	}
	
	@Override
	public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if(state.getBlock() != newState.getBlock()) {
			world.updateComparators(pos, this);
		}
		
		super.onBlockRemoved(state, world, pos, newState, moved);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		ItemStack stack = super.getPickStack(world, pos, state);
		PackageBlockEntity be = (PackageBlockEntity)world.getBlockEntity(pos);
		if(be == null) return stack;
		
		//just copy the style not the contents (like shulker boxes)
		CompoundTag tag = new CompoundTag();
		tag.put(PackageStyle.KEY, ((PackageStyle) be.getRenderAttachmentData()).toTag());
		stack.putSubTag("BlockEntityTag", tag);
		
		PackageItem.addFakeContentsTagThisSucks(stack);
		
		return stack;
	}
}
