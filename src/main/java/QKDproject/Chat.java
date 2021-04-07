package QKDproject;
import java.io.*;
import java.util.Objects;
import QKDproject.exception.*;

/**
 * Controls the flow of messages; sending and receiving, loading from and 
 * saving to a file.
 * @author Marc
 */
public class Chat implements MessageReader {
	private File chatHistory;
	private User user1, user2;
	private Protocol protocol;
	private ChatController chatView;
	private CommunicationChannel channel;
	
	public Chat(User user1, User user2, Protocol protocol, ChatController controller, CommunicationChannel channel) {
		this.user1 = user1;
		this.user2 = user2;
		this.protocol = Objects.requireNonNull(protocol, "Protocol passed must be non null");
		this.chatView = Objects.requireNonNull(controller, "ChatController must be non null");
		this.channel = channel;
		this.chatView.setChat(this);
	}
	
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
		} catch (Throwable t) {
			chatView.errorReceivingMessage(t);
		}
	}
	
	public static void openChat(Chat chat, File chatHistory) {
		
	}
}
