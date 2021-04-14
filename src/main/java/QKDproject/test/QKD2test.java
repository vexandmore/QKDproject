package QKDproject.test;

import QKDproject.*;
import QKDproject.exception.*;
/**
 *
 * @author Marc
 */
public class QKD2test {
	public static void main(String[] args) throws EncryptionException {
		
		System.out.println("w/o eve");
		QKDChannel channel = new QKDChannel(false);
		QKDBob2 bob = new QKDBob2();
		QKDAlice2 alice = new QKDAlice2(50);
		channel.setup(alice, bob);
		byte[] testMessage = Protocol.stringToBytes("Hello there");
		byte[] encrypted, decrypted;
		try {
			encrypted = alice.encryptMessage(testMessage);
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException | KeyExchangeFailure e) {
			System.out.println("Decryption failed");
		}
		
		System.out.println("\nw/ eve");
		bob = new QKDBob2();
		channel.setEavesdropping(true);
		alice = new QKDAlice2(50);
		channel.setup(alice, bob);
		try {
			encrypted = alice.encryptMessage(testMessage);
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException | KeyExchangeFailure e) {
			System.out.println("Error: " + e.getMessage());
		}
		
		System.out.println("\nw/ eve, limited security check");
		bob = new QKDBob2();
		channel.setEavesdropping(true);
		alice = new QKDAlice2(2);
		channel.setup(alice, bob);
		try {
			encrypted = alice.encryptMessage(testMessage);
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException | KeyExchangeFailure e) {
			System.out.println("Error: " + e.getMessage());
		}
		
		System.out.println("\nw/ eve, with no security");
		bob = new QKDBob2();
		channel.setEavesdropping(true);
		alice = new QKDAlice2(0);
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
