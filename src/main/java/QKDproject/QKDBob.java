package QKDproject;

import com.google.crypto.tink.subtle.AesGcmJce;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Represents the receiver end of a QKD.
 * @author Marc
 */
public class QKDBob implements Protocol {
	private byte[] key;
	private String bob_bases = "", bob_results = "", eve_results = "";
	private String bob_matching_measured = "";
	protected QKDAlice other;
	/**
	 * The path of the python script. Determined at runtime.
	 */
	private static String SCRIPT_LOCATION;
	static {
		try {
			SCRIPT_LOCATION = new File(".").getCanonicalPath() + 
					File.separatorChar + "src" + File.separatorChar + "main" + 
					File.separatorChar + "qkdImplementation.py";
		} catch (IOException e) {
			SCRIPT_LOCATION = "error getting script location";
		}
	}
	/**
	 * Shared instance of python script, used to do the quantum simulation.
	 */
	private static PyScript python;
	
	@Override
	public byte[] encryptMessage(byte[] message) {
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
	public byte[] decryptMessage(byte[] encryptedMessage) throws DecryptionFailed {
		if (key == null) {
			makeKey();
		}
		try {
			AesGcmJce a = new AesGcmJce(key);
			byte[] decrypted = a.decrypt(encryptedMessage, new byte[0]);
			return decrypted;
		} catch (GeneralSecurityException ex) {
			throw new DecryptionFailed(ex);
		}
	}
	
	protected void makeKey() {
		int bitsSent = (int) ((QKDAlice.KEY_SIZE * 8 * 2.5) / (1 - (other.getSecurityLevel() / 100.0)));
		Random rand = new Random();
		boolean keyMade = false;
		
		
		while (!keyMade) {
			String aliceData = other.getBitsBases();
			//initialize measurement bases
			bob_bases = "";
			for (int i = 0; i < bitsSent; i++) {
				bob_bases += rand.nextBoolean() ? '1' : '0';
			}
			//call python with alice's data and our bases to get our measurements
			try {
				String[] out = getPython().getResults(aliceData + " "
						+ bob_bases).split(" ");
				bob_results = out[0];
				if (bob_results.length() != bob_bases.length())
					throw new RuntimeException("error running python code");
				if (out.length > 1) {
					eve_results = out[1];
				}
			} catch (IOException| RuntimeException e) {
				System.out.println("ERROR " + e);
				return;
			}
			//figure out where we measured in the same basis as alice
			List<Integer> matchingMeasurements = other.measuredSameIndices(bob_bases);
			bob_matching_measured = QKDAlice.keepAtIndices(matchingMeasurements, bob_results);
			//make and compare a sample
			List<Integer> sampleIndices = QKDAlice.sampleIndices(other.getSecurityLevel(), bob_matching_measured.length());
			String bob_sample = QKDAlice.keepAtIndices(sampleIndices, bob_matching_measured);
			if (other.samplesMatch(bob_sample, sampleIndices)) {
				//make the key here
				String bob_key = QKDAlice.removeAtIndices(sampleIndices, bob_matching_measured);
				key = QKDAlice.bitStringToArray(bob_key, QKDAlice.KEY_SIZE);
				keyMade = true;
			} else {
				//sample shows there was eavesdropping / noise
				System.out.println("Eavesdropper detected! Restarting");
			}
		}
	}
	
	private static PyScript getPython() throws IOException {
		if (python == null)
			python = new PyScript(SCRIPT_LOCATION, "quantum");
		return python;
	}
}