package QKDproject;
import java.util.*;

/**
 * Class the Chats will use to send encrypted messages to each other. Represents
 * a public network.
 * @author Marc
 */
public class CommunicationChannel {
	private List<MessageReader> listeners = new ArrayList<>();
	
	public void addListener(MessageReader listener) {
		listeners.add(listener);
	}
	
	/**
	 * Send message to all listeners.
	 * @param message
	 * @param caller 
	 */
	public void sendMessage(byte[] message, MessageReader caller) {
		System.out.println("Message: " + Protocol.bytesToString(message));
		for (MessageReader m: listeners) {
			if (m != caller)
				m.newMessageAvailable(message);
		}
	}
}
