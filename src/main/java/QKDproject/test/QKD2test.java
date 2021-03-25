package QKDproject.test;

import QKDproject.*;
import QKDproject.exception.*;
/**
 *
 * @author Marc
 */
public class QKD2test {
	public static void main(String[] args) throws KeyExchangeFailure, 
			DecryptionException, EncryptionException {
		for (int i = 0; i < 10; i++) {
			System.out.println("w/o eve");
		QKDBob2 bob = new QKDBob2();
		QKDAlice2 alice = new QKDAlice2(bob, false, 50);
		byte[] testMessage = Protocol.stringToBytes("Hello there");
		byte[] encrypted = alice.encryptMessage(testMessage);
		byte[] decrypted;
		try {
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException e) {
			System.out.println("Decryption failed");
		}
		}
		
	}
}
