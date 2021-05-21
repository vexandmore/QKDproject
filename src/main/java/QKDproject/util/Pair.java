/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject.util;

import java.util.Objects;

/**
 * Represents an unordered pair. Cannot contain null elements.
 */
public final class Pair<T> {
	public final T u1;
	public final T u2;

	public Pair(T u1, T u2) {
		this.u1 = Objects.requireNonNull(u1);
		this.u2 = Objects.requireNonNull(u2);
	}

	@Override
	public int hashCode() {
		return u1.hashCode() + u2.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Pair)) {
			return false;
		}
		final Pair other = (Pair) obj;
		return u1.equals(other.u1) && u2.equals(other.u2) || u1.equals(other.u2) && u2.equals(other.u1);
	}
	
}
