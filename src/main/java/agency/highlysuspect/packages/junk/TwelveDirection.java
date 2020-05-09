package agency.highlysuspect.packages.junk;

import net.minecraft.entity.Entity;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

import java.util.EnumMap;
import java.util.Locale;

//Cut and paste this from Carved Melons without looking too closely at it lmao
public enum TwelveDirection implements StringIdentifiable {
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
		public String asString() {
			return name().toLowerCase(Locale.ROOT);
		}
		
		public TwelveDirection withSecondary(Direction sec) {
			switch(primaryDirection) {
				case UP: return ups.get(sec);
				case DOWN: return downs.get(sec);
				default: return this;
			}
		}
		
		public static TwelveDirection fromEntity(Entity ent) {
			Direction d = Direction.getEntityFacingOrder(ent)[0];
			TwelveDirection td = byPrimary.get(d);
			
			if(d.getAxis() != Direction.Axis.Y) {
				return td;
			} else {
				return td.withSecondary(ent.getHorizontalFacing().getOpposite());
			}
		}
		
		public static TwelveDirection fromDirection(Direction dir) {
			return byPrimary.get(dir);
		}
		
		public TwelveDirection getOpposite() {
			switch(this) {
				case UP_NORTH: return DOWN_SOUTH;
				case UP_EAST: return DOWN_WEST;
				case UP_SOUTH: return DOWN_NORTH;
				case UP_WEST: return DOWN_EAST;
				case NORTH: return SOUTH;
				case EAST: return WEST;
				case SOUTH: return NORTH;
				case WEST: return EAST;
				case DOWN_NORTH: return UP_SOUTH;
				case DOWN_EAST: return UP_WEST;
				case DOWN_SOUTH: return UP_NORTH;
				case DOWN_WEST: return UP_EAST;
				default: throw new RuntimeException("Impossible");
			}
		}
	}
