/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject.test;

import QKDproject.Protocol;
import java.util.Random;
import com.google.crypto.tink.subtle.AesGcmJce;
import java.security.GeneralSecurityException;
/**
 * Simple class to test encryption with tink.
 * @author Marc
 */
public class MockProtocol implements Protocol {
	private byte[] sharedKey;
	
	public MockProtocol() {
		sharedKey = determineSharedKey();
	}
	
	private byte[] determineSharedKey() {
		Random r = new Random();
		byte[] key = new byte[16];
		r.nextBytes(key);
		return key;
	}

	@Override
	public byte[] encryptMessage(byte[] message) {
		try {
			AesGcmJce a = new AesGcmJce(sharedKey);
			byte[] encrypted = a.encrypt(message, new byte[0]);
			return encrypted;
		} catch (GeneralSecurityException ex) {
			System.out.println("error\n" + ex);
			return null;
		}
	}

	@Override
	public byte[] decryptMessage(byte[] encryptedMessage) {
		try {
			AesGcmJce a = new AesGcmJce(sharedKey);
			byte[] decrypted = a.decrypt(encryptedMessage, new byte[0]);
			return decrypted;
		} catch (GeneralSecurityException ex) {
			System.out.println("Error\n" + ex);
			return null;
		}
	}
	
}
