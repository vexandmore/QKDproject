package QKDproject.test;

import QKDproject.*;
import javafx.event.ActionEvent;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

/**
 *
 * @author Marc
 */
public class SimpleUserWindow extends UserWindowController {
	private VBox vbox;
	private Scene mainScene;
	private Stage mainStage;
	private User u1;
	
	public static UserWindowController create(User u) {
		SimpleUserWindow suw = new SimpleUserWindow();
		suw.vbox = new VBox();
		suw.vbox.setPrefSize(300, 300);
		suw.mainStage = new Stage();
		suw.mainStage.setTitle(u.getName() + "'s chats");
		suw.mainScene = new Scene(suw.vbox);
		suw.mainStage.setScene(suw.mainScene);
		suw.mainStage.show();

		suw.setUser(u);
		suw.u1 = u;
		return suw;
	}
	
	private void setUser(User u) {
		vbox.getChildren().add(new Label("This is: " + u.getName()));
	}

	@Override
	public void addChat(Parent chatWindow, Button backButton, Chat c) {
		Button goToChat = new Button("chat with " + c.getUser2());
		Scene chatScene = new Scene(chatWindow);
		goToChat.setOnAction((ActionEvent ae) -> {
			mainStage.setScene(chatScene);
		});
		vbox.getChildren().add(goToChat);
		backButton.setOnAction((ActionEvent ae) -> {
			mainStage.setScene(mainScene);
		});
	}

	@Override
	public User getUser() {
		return u1;
	}
}
