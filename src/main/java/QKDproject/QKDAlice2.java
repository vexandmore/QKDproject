package QKDproject;

import static QKDproject.QKDAlice.bitStringToArray;
import static QKDproject.QKDAlice.keepAtIndices;
import static QKDproject.QKDAlice.removeAtIndices;
import QKDproject.exception.*;
import com.google.crypto.tink.subtle.AesGcmJce;
import org.jasypt.commons.CommonUtils;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 *
 * @author Marc
 */
public class QKDAlice2 implements Protocol {
	private byte[] key;
	protected QKDBob2 bob;
	protected QKDChannel channel;
	public final static int KEY_SIZE = 16;
	private final int securityLevel;
	private String alice_bits, alice_bases, alice_circuits;
	private String alice_sample = "", alice_matching_measured = "";
	
	/**
	 * The path of the python script. Determined at runtime.
	 */
	private static String SCRIPT_LOCATION;
	static {
		try {
			SCRIPT_LOCATION = new File(".").getCanonicalPath() + 
					File.separatorChar + "src" + File.separatorChar + "main" + 
					File.separatorChar + "qkdImplementation2.py";
		} catch (IOException e) {
			SCRIPT_LOCATION = "error getting script location";
		}
	}
	/**
	 * Shared instance of python script, used to do the quantum simulation.
	 */
	private static PyScript python;

	public QKDAlice2(QKDBob2 other, QKDChannel channel, int securityLevel) {
		this.bob = other;
		this.bob.alice = this;
		this.securityLevel = securityLevel;
		this.channel = channel;
	}
	
	protected int getSecurityLevel() {
		return securityLevel;
	}
	
	protected void makeKey() throws KeyExchangeFailure {
		try {
			int bitsSent = (int) ((KEY_SIZE * 8 * 2.5) / (1 - (securityLevel / 100.0)));
			alice_bases = "";
			alice_bits = "";
			Random rand = new Random();
			for (int i = 0; i < bitsSent; i++) {
				alice_bases += rand.nextBoolean() ? '1' : '0';
				alice_bits += rand.nextBoolean() ? '1' : '0';
			}
			alice_circuits = getPython().getResults(bitsSent + " " + alice_bits + " " + alice_bases);
			channel.passCircuitsToBob(alice_circuits);
		} catch (IOException e) {
			throw new KeyExchangeFailure(e);
		}
	}
	
	/**
	 * Returns the indices where Alice and Bob measured in the same base.
	 * @param bobBases String representing the bases Bob measured in.
	 * @return List of the indices where Alice and Bob measured in the same base.
	 */
	protected List<Integer> measuredSameIndices(String bobBases) {
		if (alice_bases.length() != bobBases.length())
			throw new IllegalArgumentException("bases " + bobBases + " " + 
					this.alice_bases + " must be same length");
		List<Integer> matchingIndices = new ArrayList<>();
		for (int i = 0; i < bobBases.length(); i++) {
			if (this.alice_bases.charAt(i) == bobBases.charAt(i)) {
				matchingIndices.add(i);
			}
		}
		alice_matching_measured = keepAtIndices(matchingIndices, alice_bits);
		return matchingIndices;
	}
	
	/**
	 * Check if the samples match. If they do, Alice and Bob both make their 
	 * key.
	 * @param bobSample Bob's sample.
	 * @param sampleIndices The indicies which Bob used to make his sample.
	 * @return Whether or not the sample Alice has made matches Bob's.
	 */
	protected boolean samplesMatch(String bobSample, List<Integer> sampleIndices) {
		alice_sample = keepAtIndices(sampleIndices, alice_matching_measured);
		if (alice_sample.equals(bobSample)) {
			String aliceKey = removeAtIndices(sampleIndices, alice_matching_measured);
			this.key = bitStringToArray(aliceKey, KEY_SIZE);
			return true;
		} else {
			return alice_sample.equals(bobSample);
		}
	}
	
	@Override
	public byte[] encryptMessage(byte[] message) throws KeyExchangeFailure, EncryptionException {
		if (key == null) {
			makeKey();
		}
		try {
			AesGcmJce a = new AesGcmJce(key);
			byte[] encrypted = a.encrypt(message, new byte[0]);
			return encrypted;
		} catch (GeneralSecurityException ex) {
			System.out.println("error\n" + ex);
			return null;
		}
	}
	
	@Override
	public byte[] decryptMessage(byte[] encryptedMessage) throws KeyExchangeFailure, DecryptionException {
		if (key == null) {
			makeKey();
		}
		try {
			AesGcmJce a = new AesGcmJce(key);
			byte[] decrypted = a.decrypt(encryptedMessage, new byte[0]);
			return decrypted;
		} catch (GeneralSecurityException ex) {
			throw new DecryptionException(ex);
		}
	}
	
	private PyScript getPython() throws IOException {
		if (python == null)
			python = new PyScript(SCRIPT_LOCATION, "quantum");
		return python;
	}
}
