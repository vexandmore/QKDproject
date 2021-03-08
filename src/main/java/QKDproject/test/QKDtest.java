package QKDproject.test;
import QKDproject.*;
/**
 * Tests QKD class.
 * @author Marc
 */
public class QKDtest {
	public static void main(String[] args) {
		System.out.println("w/o eve");
		QKD qkd = new QKD(false, 50, true);
		QKD o = new QKD(false, 50, false);
		qkd.connect(o);
		byte[] testMessage = Protocol.stringToBytes("Hello there");
		byte[] encrypted = qkd.encryptMessage(testMessage);
		byte[] decrypted;
		try {
			decrypted = o.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionFailed e) {
			System.out.println("Decryption failed");
		}
		
		System.out.println("\nw/ eve, comparing no qubits");
		qkd = new QKD(true, 0, true);
		o = new QKD(true, 0, false);
		qkd.connect(o);
		encrypted = qkd.encryptMessage(testMessage);
		try {
			decrypted = o.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionFailed e) {
			System.out.println("Decryption failed");
		}
		
		System.out.println("\nw/ eve, comparing a twentieth the qubits");
		qkd = new QKD(true, 5, true);
		o = new QKD(true, 5, false);
		qkd.connect(o);
		encrypted = qkd.encryptMessage(testMessage);
		try {
			decrypted = o.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionFailed e) {
			System.out.println("Decryption failed");
		}
		
		System.out.println("\nw/ eve, comparing half the qubits");
		qkd = new QKD(true, 50, true);
		o = new QKD(true, 50, false);
		qkd.connect(o);
		encrypted = qkd.encryptMessage(testMessage);
		try {
			decrypted = o.decryptMessage(encrypted);
			System.out.println("Message: " + Protocol.bytesToString(decrypted));
		} catch (DecryptionFailed e) {
			System.out.println("Decryption failed");
		}
	}
}
