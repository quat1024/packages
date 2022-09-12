package agency.highlysuspect.packages.junk;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collection;

public class PUtil {
	public static final Direction[] DIRECTIONS_AND_NULL = new Direction[]{
		Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, null
	};
	
	@SafeVarargs
	public static <T> ArrayList<T> concat(Collection<T>... collections) {
		ArrayList<T> list = new ArrayList<>();
		
		for(Collection<T> c : collections) {
			list.addAll(c);
		}
		
		return list;
	}
	
	//my favorite method in the whole wide world
	public static float rangeRemap(float value, float low1, float high1, float low2, float high2) {
		float value2 = Mth.clamp(value, low1, high1);
		return low2 + (value2 - low1) * (high2 - low2) / (high1 - low1);
	}
}
