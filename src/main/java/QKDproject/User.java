package QKDproject;

import java.util.Objects;

/**
 * Represents a user.
 * @author Marc
 */
public class User {
	private String name;
	
	public User(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other == null)
			return false;
		if (other instanceof User) {
			User otherUser = (User) other;
			return this.name.equals(otherUser.name);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
