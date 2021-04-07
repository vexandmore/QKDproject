package QKDproject;

import java.util.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.*;

/**
 * Represents a user.
 * @author Marc
 */
public class User {
	private String name;
	private List<Chat> chatWindows = new ArrayList<>();
	
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
			return this.name.equals(otherUser.name) && this.chatWindows.equals(otherUser.chatWindows);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
