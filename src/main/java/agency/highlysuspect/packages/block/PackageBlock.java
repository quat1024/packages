package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.item.PackageItem;
import agency.highlysuspect.packages.junk.PackageStyle;
import agency.highlysuspect.packages.junk.TwelveDirection;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class PackageBlock extends Block implements BlockEntityProvider {
	public PackageBlock(Settings settings) {
		super(settings);
		
		setDefaultState(getDefaultState().with(FACING, TwelveDirection.NORTH));
	}
	
	public static final Identifier CONTENTS = new Identifier(Packages.MODID, "package_contents");
	 
	//States, materials, etc.
	public static final Property<TwelveDirection> FACING = EnumProperty.of("facing", TwelveDirection.class);
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		PlayerEntity placer = ctx.getPlayer();
		if(placer == null) return getDefaultState();
		else return getDefaultState().with(FACING, TwelveDirection.fromEntity(ctx.getPlayer()).getOpposite());
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder.add(FACING));
	}
	
	@Override
	public Material getMaterial(BlockState state) {
		//TODO make a state property for common materials of barrel
		//Does having the proper material even matter though, btw?
		return super.getMaterial(state);
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
		
		//Copy custom name
		if(stack.hasCustomName()) {
			pkg.setCustomName(stack.getName());
		}
		
		//This is not *strictly* needed because the sync will take care of it, but this prevents packages flickering the default style after placed.
		pkg.setStyle(PackageStyle.fromItemStack(stack));
		
		if(!world.isClient) pkg.sync();
	}
}
