/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject;

import javafx.beans.Observable;
import javafx.beans.property.*;
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
	private int numberMessages = 0;
	
	public ChatController() {
	}
	public void initialize() {
		chatGrid.setVgap(10);
		chatGrid.setPadding(new Insets(0, 5, 30, 0));
	}
	@FXML
	public void sendMessage() {
		//make and add chat bubble
		ChatBubble newBubble = new ChatBubble(textfield.getText(), chatGrid.widthProperty());
		int column = (numberMessages+1) % 2;
		chatGrid.add(newBubble, column, numberMessages++);
		RowConstraints constraint = new RowConstraints();
		constraint.prefHeightProperty().bind(newBubble.heightProperty());
		constraint.setVgrow(Priority.NEVER);
		chatGrid.getRowConstraints().add(constraint);
		//scroll to bottom
		scrollPane.vvalueProperty().set(scrollPane.getVmax());
	}
}

class ChatBubble extends StackPane {
	private Rectangle bubble;
	private Text text;
	private ReadOnlyDoubleProperty parentWidth;
	public ChatBubble(String text, ReadOnlyDoubleProperty width) {
		bubble = new Rectangle();
		bubble.setFill(Color.web("#548C54"));
		bubble.setStroke(Color.BLACK);
		bubble.widthProperty().bind(width.divide(2));
		
		this.text = new Text(text);
		this.text.wrappingWidthProperty().bind(bubble.widthProperty().subtract(10));
		
		this.text.boundsInLocalProperty().addListener(cl -> {
			var b = (ReadOnlyObjectProperty<Bounds>)cl;
			bubble.heightProperty().set(b.get().getHeight() + 10);
		});
		
		bubble.heightProperty().set(this.text.boundsInLocalProperty().getValue().getHeight() + 10);
		this.getChildren().addAll(bubble, this.text);
		
		this.setPadding(new Insets(5,5,5,5));
	}
}