package QKDproject.test;
import QKDproject.*;
import QKDproject.exception.*;
/**
 * Tests QKD class.
 * @author Marc
 */
public class QKDtest {
	public static void main(String[] args) throws KeyExchangeFailure, 
			DecryptionException, EncryptionException{
		System.out.println("w/o eve");
		QKDAlice alice = new QKDAlice(false, 50);
		QKDBob bob = new QKDBob();
		alice.connect(bob);
		byte[] testMessage = Protocol.stringToBytes("Hello there");
		byte[] encrypted = alice.encryptMessage(testMessage);
		byte[] decrypted;
		try {
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException e) {
			System.out.println("Decryption failed");
		}
		
		System.out.println("\nw/ eve, comparing no qubits");
		alice = new QKDAlice(true, 0);
		bob = new QKDBob();
		alice.connect(bob);
		encrypted = alice.encryptMessage(testMessage);
		try {
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException e) {
			System.out.println("Decryption failed");
		}
		
		System.out.println("\nw/ eve, comparing 1/25 of the qubits");
		alice = new QKDAlice(true, 4);
		bob = new QKDBob();
		alice.connect(bob);
		encrypted = alice.encryptMessage(testMessage);
		try {
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException e) {
			System.out.println("Decryption failed");
		}
		
		System.out.println("\nw/ eve, comparing half the qubits");
		alice = new QKDAlice(true, 50);
		bob = new QKDBob();
		alice.connect(bob);
		encrypted = alice.encryptMessage(testMessage);
		try {
			decrypted = bob.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionException e) {
			System.out.println("Decryption failed");
		}
	}
}
