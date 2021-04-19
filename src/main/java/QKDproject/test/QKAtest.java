
package QKDproject.test;
import QKDproject.*;
import QKDproject.exception.*;
import java.io.IOException;

/**
 *
 * @author Raphael
 * Tests QKA class
 */
public class QKAtest {
    public static void main(String[] args) throws IOException, DecryptionException {
        //System.out.println("w/o Eve");
        QKA test = new QKA(false, 0.5);
        QKAuser alice = new QKAuser();
        QKAuser bob = new QKAuser();
        alice.connect(bob);
        
        test.makeKey(alice,bob);
        String testMessage = "Hello World";
        String encrypted = alice.encryptMessage(testMessage);
        String decrypted = bob.decryptMessage(encrypted);
        System.out.println(decrypted);
        
    }
}
