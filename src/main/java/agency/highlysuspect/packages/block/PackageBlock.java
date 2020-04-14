package agency.highlysuspect.packages.block;

import agency.highlysuspect.packages.block.entity.PackageBlockEntity;
import agency.highlysuspect.packages.junk.TwelveDirection;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.world.BlockView;

public class PackageBlock extends Block implements BlockEntityProvider {
	public PackageBlock(Settings settings) {
		super(settings);
		
		setDefaultState(getDefaultState().with(FACING, TwelveDirection.NORTH));
	}
	 
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
}
