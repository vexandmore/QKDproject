
package QKDproject;

import QKDproject.exception.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.scene.Node;
import javafx.scene.input.*;


/**
 * Controller for the Chat window.
 * @author Marc and Raphael
 */
public class ChatController {
	@FXML public VBox chatGrid;
	@FXML public ScrollPane scrollPane;
	@FXML public TextField textfield;
	@FXML public BorderPane pane;
	@FXML public Button sendButton;
	@FXML public Text headerText;
	@FXML public Button backButton;
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
		headerText.setText("Conversation with " + c.getUser2());
	}
	
	@FXML
	private void initialize() {
		sendButton.disableProperty().bind(textfield.textProperty().isEmpty());
	}
	
	@FXML
	private void handleTextFieldKeypress(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ENTER) && !(textfield.getText().isEmpty()))
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
	
	public ChatBubble(String text, ReadOnlyDoubleProperty width, Pos alignment) {
		this(text, width, alignment, Color.web("#548C54"));
	}
	
	public ChatBubble(String text, ReadOnlyDoubleProperty width, Pos alignment, Color bubbleColor) {
		//Make bubble
		bubble = new Rectangle();
		bubble.setFill(bubbleColor);
		bubble.setStroke(Color.BLACK);
		bubble.setArcHeight(15);
		bubble.setArcWidth(15);
		//add text and get its total width
		this.text = new Text(text);
		double textWidth = this.text.prefWidth(-1);
		
		//This listener changes the chat bubble's dimensions whenever the width changes
		ChangeListener<Number> n = (ObservableValue<? extends Number> o, Number old, Number newValue) -> {
			double maxWidth = newValue.doubleValue() * 0.75;
			if (textWidth < maxWidth) {
				this.text.setWrappingWidth(textWidth);
				bubble.setWidth(textWidth + 10);
			} else {
				this.text.setWrappingWidth(maxWidth);
				bubble.setWidth(maxWidth + 10);
			}
			bubble.setHeight(this.text.boundsInLocalProperty().get().getHeight() + 10);
		};
		n.changed(width, width.getValue(), width.getValue());
		width.addListener(n);
		//Set margin and alignments
		StackPane.setMargin(this.text, new Insets(0, 5, 0, 5));
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