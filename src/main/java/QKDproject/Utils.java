package QKDproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides utility methods for QKD.
 * @author Marc
 */
public class Utils {

	/**
	 * Returns a new String consisting of the input string where only characters
	 * at the given indices have been kept. Characters are added in the order
	 * they are in the list
	 * @param indices List containing the indices to keep
	 * @param str Input string
	 * @return String with chars at the given indices
	 */
	public static String keepAtIndices(List<Integer> indices, String str) {
		if (indices.size() > str.length()) {
			throw new IllegalArgumentException("index list must not be longer" + "than string");
		}
		if (indices.isEmpty()) {
			return "";
		}
		StringBuilder out = new StringBuilder();
		indices.forEach(i -> out.append(str.charAt(i)));
		return out.toString();
	}

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
	 * Returns the indices where the Strings have identical characters. Strings
	 * must be of the same length.
	 * @param a First String
	 * @param b Second string
	 * @return List containing indices where the Strings match
	 */
	public static List<Integer> matchingIndices(String a, String b) {
		if (a.length() != b.length()) {
			throw new IllegalArgumentException("Strings must be same length");
		}
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < a.length(); i++) {
			if (a.charAt(i) == b.charAt(i)) {
				indices.add(i);
			}
		}
		return indices;
	}

	/**
	 * Returns a new String consisting of the input string where only characters
	 * at the given indices have been removed. List must be sorted.
	 * @param indices List containing the indices to keep
	 * @param str Input string
	 * @return String with chars at the given indices
	 */
	public static String removeAtIndices(List<Integer> indices, String str) {
		if (indices.size() > str.length()) {
			throw new IllegalArgumentException("index list must not be longer" + "than string");
		}
		if (indices.isEmpty()) {
			return str;
		}
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			if (Collections.binarySearch(indices, i) < 0) {
				out.append(str.charAt(i)); //append to the string if the index isn't in list
			}
		}
		return out.toString();
	}
	
}
