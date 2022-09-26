package agency.highlysuspect.packages.junk;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;

import java.util.EnumMap;
import java.util.Locale;

//Cut and paste this from Carved Melons without looking too closely at it lmao
public enum TwelveDirection implements StringRepresentable {
	UP_NORTH(Direction.UP, Direction.NORTH),
	UP_SOUTH(Direction.UP, Direction.SOUTH),
	UP_EAST(Direction.UP, Direction.EAST),
	UP_WEST(Direction.UP, Direction.WEST),
	NORTH(Direction.NORTH, null),
	SOUTH(Direction.SOUTH, null),
	EAST(Direction.EAST, null),
	WEST(Direction.WEST, null),
	DOWN_NORTH(Direction.DOWN, Direction.NORTH),
	DOWN_SOUTH(Direction.DOWN, Direction.SOUTH),
	DOWN_EAST(Direction.DOWN, Direction.EAST),
	DOWN_WEST(Direction.DOWN, Direction.WEST);
	
	TwelveDirection(Direction primaryDirection, Direction secondaryDirection) {
		this.primaryDirection = primaryDirection;
		this.secondaryDirection = secondaryDirection;
	}
	
	public final Direction primaryDirection;
	public final Direction secondaryDirection;
	
	public static final EnumMap<Direction, TwelveDirection> byPrimary = new EnumMap<>(Direction.class);
	public static final EnumMap<Direction, TwelveDirection> ups = new EnumMap<>(Direction.class);
	public static final EnumMap<Direction, TwelveDirection> downs = new EnumMap<>(Direction.class);
	
	static {
		byPrimary.put(Direction.UP, UP_NORTH);
		byPrimary.put(Direction.NORTH, NORTH);
		byPrimary.put(Direction.SOUTH, SOUTH);
		byPrimary.put(Direction.EAST, EAST);
		byPrimary.put(Direction.WEST, WEST);
		byPrimary.put(Direction.DOWN, DOWN_NORTH);
		
		ups.put(Direction.NORTH, UP_NORTH);
		ups.put(Direction.SOUTH, UP_SOUTH);
		ups.put(Direction.EAST, UP_EAST);
		ups.put(Direction.WEST, UP_WEST);
		
		downs.put(Direction.NORTH, DOWN_NORTH);
		downs.put(Direction.SOUTH, DOWN_SOUTH);
		downs.put(Direction.EAST, DOWN_EAST);
		downs.put(Direction.WEST, DOWN_WEST);
	}
	
	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public TwelveDirection withSecondary(Direction sec) {
		return switch(primaryDirection) {
			case UP -> ups.get(sec);
			case DOWN -> downs.get(sec);
			default -> this;
		};
	}
	
	public static TwelveDirection fromEntity(Entity ent) {
		Direction d = Direction.orderedByNearest(ent)[0];
		TwelveDirection td = byPrimary.get(d);
		
		if(d.getAxis() != Direction.Axis.Y) {
			return td;
		} else {
			return td.withSecondary(ent.getDirection().getOpposite());
		}
	}
	
	public static TwelveDirection fromDirection(Direction dir) {
		return byPrimary.get(dir);
	}
	
	public TwelveDirection getOpposite() {
		return switch(this) {
			case UP_NORTH -> DOWN_SOUTH;
			case UP_EAST -> DOWN_WEST;
			case UP_SOUTH -> DOWN_NORTH;
			case UP_WEST -> DOWN_EAST;
			case NORTH -> SOUTH;
			case EAST -> WEST;
			case SOUTH -> NORTH;
			case WEST -> EAST;
			case DOWN_NORTH -> UP_SOUTH;
			case DOWN_EAST -> UP_WEST;
			case DOWN_SOUTH -> UP_NORTH;
			case DOWN_WEST -> UP_EAST;
		};
	}
}
