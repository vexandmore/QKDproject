package QKDproject;

import QKDproject.exception.*;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import QKDproject.visualization.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Marc
 */
public class QKDAlice2 implements Protocol, Visualizable {
	private Visualizer visualizer;
	private MeasurementSet key;
	private StandardPBEByteEncryptor textEncryptor = new StandardPBEByteEncryptor();
	protected QKDChannel channel;
	public final static int KEY_SIZE = 3;
	private final int securityLevel;
	private String alice_bits, alice_bases, alice_circuits;
	private MeasurementSet alice_sample, alice_matching_measured;
	
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
	/**
	 * Shared instance of python script, used to do the quantum simulation.
	 */
	private static PyScript python;

	public QKDAlice2(int securityLevel) {
		this.securityLevel = securityLevel;
	}
	
	protected int getSecurityLevel() {
		return securityLevel;
	}
	
	protected synchronized void makeKey() throws KeyExchangeFailure {
		if (key != null)
			return;
		boolean keyMade = false;
		try {
			for (int numTries = 1; numTries <= 5 && !keyMade; numTries++) {
				int bitsSent = (int) ((KEY_SIZE * 8 * 2.5) / (1 - (securityLevel / 100.0)));
				alice_bases = "";
				alice_bits = "";
				Random rand = new Random();
				for (int i = 0; i < bitsSent; i++) {
					alice_bases += rand.nextBoolean() ? '1' : '0';
					alice_bits += rand.nextBoolean() ? '1' : '0';
				}
				if (visualizer != null) {
					visualizer.addBits(alice_bits, alice_bases);
				}

				alice_circuits = getPython().getResults(bitsSent + " " + alice_bits + " " + alice_bases);
				keyMade = channel.passCircuitsToBob(alice_circuits);
			}
			if (!keyMade)
				throw new KeyExchangeFailure("Tried 5 times, and could not establish key (eavesdropper)");
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
		alice_matching_measured = new MeasurementSet(alice_bits, matchingIndices);
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
		alice_sample = alice_matching_measured.makeSubstring(sampleIndices);
		if (alice_sample.toString().equals(bobSample)) {
			this.key = alice_sample.complement();
			textEncryptor.setPassword(this.key.toString());
			if (visualizer != null)
				visualizer.addKey(this.key.toString(), this.key.indicesRoot(), "alice");
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public byte[] encryptMessage(byte[] message) throws KeyExchangeFailure, EncryptionException {
		if (key == null) {
			makeKey();
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
			makeKey();
		}
		try {
			return textEncryptor.decrypt(encryptedMessage);
		} catch(EncryptionOperationNotPossibleException e) {
			throw new DecryptionException(e);
		}
	}
	
	private PyScript getPython() throws IOException {
		if (python == null)
			python = new PyScript(SCRIPT_LOCATION, "QiskitEngine");
		return python;
	}

	@Override
	public void setVisualizer(Visualizer v) {
		visualizer = v;
		channel.setVisualizer(v);
	}
}
