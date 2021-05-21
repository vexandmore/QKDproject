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
	private MeasurementSet key;
	private StandardPBEByteEncryptor textEncryptor = new StandardPBEByteEncryptor();
	private String bob_bases = "";
	private MeasurementSet bob_results, bob_matching_measured;
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
	
	protected boolean passCircuits(String circuits) throws IOException, KeyExchangeFailure {
		boolean keyMade = false;
		Random rand = new Random();

		//make measurement bases
		bob_bases = "";
		int bitsSent = (int) ((QKDAlice2.KEY_SIZE * 8 * 2.5) / (1 - (alice.getSecurityLevel() / 100.0)));
		for (int i = 0; i < bitsSent; i++) {
			bob_bases += rand.nextBoolean() ? '1' : '0';
		}
		//make measurements
		String[] pythonOutput = getPython().getResults(bob_bases + " " + circuits).split(" ", 2);
		bob_results = new MeasurementSet(pythonOutput[0]);

		if (this.visualizer != null) {
			visualizer.addMeasurement(bob_bases, bob_results.toString());
		}

		if (bob_results.length() != bob_bases.length()) {
			throw new KeyExchangeFailure("Error running python code,"
					+ " result was unexpected length. Verify the anaconda setup.");
		}
		//figure out where we measured in the same basis as alice
		List<Integer> matchingMeasurements = alice.measuredSameIndices(bob_bases);
		bob_matching_measured = bob_results.makeSubstring(matchingMeasurements);
		//make and compare a sample
		List<Integer> sampleIndices = Utils.sampleIndices(alice.getSecurityLevel(), bob_matching_measured.length());
		//String bob_sample = Utils.keepAtIndices(sampleIndices, bob_matching_measured);
		MeasurementSet bob_sample = bob_matching_measured.makeSubstring(sampleIndices);
		if (alice.samplesMatch(bob_sample.toString(), sampleIndices)) {
			//make the key here
			this.key = bob_sample.complement();
			this.textEncryptor.setPassword(key.toString());
			System.out.println("b: " + this.key);

			if (visualizer != null) {
				visualizer.addKey(key.toString(), key.indicesRoot());
			}
			return true;
		} else {
			return false;
		}
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

	private List<Integer> makeSampleIndices(List<Integer> matchingMeasurements, List<Integer> sampleIndices) {
		List<Integer> out = new ArrayList<>();
		for (int i: sampleIndices) {
			out.add(matchingMeasurements.get(i));
		}
		return out;
	}
}
