package QKDproject;

/**
 * Is thrown when an attempt at decryption fails.
 * @author Marc
 */
public class DecryptionFailed extends Exception {
	public DecryptionFailed(Throwable cause) {
		super(cause);
	}
	public DecryptionFailed(String message, Throwable cause) {
		super(message, cause);
	}
}
