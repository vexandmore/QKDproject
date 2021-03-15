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
	//private RowConstraints constraint;
	
	public ChatController() {
	}
	public void initialize() {
		chatGrid.setVgap(10);
		chatGrid.setPadding(new Insets(0, 0, 30, 0));
		
		ColumnConstraints a = new ColumnConstraints();
		a.setPercentWidth(50);
		ColumnConstraints b = new ColumnConstraints();
		b.setPercentWidth(50);
		chatGrid.getColumnConstraints().addAll(a, b);
	}
	@FXML
	public void sendMessage() {
		ChatBubble newBubble = new ChatBubble(textfield.getText(), chatGrid.widthProperty());
		int column = numberMessages % 2;
		chatGrid.add(newBubble, column, numberMessages++);
		
		RowConstraints constraint = new RowConstraints();
		constraint.setMinHeight(newBubble.getBHeight());
		constraint.setVgrow(Priority.NEVER);
		
		chatGrid.getRowConstraints().add(constraint);
		
		scrollPane.vvalueProperty().set(scrollPane.getVmax());
	}
}

class ChatBubble extends StackPane {
	private Rectangle bubble;
	private Text text;
	private ReadOnlyDoubleProperty parentWidth;
	public ChatBubble(String text, ReadOnlyDoubleProperty width) {
		bubble = new Rectangle();
		bubble.setFill(Color.GREEN);
		bubble.setStroke(Color.BLACK);
		bubble.widthProperty().bind(width.divide(2));
		//bubble.widthProperty().set(200);
		
		this.text = new Text(text);
		this.text.wrappingWidthProperty().bindBidirectional(bubble.widthProperty());
		
		this.text.boundsInLocalProperty().addListener(cl -> {
			var b = (ReadOnlyObjectProperty<Bounds>)cl;
			bubble.heightProperty().set(b.get().getHeight());
		});
		
		bubble.heightProperty().set(this.text.boundsInLocalProperty().getValue().getHeight());
		this.getChildren().addAll(bubble, this.text);
	}
	
	public double getBHeight() {
		return bubble.heightProperty().doubleValue();
	}
}