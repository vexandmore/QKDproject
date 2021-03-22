/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import QKDproject.exception.*;

/**
 * Controller for the Chat window.
 * @author Marc
 */
public class ChatController {
	@FXML public GridPane chatGrid;
	@FXML public ScrollPane scrollPane;
	@FXML public TextField textfield;
	@FXML public BorderPane pane;
	private int currentRow = 0;
	private Chat chat;
	
	public ChatController() {
	}
	
	/**
	 * Called by the Chat so that this can send messages to Chat and Chat can 
	 * notify this of new messages.
	 * @param c Chat instance.
	 */
	protected void setChat(Chat c) {
		this.chat = c;
	}
	
	private void initialize() {
		chatGrid.setVgap(10);
		//chatGrid.setPadding(new Insets(30, 30, 30, 30));
	}
	
	@FXML
	private void sendMessage() {
		/*make and add chat bubble*/
		ChatBubble newBubble = new ChatBubble(textfield.getText(), chatGrid.widthProperty());
		int column = 1;
		chatGrid.add(newBubble, column, currentRow++);
		RowConstraints bubbleConstraint = new RowConstraints();
		bubbleConstraint.prefHeightProperty().bind(newBubble.heightProperty());
		bubbleConstraint.setVgrow(Priority.NEVER);
		chatGrid.getRowConstraints().add(bubbleConstraint);
		//scroll to bottom
		scrollPane.vvalueProperty().set(scrollPane.getVmax());
		/*make and add progress indicator*/
		ChatIndicator i = new ChatIndicator(ChatIndicator.Progress.STARTED);
		RowConstraints progressConstraint = new RowConstraints();
		progressConstraint.prefHeightProperty().bind(i.heightProperty());
		chatGrid.getRowConstraints().add(progressConstraint);
		chatGrid.add(i, column, currentRow++);
		
		//send message to other chatter asynchronously
		Runnable r = () -> {
			try {
				chat.sendMessage(textfield.getText());
				i.setProgress(ChatIndicator.Progress.SENT);
			} catch (Exception t) {
				showError(t);
				i.setProgress(ChatIndicator.Progress.FAILED);
			}
		};
		new Thread(r).start();
	}
	
	private void showError(Throwable t) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			String titleText = "Error";
			if (t instanceof KeyExchangeFailure) {
				titleText = "Error exchanging key";
			} else if (t instanceof EncryptionException) {
				titleText = "Error encrypting message";
			} else if (t instanceof DecryptionException) {
				titleText = "Error decrypting message";
			}
			alert.setTitle(titleText);
			alert.setHeaderText(titleText);
			alert.setContentText(t.toString());
			alert.showAndWait();
		});
	}
	
	/**
	 * Displays the message as a message someone else sent. Can be called 
	 * from a thread other than this's GUI thread (runs itself with 
	 * Platform.runLater())
	 * @param message Message to display
	 */
	protected void receiveMessage(String message) {
		Platform.runLater(() -> {
			if (message == null) {
				showError(new IllegalArgumentException("Received null message"));
			} else {
				//make and add chat bubble
				ChatBubble newBubble = new ChatBubble(message, chatGrid.widthProperty());
				int column = 0;
				chatGrid.add(newBubble, column, currentRow++);
				RowConstraints constraint = new RowConstraints();
				constraint.prefHeightProperty().bind(newBubble.heightProperty());
				constraint.setVgrow(Priority.NEVER);
				chatGrid.getRowConstraints().add(constraint);
				//scroll to bottom
				scrollPane.vvalueProperty().set(scrollPane.getVmax());
			}
		});
	}
	
	/**
	 * Intended to be called when the Chat managing the receiving of messages
	 * encounters an exception.
	 * @param t Throwable that caused the error.
	 */
	protected void errorReceivingMessage(Throwable t) {
		showError(t);
	}
}

/**
 * Represents a chat bubble, ie text in a rectangle.
 * @author Marc
 */
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
		
		//Make the rectangle scale in height with text
		this.text.boundsInLocalProperty().addListener(cl -> {
			var b = (ReadOnlyObjectProperty<Bounds>)cl;
			bubble.heightProperty().set(b.get().getHeight() + 10);
		});
		
		bubble.heightProperty().set(this.text.boundsInLocalProperty().getValue().getHeight() + 10);
		this.getChildren().addAll(bubble, this.text);
		
		this.setPadding(new Insets(5,5,5,5));
	}
}

/**
 * Represents progress on whether the message has been sent.
 * @author Marc
 */
class ChatIndicator extends StackPane {
	enum Progress {
		STARTED, SENT, FAILED
	}
	
	private Progress progress;
	private Text indicator;
	
	public ChatIndicator() {
		this(Progress.STARTED);
	}
	
	public ChatIndicator(Progress initialProgress) {
		indicator = new Text();
		this.getChildren().add(indicator);
		this.setAlignment(Pos.CENTER_RIGHT);
		setProgress(initialProgress);
	}
	
	/**
	 * Updates the progress display. Can be called outside the main GUI thread.
	 * @param newProgress 
	 */
	public void setProgress(Progress newProgress) {
		progress = newProgress;
		updateProgress();
	}
	
	private void updateProgress() {
		Platform.runLater(() -> {
			switch (progress) {
				case STARTED:
					indicator.setText("Sending...");
					break;
				case SENT:
					indicator.setText("Sent.");
					break;
				case FAILED:
					indicator.setText("Message failed to send");
			}
		});
	}
}