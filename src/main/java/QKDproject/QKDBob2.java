package QKDproject;

import QKDproject.exception.*;
import QKDproject.visualization.*;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import java.util.*;
import java.io.*;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

/**
 *
 * @author Marc
 */
public class QKDBob2 implements Protocol, Visualizable {
	private Visualizer visualizer;
	private String key;
	private StandardPBEByteEncryptor textEncryptor = new StandardPBEByteEncryptor();
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
			SCRIPT_LOCATION = new File(".").getCanonicalPath() + File.separatorChar + "qkdImplementation2.py";
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
			bob_matching_measured = Utils.keepAtIndices(matchingMeasurements, bob_results);
			//make and compare a sample
			List<Integer> sampleIndices = Utils.sampleIndices(alice.getSecurityLevel(), bob_matching_measured.length());
			String bob_sample = Utils.keepAtIndices(sampleIndices, bob_matching_measured);
			if (alice.samplesMatch(bob_sample, sampleIndices)) {
				//make the key here
				this.key = Utils.removeAtIndices(sampleIndices, bob_matching_measured);
				this.textEncryptor.setPassword(key);
				System.out.println("b: " + this.key);
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
			return textEncryptor.encrypt(message);
		} catch (EncryptionOperationNotPossibleException e) {
			throw new EncryptionException(e);
		}
	}
	
	@Override
	public byte[] decryptMessage(byte[] encryptedMessage) throws KeyExchangeFailure, DecryptionException {
		if (key == null) {
			alice.makeKey();
		}
		try {
			return textEncryptor.decrypt(encryptedMessage);
		} catch (EncryptionOperationNotPossibleException e) {
			throw new DecryptionException(e);
		}
	}
	
	private static PyScript getPython() throws IOException {
		if (python == null)
			python = new PyScript(SCRIPT_LOCATION, "QiskitEngine");
		return python;
	}
	
	@Override
	public void setVisualizer(Visualizer v) {
		visualizer = v;
	}
}
