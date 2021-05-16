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
	  FXMLLoader loader = new FXMLLoader(getClass().getResource("ControlUsers.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root);
		stage.setTitle("Controller");
		stage.setMinWidth(450);
		stage.setScene(scene);
		stage.show();
  }
  
  public static void main(String[] args) {
    launch(args);
  }
}
