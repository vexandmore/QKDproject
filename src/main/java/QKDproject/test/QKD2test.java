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
		
		System.out.println("w/o eve");
		QKDChannel channel = new QKDChannel(false);
		QKDBob2 bob = new QKDBob2();
		QKDAlice2 alice = new QKDAlice2(bob, channel, 50);
		channel.setup(alice, bob);
		byte[] testMessage = Protocol.stringToBytes("Hello there");
		byte[] encrypted = alice.encryptMessage(testMessage);
		byte[] decrypted;
		try {
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException e) {
			System.out.println("Decryption failed");
		}
		
		System.out.println("with eve");
		bob = new QKDBob2();
		channel.setEavesdropping(true);
		alice = new QKDAlice2(bob, channel, 50);
		channel.setup(alice, bob);
		try {
			encrypted = alice.encryptMessage(testMessage);
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException | KeyExchangeFailure e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}
