package QKDproject;
import java.io.*;
import java.util.Objects;
import QKDproject.exception.*;
import javafx.beans.property.*;

/**
 * Controls the flow of messages; sending and receiving. Connects to a
 * CommunicationChannel and one ChatController.
 * @author Marc
 */
public class Chat implements MessageReader {
	private User user1, user2;
	private Protocol protocol;
	private ChatController chatView;
	private CommunicationChannel channel;
	private SimpleStringProperty latestMessage = new SimpleStringProperty();
	
	public Chat(User user1, User user2, Protocol protocol, ChatController controller, CommunicationChannel channel) {
		this.user1 = user1;
		this.user2 = user2;
		this.protocol = Objects.requireNonNull(protocol);
		this.chatView = Objects.requireNonNull(controller);
		this.channel = channel;
		this.chatView.setChat(this);
	}
	
	/**
	 * Get the main user represented by this Chat.
	 */
	public User getUser1() {
		return user1;
	}
	
	public User getUser2() {
		return user2;
	}
	
	protected void changeProtocol(Protocol newProtocol) {
		this.protocol = newProtocol;
	}
	
	/**
	 * Sends message to other Chat.Intended to be called from a Chat.
	 *
	 * @param plaintext Message to be encrypted and sent.
	 * @throws QKDproject.exception.EncryptionException
	 * @throws QKDproject.exception.KeyExchangeFailure
	 */
	protected void sendMessage(String plaintext) throws EncryptionException, KeyExchangeFailure {
		channel.sendMessage(protocol.encryptMessage(Protocol.stringToBytes(plaintext)), this);
		latestMessage.set(user1.getName() + ": " + plaintext);
	} 
	
	/**
	 * Decrypts and sends received message to the connected Chat instance.
	 * @param message Encrypted data.
	 */
	@Override
	public void newMessageAvailable(byte[] message) {
		try {
			byte[] decrypted = protocol.decryptMessage(message);
			String plaintext = Protocol.bytesToString(decrypted);
			chatView.receiveMessage(plaintext);
			latestMessage.set(user2.getName() + ": " + plaintext);
		} catch (Throwable t) {
			chatView.errorReceivingMessage(t);
		}
	}
	
	/**
	 * Returns a property representing the latest message sent between the
	 * chatters. The String is the username, a colon, followed by the message.
	 */
	public ReadOnlyStringProperty latestMessageProperty() {
		return latestMessage;
	}
}
