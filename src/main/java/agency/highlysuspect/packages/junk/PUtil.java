package agency.highlysuspect.packages.junk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PUtil {
	//Yeah I know there's collections.singleton and immutablelist.of etc, but I want mutable ones!
	@SafeVarargs
	public static <T> ArrayList<T> arrayListOf(T... things) {
		ArrayList<T> list = new ArrayList<>();
		Collections.addAll(list, things);
		return list;
	}
	
	@SafeVarargs
	public static <T> ArrayList<T> concat(Collection<T> others, T... things) {
		ArrayList<T> list = new ArrayList<>(others);
		Collections.addAll(list, things);
		return list;
	}
	
	public static <T> ArrayList<T> concatCollections(Collection<T> a, Collection<T> b) {
		ArrayList<T> list = new ArrayList<>(a);
		list.addAll(b);
		return list;
	}
}
