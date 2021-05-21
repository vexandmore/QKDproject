package QKDproject;

import java.util.*;

/**
 * Represents a sort of substring of a String, where instead of simply being
 * able to choose a start and end index, any arbitrary selection of indices
 * from the String can be chosen. Immutable by design and is immutable if and
 * only if the CharSequence passed to the second constructor is not modified.
 * @author Marc
 */
public final class MeasurementSet implements CharSequence {
	private final CharSequence parent;
	private final List<Integer> indices;
	
	/**
	 * Create a MeasurementSet from the given String.
	 * @param root String that this MeasurementSet will use.
	 */
	public MeasurementSet(String root) {
		this.parent = root;
		indices = new ArrayList<>();
		for (int i = 0; i < root.length(); i++) {
			indices.add(i);
		}
	}
	
	/**
	 * Creates a MeasurementSet from the given MeasurementSet and indices.
	 * @param parent CharSequence this is based on. Should not be modified in
	 * the lifetime of this object.
	 * @param indices Indices from the parent that will comprise this 
	 * MeasurementSet. Is copied.
	 */
	public MeasurementSet(CharSequence parent, List<Integer> indices) {
		this.parent = parent;
		this.indices = new ArrayList<>(indices);
	}
	
	/**
	 * Make a MeasurementSet that is a substring of this from the given indices.
	 * @param indices Indices from the parent that will comprise the new 
	 * MeasurementSet. Is copied.
	 * @return 
	 */
	public MeasurementSet makeSubstring(Integer... indices) {
		return makeSubstring(Arrays.asList(indices));
	}
	
	/**
	 * Make a MeasurementSet that is a substring of this from the given indices.
	 * @param indices Indices from the parent that will comprise the new 
	 * MeasurementSet. Is copied.
	 * @return 
	 */
	public MeasurementSet makeSubstring(List<Integer> indices) {
		return new MeasurementSet(this, indices);
	}
	
	/**
	 * Makes the complement of this (the MeasurementSet with the opposite set 
	 * of indices) with respect to its immediate parent.
	 * @return Complement
	 */
	public MeasurementSet complement() {
		return new MeasurementSet(parent, complementOf(indices, parent.length() - 1));
	}
	
	/**
	 * Return the indices of this with respect to the root (the MeasurementSet
	 * which was constructed with a String).
	 * @return Unmodifiable list of indices of this with respect to the root 
	 * String
	 */
	public List<Integer> indicesRoot() {
		if (parent instanceof MeasurementSet) {
			MeasurementSet mParent = (MeasurementSet) parent;
			List<Integer> relParent = mParent.indicesRoot();
			
			List<Integer> out = new ArrayList<>();
			for (int i: indices) {
				out.add(relParent.get(i));
			}
			return out;
		} else {
			return Collections.unmodifiableList(indices);
		}
	}
	
	@Override
	public int length() {
		return indices.size();
	}

	@Override
	public char charAt(int index) {
		return parent.charAt(indices.get(index));
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		return this.toString().subSequence(start, end);
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int i: indices) {
			s.append(parent.charAt(i));
		}
		return s.toString();
	}
	
	/**
	 * Returns if the other object is equal to this one. Returns true if other 
	 * is a MeasurementSet and has the same parent and indices.
	 * @param other Object to compare against.
	 * @return True if the other object is equal to this.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other instanceof MeasurementSet) {
			MeasurementSet ot = (MeasurementSet) other;
			return this.parent.equals(ot.parent) && this.indices.equals(ot.indices);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	/**
	 * Returns the complement of the given list with respect to the list of
	 * integers from [0,max] inclusive.
	 * TODO: improve this from n^2 to n.
	 * @param list List whose complement will be taken
	 * @param max Max value for the complement
	 * @return Complement of the list
	 */
	private static List<Integer> complementOf(List<Integer> list, int max) {
		List<Integer> out = new ArrayList<>();
		ListIterator<Integer> iterator = list.listIterator();
		int nextIndexToAdd = 0;
		while (iterator.hasNext()) {
			int cannotHave = iterator.next();
			for (int i = nextIndexToAdd; i < cannotHave; i++) {
				out.add(i);
			}
			nextIndexToAdd = cannotHave + 1;
		}
		for (int i = nextIndexToAdd; i <= max; i++) {
			out.add(i);
		}
		return out;
	}
}
