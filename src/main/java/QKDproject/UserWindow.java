/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject;
import QKDproject.Chat;
import QKDproject.User;
import QKDproject.UserWindowController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Raphael
 */
public class UserWindow extends UserWindowController{
    private Scene mainScene;
    private Stage mainStage;
    private User u1;
    private HBox hbox;
    private GridPane grid;
    private final BorderPane border;
    private int numOthers = 0;
    
    
    public UserWindow(User u) {  
        this.border = new BorderPane();
        border.setPrefSize(300, 350);
        this.hbox = new HBox();
        this.hbox.setPadding(new Insets(15, 12, 15, 12));
        this.hbox.setSpacing(10);
        this.hbox.setStyle("-fx-background-color:  #228B22;");
        this.u1 = u;
        Label title = new Label(u1.getName() + "'s chats");
        title.setStyle("-fx-font-weight: bold");
        title.setFont(new Font(18));
        hbox.getChildren().add(title);
        
        
        this.border.setTop(hbox);

        this.grid = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints(200);
        ColumnConstraints col2 = new ColumnConstraints(100);
        grid.getColumnConstraints().addAll(col1, col2);
        
        this.border.setCenter(grid);

        mainStage = new Stage();
        mainStage.setTitle(u1.getName() + "'s chats");
        mainScene = new Scene(border);
        mainStage.setScene(mainScene);
        mainStage.show();
    }

    public void openUserWindow() {
        //this.lastMessage = new TextField(c.latestMessageProperty().toString());
        //updateLastMessage();
        mainStage.show();
    }


    @Override
    public void addChat(Parent chatWindow, Button backButton, Chat c) {
        Scene chatScene = new Scene(chatWindow);
        //change
        Button goToChat = new Button("chat with " + c.getUser2());

        goToChat.setOnAction((ActionEvent ae) -> {
            mainStage.setScene(chatScene);
        });
        backButton.setOnAction((ActionEvent ae) -> {
            mainStage.setScene(mainScene);
        });
        
        //gui stuff
        //idea:gridpane contains in row 0: vbox & last message, row 1: button tochat
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);
        Label otherName = new Label(c.getUser2().getName());
        otherName.setStyle("-fx-font-weight: bold");
        Label latestMessage = new Label(c.latestMessageProperty().toString());
        latestMessage.textProperty().bind(c.latestMessageProperty());
        vbox.getChildren().addAll(otherName, latestMessage);
 
        grid.addRow(numOthers, vbox, goToChat);


        numOthers++; 
    }
        
    @Override
    public User getUser() {
            return u1;
    }
    
}

