package QKDproject;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.scene.Node;
import QKDproject.exception.*;
import javafx.scene.input.*;


/**
 * Controller for the Chat window.
 * @author Marc
 */
public class ChatController {
	@FXML public VBox chatGrid;
	@FXML public ScrollPane scrollPane;
	@FXML public TextField textfield;
	@FXML public BorderPane pane;
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
	
	@FXML
	private void initialize() {
	}
	
	@FXML
	private void handleTextFieldKeypress(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ENTER))
			sendMessage();
	}
	
	@FXML
	private void sendMessage() {
		ChatBubble newBubble = new ChatBubble(textfield.getText(), chatGrid.widthProperty(), Pos.CENTER_RIGHT);
		ChatIndicator i = new ChatIndicator(ChatIndicator.Progress.STARTED);
		addToThread(newBubble, i);
		
		//send message to other chatter asynchronously and scroll pane
		String message = textfield.getText();
		textfield.setText("");
		new Thread(() -> {
			try {
				chat.sendMessage(message);
				i.setProgress(ChatIndicator.Progress.SENT);
			} catch (Exception t) {
				showError(t);
				i.setProgress(ChatIndicator.Progress.FAILED);
			}
		}).start();
	}
	
	private void showError(Throwable t) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			String titleText = "Unexpected Error";
			if (t instanceof KeyExchangeFailure) {
				titleText = "Error exchanging key";
			} else if (t instanceof EncryptionException) {
				titleText = "Error encrypting message";
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
			//make and add chat bubble
			ChatBubble newBubble = new ChatBubble(message, chatGrid.widthProperty(), Pos.CENTER_LEFT);
			addToThread(newBubble);
		});
	}
	
	/**
	 * Intended to be called when the Chat managing the receiving of messages
	 * encounters an exception. Can be called from any thread.
	 * @param t Throwable that caused the error.
	 */
	protected void errorReceivingMessage(Throwable t) {
		if (t instanceof DecryptionException) {
			ChatBubble newBubble = new ChatBubble("[could not decrypt]", chatGrid.widthProperty(), Pos.CENTER_LEFT, Color.GREY);
			Platform.runLater(() -> {
				addToThread(newBubble);
			});
		} else {
			showError(t);
		}
	}
	
	/**
	 * Add nodes to the chat window and scrolls to bottom. Must be called from 
	 * the GUI thread.
	 * @param elements Nodes to add to chat thread
	 */
	private void addToThread(Node... elements) {
		chatGrid.getChildren().addAll(elements);
		scrollPane.layout();
		scrollPane.vvalueProperty().set(scrollPane.getVmax());//scroll
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
	
	public ChatBubble(String text, ReadOnlyDoubleProperty width, Pos alignment) {
		this(text, width, alignment, Color.web("#548C54"));
	}
	public ChatBubble(String text, ReadOnlyDoubleProperty width, Pos alignment, Color bubbleColor) {
		//Make bubble, bind width to chat window
		bubble = new Rectangle();
		bubble.setFill(bubbleColor);
		bubble.setStroke(Color.BLACK);
		bubble.widthProperty().bind(width.divide(2));
		bubble.arcHeightProperty().set(15);
		bubble.arcWidthProperty().set(15);
		//add text and bind its width
		this.text = new Text(text);
		this.text.wrappingWidthProperty().bind(bubble.widthProperty().subtract(10));
		StackPane.setMargin(this.text, new Insets(0, 5, 0, 5));
		
		
		//Make the rectangle scale in height with text
		this.text.boundsInLocalProperty().addListener(cl -> {
			var b = (ReadOnlyObjectProperty<Bounds>)cl;
			bubble.heightProperty().set(b.get().getHeight() + 10);
		});
		bubble.heightProperty().set(this.text.boundsInLocalProperty().getValue().getHeight() + 10);
		
		this.setAlignment(alignment);
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
		this.setPadding(new Insets(0, 5, 5, 0));
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