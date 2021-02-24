/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 *
 * @author Marc
 */
public class Main extends Application {
	@Override // Override the start method in the Application class
  public void start(Stage stage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("ChatMockup.fxml"));
    
    Scene scene = new Scene(root);
    stage.setTitle("Login");
    stage.setScene(scene);
    stage.show();//test
  }
  
  public static void main(String[] args) {
    launch(args);
  }
}
