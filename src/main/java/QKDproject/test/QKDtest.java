package QKDproject.test;
import QKDproject.*;
import QKDproject.exception.*;
/**
 * Tests QKD class.
 * @author Marc
 */
public class QKDtest {
	public static void main(String[] args) throws EncryptionException {
		System.out.println("w/o eve");
		QKDAlice alice = new QKDAlice(false, 50);
		QKDBob bob = new QKDBob();
		alice.connect(bob);
		byte[] testMessage = Protocol.stringToBytes("Hello there");
		byte[] encrypted, decrypted;
		try {
			encrypted = alice.encryptMessage(testMessage);
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException e) {
			System.out.println("Decryption failed");
		} catch (KeyExchangeFailure e) {
			System.out.println("Could not establish encryption key");
		}
		
		System.out.println("\nw/ eve, comparing no qubits");
		alice = new QKDAlice(true, 0);
		bob = new QKDBob();
		alice.connect(bob);
		try {
			encrypted = alice.encryptMessage(testMessage);
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException e) {
			System.out.println("Decryption failed");
		} catch (KeyExchangeFailure e) {
			System.out.println("Could not establish encryption key");
		}
		
		System.out.println("\nw/ eve, comparing 1/25 of the qubits");
		alice = new QKDAlice(true, 4);
		bob = new QKDBob();
		alice.connect(bob);
		try {
			encrypted = alice.encryptMessage(testMessage);
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException e) {
			System.out.println("Decryption failed");
		} catch (KeyExchangeFailure e) {
			System.out.println("Could not establish encryption key");
		}
		
		System.out.println("\nw/ eve, comparing half the qubits");
		alice = new QKDAlice(true, 50);
		bob = new QKDBob();
		alice.connect(bob);
		try {
			encrypted = alice.encryptMessage(testMessage);
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException e) {
			System.out.println("Decryption failed");
		}  catch (KeyExchangeFailure e) {
			System.out.println("Could not establish encryption key");
		}
	}
}
