package QKDproject;

import static QKDproject.QKDAlice.bitStringToArray;
import static QKDproject.QKDAlice.keepAtIndices;
import static QKDproject.QKDAlice.sampleIndices;
import static QKDproject.QKDAlice.removeAtIndices;
import QKDproject.exception.*;
import com.google.crypto.tink.subtle.AesGcmJce;
import java.security.GeneralSecurityException;
import java.util.*;
import java.io.*;

/**
 *
 * @author Marc
 */
public class QKDBob2 implements Protocol {
	private byte[] key;
	private String bob_bases = "", bob_results = "";
	private String bob_matching_measured = "";
	protected QKDChannel alice;
	private static PyScript python;
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
	
	protected void passCircuits(String circuits) throws IOException, KeyExchangeFailure {
		boolean keyMade = false;
		Random rand = new Random();
		
		for (int numAttempts = 0; numAttempts < 5 && !keyMade; numAttempts++) {
			//make measurement bases
			bob_bases = "";
			int bitsSent = (int) ((QKDAlice2.KEY_SIZE * 8 * 2.5) / (1 - (alice.getSecurityLevel() / 100.0)));
			for (int i = 0; i < bitsSent; i++) {
				bob_bases += rand.nextBoolean() ? '1' : '0';
			}
			//make measurements
			String[] pythonOutput = getPython().getResults(bob_bases + " " + circuits).split(" ", 2);
			bob_results = pythonOutput[0];
			if (bob_results.length() != bob_bases.length()) {
				System.out.println("bob results: " + bob_results);
				for (int i = 0; i < 20; i++) {
					System.out.println(python.getResults(""));
				}
				throw new KeyExchangeFailure("Error running python code,"
						+ " result was unexpected length. Verify the anaconda setup.");
			}
			//figure out where we measured in the same basis as alice
			List<Integer> matchingMeasurements = alice.measuredSameIndices(bob_bases);
			bob_matching_measured = keepAtIndices(matchingMeasurements, bob_results);
			//make and compare a sample
			List<Integer> sampleIndices = sampleIndices(alice.getSecurityLevel(), bob_matching_measured.length());
			String bob_sample = keepAtIndices(sampleIndices, bob_matching_measured);
			if (alice.samplesMatch(bob_sample, sampleIndices)) {
				//make the key here
				String bob_key = removeAtIndices(sampleIndices, bob_matching_measured);
				key = bitStringToArray(bob_key, QKDAlice.KEY_SIZE);
				keyMade = true;
			} else {
				//System.out.println("Key not made, eavesdropper or noise");
			}
		}
		if (keyMade)
			return;
		else
			throw new KeyExchangeFailure("Tried 5 times and could not establish a shared key.");
	}
	
	@Override
	public byte[] encryptMessage(byte[] message) throws KeyExchangeFailure, EncryptionException {
		if (key == null) {
			alice.makeKey();
		}
		try {
			AesGcmJce a = new AesGcmJce(key);
			byte[] encrypted = a.encrypt(message, new byte[0]);
			return encrypted;
		} catch (GeneralSecurityException ex) {
			throw new EncryptionException(ex);
		}
	}
	
	@Override
	public byte[] decryptMessage(byte[] encryptedMessage) throws KeyExchangeFailure, DecryptionException {
		if (key == null) {
			alice.makeKey();
		}
		try {
			AesGcmJce a = new AesGcmJce(key);
			byte[] decrypted = a.decrypt(encryptedMessage, new byte[0]);
			return decrypted;
		} catch (GeneralSecurityException ex) {
			throw new DecryptionException(ex);
		}
	}
	
	private static PyScript getPython() throws IOException {
		if (python == null)
			python = new PyScript(SCRIPT_LOCATION, "quantum");
		return python;
	}
}
