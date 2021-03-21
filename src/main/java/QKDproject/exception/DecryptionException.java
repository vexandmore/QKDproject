package QKDproject.exception;

/**
 * Thrown when an error occurs in decrypting a message.
 * @author Marc
 */
public class DecryptionException extends Exception {
	public DecryptionException() {
		super();
	}
	
	public DecryptionException(String msg) {
		super(msg);
	}
	
	public DecryptionException(String msg, Exception cause) {
		super(msg, cause);
	}
	
	public DecryptionException(Exception cause) {
		super(cause);
	}
	
	public String toString() {
		return this.getMessage();
	}
}
