
package QKDproject.test;

import QKDproject.*;
import javafx.event.ActionEvent;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

/**
 *
 * @author Marc and Raphael
 */
public class SimpleUserWindow{
	private VBox vbox;
	private Scene mainScene;
	private Stage mainStage;
	private User u1;
	
	public SimpleUserWindow(User u) {
		this.u1 = u;
		this.vbox = new VBox();
		vbox.setPrefSize(300, 300);
		vbox.getChildren().add(new Label("This is: " + u1.getName()));
		
		mainStage = new Stage();
		mainStage.setTitle(u1.getName() + "'s chats");
		mainScene = new Scene(vbox);
		mainStage.setScene(mainScene);
		mainStage.show();
	}
        
        public void openUserWindow() {
                mainStage.show();
        }
        
        
	//@Override
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
        
	//@Override
	public User getUser() {
		return u1;
	}
}

