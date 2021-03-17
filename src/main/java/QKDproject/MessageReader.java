package QKDproject;

/**
 * This interface allows the CommunicationChannel to signal the Chat instances
 * that a message has been sent.
 * @author Marc
 */
public interface MessageReader {
	public void newMessageAvailable(byte[] message);
}
