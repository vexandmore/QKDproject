package QKDproject;

import com.google.crypto.tink.subtle.AesGcmJce;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import static QKDproject.PyUtils.runPythonConda;

/**
 * Protocol that performs quantum key distribution. It is assumed that each
 * Chat instance will have one. This is because it is possible that each QKD 
 * will have a different final key, if there has been eavesdropping and no
 * security check.
 * @author Marc
 */
public class QKD implements Protocol {
	/**
	 * Key used for encryption.
	 */
	private byte[] key;
	/**
	 * Target final key size in bytes.
	 */
	private final static int KEY_SIZE = 16;
	/**
	 * The path of the python script. Determined at runtime by a static block.
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
	private QKD other;
	private final boolean isAlice;
	private boolean eavesdropper;
	private double securityLevel;
	private String alice_bits = "";
	private String alice_bases = "", bob_bases = "", eve_bases = "";
	private String bob_results = "", eve_results = "";
	private String alice_sample = "", bob_sample = "";
	
	/**
	 * Constructs a QKD with the parameters. Does not immediately determine a
	 * shared key.
	 * @param eavesdropper Whether or not an eavesdropper will intercept
	 * @param securityLevel The security level. Is a percentage of the qubits
	 * that are compared.
	 * @param isAlice whether or not this QKD is Alice
	 */
	public QKD(boolean eavesdropper, int securityLevel, boolean isAlice) {
		this.eavesdropper = eavesdropper;
		this.securityLevel = securityLevel;
		this.isAlice = isAlice;
	}
	
	/**
	 * "Connects" the two QKDs so they can talk to each other. One must be an
	 * Alice and the other not.
	 * @param other 
	 */
	public void connect(QKD other) {
		if (!(isAlice ^ other.isAlice)) {
			throw new IllegalArgumentException("Tried to connect two QKDs where "
					+ "both are Alices or neither are.");
		}
		this.other = other;
		other.other = this;
	}
	
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
	public byte[] decryptMessage(byte[] encryptedMessage) {
		if (key == null) {
			makeKey();
		}
		try {
			AesGcmJce a = new AesGcmJce(key);
			byte[] decrypted = a.decrypt(encryptedMessage, new byte[0]);
			return decrypted;
		} catch (GeneralSecurityException ex) {
			System.out.println("Error\n" + ex);
			return null;
		}
	}
	
	/**
	 * Makes a key.
	 */
	public void makeKey() {
		if (isAlice) {
			int bitsSent = (int) (KEY_SIZE * 8 * 2.5 / (1 - (securityLevel / 100))); 
			Random rand = new Random();
			//initialize alice's bits, bases and bob's bases and eave's bases (if necessary)
			for (int i = 0; i < bitsSent; i++) {
				alice_bases += rand.nextBoolean() ? '1' : '0';
				alice_bits += rand.nextBoolean() ? '1' : '0';
				bob_bases += rand.nextBoolean() ? '1' : '0';
				if (eavesdropper)
					eve_bases += rand.nextBoolean() ? '1' : '0';
			}
			//send to python code and get Bob's and Eve's measurements
			if (eavesdropper) {
				try ( BufferedReader in = runPythonConda(SCRIPT_LOCATION, "quantum",
						alice_bits, alice_bases, bob_bases, eve_bases)) {
					bob_results = in.readLine();
					eve_results = in.readLine();
					//System.out.println("eve: " + eve_results);
				} catch (IOException e) {
					System.out.println("ERROR " + e);
				}
			} else {
				//send to python code and get Bob's measurements
				try ( BufferedReader in = runPythonConda(SCRIPT_LOCATION, "quantum",
						alice_bits, alice_bases, bob_bases)) {
					bob_results = in.readLine();
				} catch (IOException e) {
					System.out.println("ERROR " + e);
				}
			}
			
			List<Integer> matchingMeasurements = matchingIndices(alice_bases, bob_bases);
			String aliceKey = keepAtIndices(matchingMeasurements, alice_bits);
			String bobKey = keepAtIndices(matchingMeasurements, bob_results);
			
			System.out.println("alice:" + aliceKey);
			System.out.println("bob:  " + bobKey);
			//send data to other QKD
		} else {
			other.makeKey();
		}
	}
	
	/**
	 * Returns the indices where the Strings have identical characters. Strings
	 * must be of the same length.
	 * @param a First String
	 * @param b Second string
	 * @return List containing indices where the Strings match
	 */
	private static List<Integer> matchingIndices(String a, String b) {
		if (a.length() != b.length())
			throw new IllegalArgumentException("Strings must be same length");
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < a.length(); i++) {
			if (a.charAt(i) == b.charAt(i)) {
				indices.add(i);
			}
		}
		return indices;
	}
	
	/**
	 * Returns a new String consisting of the input string where only characters
	 * at the given indices have been removed.
	 * @param indices List containing the indices to keep
	 * @param str Input string
	 * @return String with chars at the given indices
	 */
	private String keepAtIndices(List<Integer> indices, String str) {
		if (indices.size() > str.length())
			throw new IllegalArgumentException("index list must not be longer"
					+ "than string");
		if (indices.isEmpty())
			return "";
		StringBuilder out = new StringBuilder();
		indices.forEach((i) -> out.append(str.charAt(i)));
		return out.toString();
	}
}
