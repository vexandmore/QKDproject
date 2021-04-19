package QKDproject;

import QKDproject.exception.DecryptionException;
import QKDproject.exception.EncryptionException;
import QKDproject.exception.KeyExchangeFailure;

/**
 * Encapsulates the current encryption parameters between two users.
 * @author Marc
 */
public class EncryptionParameters {
	public enum EncryptionType {
		QKD,
		QKA
	}
	
	public final EncryptionType type;
	public final boolean eavesdropped;
	public final double security;

	public EncryptionParameters(EncryptionType type, boolean eavesdropped, double security) {
		this.type = type;
		this.eavesdropped = eavesdropped;
		this.security = security;
	}
	
	/**
	 * Make a pair of protocols that have the properties of this object.
	 * @return Array containing 2 protocols.
	 */
	public Protocol[] makeProtocols() {
		Protocol[] out = new Protocol[2];
		if (this.type == EncryptionType.QKD) {
			QKDChannel channel = new QKDChannel(eavesdropped);
			QKDBob2 bob = new QKDBob2();
			QKDAlice2 alice = new QKDAlice2((int)security);
			channel.setup(alice, bob);
			out[0] = alice;
			out[1] = bob;
			return out;
		} else {
			QKA test = new QKA(eavesdropped, security);
			//This is made as a one element array so whether or not the key is made
			//is a state that will be shared between both QKA users.
			boolean[] keyMade = new boolean[1];
			
			QKAuser alice = new QKAuser();
			QKAuser bob = new QKAuser();
			alice.connect(bob);
			
			Protocol a = new Protocol() {
				@Override
				public byte[] encryptMessage(byte[] message) throws KeyExchangeFailure, EncryptionException {
					if (keyMade[0]) {
						return alice.encryptMessage(message);
					} else {
						try {
							test.makeKey(alice, bob);
							keyMade[0] = true;
							return alice.encryptMessage(message);
						} catch (java.io.IOException e) {
							throw new KeyExchangeFailure(e);
						}
					}
				}
				@Override
				public byte[] decryptMessage(byte[] encryptedMessage) throws KeyExchangeFailure, DecryptionException {
					return alice.decryptMessage(encryptedMessage);
				}
			};
			
			Protocol b = new Protocol() {
				@Override
				public byte[] encryptMessage(byte[] message) throws KeyExchangeFailure, EncryptionException {
					if (keyMade[0]) {
						return bob.encryptMessage(message);
					} else {
						try {
							test.makeKey(alice, bob);
							keyMade[0] = true;
							return bob.encryptMessage(message);
						} catch (java.io.IOException e) {
							throw new KeyExchangeFailure(e);
						}
					}
				}
				@Override
				public byte[] decryptMessage(byte[] encryptedMessage) throws KeyExchangeFailure, DecryptionException {
					return bob.decryptMessage(encryptedMessage);
				}
			};
			out[0] = a;
			out[1] = b;
			return out;
		}
	}
}