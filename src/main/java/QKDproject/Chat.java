package QKDproject;
import java.io.*;

/**
 * Represents 
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
		this.protocol = protocol;
		this.chatView = controller;
		this.channel = channel;
		this.chatView.setChat(this);
	}
	
	protected void sendMessage(String plaintext) {
		channel.sendMessage(protocol.encryptMessage(Protocol.stringToBytes(plaintext)), this);
	} 
	
	@Override
	public void newMessageAvailable(byte[] message) {
		try {
			byte[] decrypted = protocol.decryptMessage(message);
			String plaintext = Protocol.bytesToString(decrypted);
			chatView.receiveMessage(plaintext);
		} catch (DecryptionFailed e) {
			chatView.receiveMessage(null);
		}
	}
	
	public static void openChat(Chat chat, File chatHistory) {
		
	}
}
