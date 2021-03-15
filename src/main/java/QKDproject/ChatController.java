/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject;

import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 *
 * @author Marc
 */
public class ChatController {
	@FXML public GridPane chatGrid;
	@FXML public ScrollPane scrollPane;
	@FXML public TextField textfield;
	@FXML public BorderPane pane;
	private int numberMessages = 3;
	//private RowConstraints constraint;
	
	public ChatController() {
	}
	public void initialize() {
		chatGrid.getRowConstraints().clear();
		for (int i = 0; i < 3; i++) {
		RowConstraints constraint = new RowConstraints();
		constraint.setVgrow(Priority.SOMETIMES);
		constraint.setMinHeight(100);
		chatGrid.getRowConstraints().add(constraint);
		}
		//add blank 50pix row
		/*RowConstraints constraint = new RowConstraints();
		constraint.setVgrow(Priority.SOMETIMES);
		constraint.setMinHeight(100);
		chatGrid.getRowConstraints().add(constraint);
		chatGrid.add(new Text(""), 1, numberMessages+1);
		chatGrid.getRowConstraints().add(constraint);*/
		
		chatGrid.setVgap(10);
		chatGrid.setPadding(new Insets(0, 0, 50, 0));
	}
	@FXML
	public void sendMessage() {
		System.out.println(textfield.getText());
		ChatBubble newBubble = new ChatBubble(textfield.getText());
		chatGrid.add(newBubble, 1, numberMessages++);
		
		RowConstraints constraint = new RowConstraints();
		//constraint.setMinHeight(100);
		constraint.setMinHeight(newBubble.getBHeight());
		constraint.setVgrow(Priority.ALWAYS);
		chatGrid.getRowConstraints().add(constraint);
		
		scrollPane.vvalueProperty().set(scrollPane.getVmax());
	}
}

class ChatBubble extends StackPane {
	private Rectangle bubble;
	private Text text;
	public ChatBubble(String text) {
		bubble = new Rectangle();
		bubble.setFill(Color.GREEN);
		bubble.setStroke(Color.BLACK);
		bubble.widthProperty().set(200);
		
		this.text = new Text(text);
		this.text.wrappingWidthProperty().bindBidirectional(bubble.widthProperty());
		bubble.heightProperty().set(this.text.getBoundsInLocal().getHeight());
		this.getChildren().addAll(bubble, this.text);
	}
	
	public double getBHeight() {
		return bubble.heightProperty().doubleValue();
	}
}