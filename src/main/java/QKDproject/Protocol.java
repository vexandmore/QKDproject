package QKDproject;
import java.nio.charset.StandardCharsets;
import QKDproject.exception.*;

/**
 * Encapsulates a key exchange and encryption protocol. Also contains utility
 * methods to turn a String into a byte[] and a byte[] back into a String (this
 * uses UTF-8)
 * @author Marc
 */
public interface Protocol {
	public byte[] encryptMessage(byte[] message) throws KeyExchangeFailure, EncryptionException;
	public byte[] decryptMessage(byte[] encryptedMessage) throws KeyExchangeFailure, DecryptionException;
	
	public static byte[] stringToBytes(String str) {
		return str.getBytes(StandardCharsets.UTF_8);
	}
	public static String bytesToString(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}
}
