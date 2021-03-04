/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject.test;

import QKDproject.Protocol;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 *
 * @author Marc
 */
public class EncryptionTest {
	public static void main(String[] args) {
		MockProtocol m = new MockProtocol();
		String message = "éhello this is a test é";
		byte[] messageBytes = Protocol.stringToBytes(message);
		byte[] encrypted = m.encryptMessage(messageBytes);
		byte[] decrypted = m.decryptMessage(encrypted);
		String decryptedStr = Protocol.bytesToString(decrypted);
		
		if (message.equals(decryptedStr)) {
			System.out.println("Message survived encryption");
		} else {
			System.out.println("Message different before and after");
		}
	}
}
