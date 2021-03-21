/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
/**
 *
 * @author Marc
 */
public class Main extends Application {
	
	@Override // Override the start method in the Application class
  public void start(Stage stage) throws Exception {
    //create protocols
	QKDAlice p1 = new QKDAlice(false, 50);
	QKDBob p2 = new QKDBob();
	p1.connect(p2);
	//make comm channel and users
	CommunicationChannel channel = new CommunicationChannel();
	User user1 = new User("Marc");
	User user2 = new User("Test");
	//Load guis and make Chat instances
	FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatMockup.fxml"));
	Parent root = loader.load();
	ChatController controller1 = loader.getController();
	Scene scene = new Scene(root);
    stage.setTitle("Chat window 1");
    stage.setScene(scene);
    stage.show();
	//load gui 2
	FXMLLoader loader2 = new FXMLLoader(getClass().getResource("ChatMockup.fxml"));
	Stage stage2 = new Stage();
	Parent root2 = loader2.load();
	ChatController controller2 = loader2.getController();
	Scene scene2 = new Scene(root2);
    stage2.setTitle("Chat window 2");
    stage2.setScene(scene2);
    stage2.show();
	//connect it all up
	Chat chat1 = new Chat(user1, user2, p1, controller1, channel);
	Chat chat2 = new Chat(user2, user1, p2, controller2, channel);
	channel.addListener(chat1);
	channel.addListener(chat2);
	
  }
  
  public static void main(String[] args) {
    launch(args);
  }
}
