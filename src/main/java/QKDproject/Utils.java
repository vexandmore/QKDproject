package QKDproject;

import java.util.*;

/**
 * Provides utility methods for QKD.
 * @author Marc
 */
public class Utils {


	/**
	 * Creates a list of indices that should be compared so that the number
	 * of bits that are compared in a string is approximately equivalent to the
	 * ratio parameter, in percent.
	 * @param ratio Percentage of indices that will be in the compared bits.
	 * @param length Length of strings that will be compared.
	 * @return
	 */
	public static List<Integer> sampleIndices(int ratio, int length) {
		if (ratio == 0) {
			return Arrays.asList();
		}
		double r = 100.0 / ratio; //figure out every how many bits of the string should be in the sample
		if (r < 2) {
			r = 2; //prevent the sample from containing every bit
		}
		int everyN = (int) r;
		List<Integer> out = new ArrayList<>();
		for (int i = 0; i < length; i += everyN) {
			out.add(i);
		}
		return out;
	}


	
	/**
	 * Returns true if the list is in nondecreasing (aka weakly increasing)
	 * order according to their natural ordering. Empty lists and lists of 1 
	 * item are considered sorted.
	 * @param <T> The type of the list. It must be possible to mutually compare
	 * all objects in the list.
	 * @param ls The list to check.
	 * @return Whether the list is sorted.
	 */
	public static <T extends Comparable<? super T>> boolean isSorted(List<T> ls) {
		if (ls.size() < 2)
			return true;
		
		Iterator<T> it = ls.iterator();
		T previous = it.next();
		while (it.hasNext()) {
			T current = it.next();
			if (previous.compareTo(current) > 0) 
				return false;
			previous = current;
		}
		return true;
	}
	
}
