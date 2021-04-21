package QKDproject;
import javafx.scene.*;
import javafx.scene.control.Button;

/**
 * Window made for each user, allows them to see their many chats.
 * @author Marc
 */
public abstract class UserWindowController {
	/**
	 * Static factory. Loads javafx window and returns the controller. Currently
	 * loads a SimpleUserWindow.
	 * @param u User to make controller for.
	 * @return 
	 */
	public static UserWindowController create(User u) {
		return new QKDproject.test.SimpleUserWindow(u);
	}
	
	public abstract void addChat(Parent chatWindow, Button backButton, Chat c);
	
	public abstract User getUser();
}
