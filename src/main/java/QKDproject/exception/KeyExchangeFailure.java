package QKDproject.exception;

/**
 * Thrown when key exchange fails.
 * @author Marc
 */
public class KeyExchangeFailure extends Exception {
	public KeyExchangeFailure() {
		super();
	}
	
	public KeyExchangeFailure(String msg) {
		super(msg);
	}
	
	public KeyExchangeFailure(String msg, Exception cause) {
		super(msg, cause);
	}
	
	public KeyExchangeFailure(Exception cause) {
		super(cause);
	}
	
	public String toString() {
		return this.getMessage();
	}
}
