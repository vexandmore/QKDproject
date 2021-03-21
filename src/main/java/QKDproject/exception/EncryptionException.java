package QKDproject.exception;

/**
 * Thrown when an error occurs in encryption.
 * @author Marc
 */
public class EncryptionException extends Exception {
	public EncryptionException() {
		super();
	}
	
	public EncryptionException(String msg) {
		super(msg);
	}
	
	public EncryptionException(String msg, Exception cause) {
		super(msg, cause);
	}
	
	public EncryptionException(Exception cause) {
		super(cause);
	}
	
	public String toString() {
		return this.getMessage();
	}
}
