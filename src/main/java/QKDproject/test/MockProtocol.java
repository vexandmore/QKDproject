/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject.test;

import QKDproject.Protocol;
import java.util.Random;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import java.security.GeneralSecurityException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
/**
 * Simple class to test encryption with tink.
 * @author Marc
 */
public class MockProtocol implements Protocol {
	private String sharedKey;
	private StandardPBEByteEncryptor textEncryptor = new StandardPBEByteEncryptor();
	
	public MockProtocol() {
		sharedKey = determineSharedKey();
		textEncryptor.setPassword(sharedKey);
	}
	
	private String determineSharedKey() {
		Random r = new Random();
		StringBuilder keyB = new StringBuilder();
		for (int i = 0; i < 128; i++) {
			keyB.append(r.nextInt());
		}
		return keyB.toString();
	}

	@Override
	public byte[] encryptMessage(byte[] message) {
		try {
			return textEncryptor.encrypt(message);
		} catch (EncryptionOperationNotPossibleException ex) {
			System.out.println("error\n" + ex);
			return null;
		}
	}

	@Override
	public byte[] decryptMessage(byte[] encryptedMessage) {
		try {
			return textEncryptor.decrypt(encryptedMessage);
		} catch (EncryptionOperationNotPossibleException ex) {
			System.out.println("error\n" + ex);
			return null;
		}
	}
	
}
