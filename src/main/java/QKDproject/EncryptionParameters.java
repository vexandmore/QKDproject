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
			
			QKAuser alice = new QKAuser();
			QKAuser bob = new QKAuser();
			alice.connect(bob);
			
			Protocol a = new Protocol() {
				boolean hasKey = false;
				@Override
				public byte[] encryptMessage(byte[] message) throws KeyExchangeFailure, EncryptionException {
					if (hasKey) {
						return alice.encryptMessage(message);
					} else {
						try {
							test.makeKey(alice, bob);
							hasKey = true;
							return alice.encryptMessage(message);
						} catch (java.io.IOException e) {
							throw new KeyExchangeFailure(e);
						}
					}
				}
				@Override
				public byte[] decryptMessage(byte[] encryptedMessage) throws KeyExchangeFailure, DecryptionException {
					if (hasKey) {
						return alice.decryptMessage(encryptedMessage);
					} else {
						try {
							test.makeKey(alice, bob);
							hasKey = true;
							return alice.decryptMessage(encryptedMessage);
						} catch (java.io.IOException e) {
							throw new KeyExchangeFailure(e);
						}
					}
				}
			};
			
			Protocol b = new Protocol() {
				boolean hasKey = false;
				@Override
				public byte[] encryptMessage(byte[] message) throws KeyExchangeFailure, EncryptionException {
					if (hasKey) {
						return bob.encryptMessage(message);
					} else {
						try {
							test.makeKey(alice, bob);
							hasKey = true;
							return bob.encryptMessage(message);
						} catch (java.io.IOException e) {
							throw new KeyExchangeFailure(e);
						}
					}
				}
				@Override
				public byte[] decryptMessage(byte[] encryptedMessage) throws KeyExchangeFailure, DecryptionException {
					if (hasKey) {
						return bob.decryptMessage(encryptedMessage);
					} else {
						try {
							test.makeKey(alice, bob);
							hasKey = true;
							return bob.decryptMessage(encryptedMessage);
						} catch (java.io.IOException e) {
							throw new KeyExchangeFailure(e);
						}
					}
				}
			};
			out[0] = a;
			out[1] = b;
			return out;
		}
	}
}