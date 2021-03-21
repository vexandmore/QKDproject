package QKDproject;

import com.google.crypto.tink.subtle.AesGcmJce;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Protocol that performs quantum key distribution. It is assumed that each
 * Chat instance will have one. This is because it is possible that each QKD 
 * will have a different final key, if there has been eavesdropping and no
 * security check.
 * This class is the "Alice" one, ie it makes and sends the key to the "Bob" 
 * class.
 * @author Marc
 */
public class QKDAlice implements Protocol {
	/**
	 * Key used for encryption.
	 */
	private byte[] key;
	/**
	 * Target final key size in bytes.
	 */
	public final static int KEY_SIZE = 16;
	protected QKDBob other;
	private final boolean eavesdropper;
	private final int securityLevel;
	private String alice_bits = "";
	private String alice_bases = "", eve_bases = "";
	private String alice_sample = "", alice_matching_measured = "";
	//private List<Integer> sampleIndices;
	
	/**
	 * Constructs a QKD with the parameters. Does not immediately determine a
	 * shared key.
	 * @param eavesdropper Whether or not an eavesdropper will intercept
	 * @param securityLevel The security level. Is a percentage of the qubits
	 * that are compared. It effectively makes out at 50 (if it is over 50 half
	 * the measurements will be compared the way it is implemented now).
	 */
	public QKDAlice(boolean eavesdropper, int securityLevel) {
		this.eavesdropper = eavesdropper;
		this.securityLevel = securityLevel;
	}
	
	/**
	 * "Connects" the two QKDs so they can talk to each other. One must be an
	 * Alice and the other not.
	 * @param other 
	 */
	public void connect(QKDBob other) {
		this.other = other;
		other.other = this;
	}
	
	@Override
	public byte[] encryptMessage(byte[] message) {
		if (key == null) {
			other.makeKey();
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
			other.makeKey();
		}
		try {
			AesGcmJce a = new AesGcmJce(key);
			byte[] decrypted = a.decrypt(encryptedMessage, new byte[0]);
			return decrypted;
		} catch (GeneralSecurityException ex) {
			throw new DecryptionFailed(ex);
		}
	}
	
	protected int getSecurityLevel() {
		return securityLevel;
	}
	
	/**
	 * Returns a string of Alice's bits, bases, and possibly Eve's bases (if
	 * there is an eavesdropper) each with a space between them.
	 * @return 
	 */
	protected String getBitsBases() {
		int bitsSent = (int) ((KEY_SIZE * 8 * 2.5) / (1 - (securityLevel / 100.0)));
		Random rand = new Random();
		//initialize alice's bits, bases and bob's bases and eave's bases (if necessary)
		alice_bases = "";
		alice_bits = "";
		eve_bases = "";
		for (int i = 0; i < bitsSent; i++) {
			alice_bases += rand.nextBoolean() ? '1' : '0';
			alice_bits += rand.nextBoolean() ? '1' : '0';
			if (eavesdropper) {
				eve_bases += rand.nextBoolean() ? '1' : '0';
			}
		}
		if (eavesdropper) {
			return alice_bits + " " + alice_bases + " " + eve_bases;
		} else {
			return alice_bits + " " + alice_bases;
		}
	}
	
	/**
	 * Returns the indices where Alice and Bob measured in the same base.
	 * @param bases String representing the bases Bob measured in.
	 * @return List of the indices where Alice and Bob measured in the same base.
	 */
	protected List<Integer> measuredSameIndices(String bases) {
		if (alice_bases.length() != bases.length())
			throw new IllegalArgumentException("bases " + bases + " " + 
					this.alice_bases + " must be same length");
		List<Integer> matchingIndices = new ArrayList<>();
		for (int i = 0; i < bases.length(); i++) {
			if (this.alice_bases.charAt(i) == bases.charAt(i)) {
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
	
	/**
	 * Returns the indices where the Strings have identical characters. Strings
	 * must be of the same length.
	 * @param a First String
	 * @param b Second string
	 * @return List containing indices where the Strings match
	 */
	public static List<Integer> matchingIndices(String a, String b) {
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
	 * at the given indices have been kept. Characters are added in the order 
	 * they are in the list
	 * @param indices List containing the indices to keep
	 * @param str Input string
	 * @return String with chars at the given indices
	 */
	public static String keepAtIndices(List<Integer> indices, String str) {
		if (indices.size() > str.length())
			throw new IllegalArgumentException("index list must not be longer"
					+ "than string");
		if (indices.isEmpty())
			return "";
		StringBuilder out = new StringBuilder();
		indices.forEach((i) -> out.append(str.charAt(i)));
		return out.toString();
	}
	
	/**
	 * Returns a new String consisting of the input string where only characters
	 * at the given indices have been removed. List must be sorted.
	 * @param indices List containing the indices to keep
	 * @param str Input string
	 * @return String with chars at the given indices
	 */
	public static String removeAtIndices(List<Integer> indices, String str) {
		if (indices.size() > str.length())
			throw new IllegalArgumentException("index list must not be longer"
					+ "than string");
		if (indices.isEmpty())
			return str;
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			if (Collections.binarySearch(indices, i) < 0) {
				out.append(str.charAt(i));//append to the string if the index isn't in list
			}
		}
		return out.toString();
	}
	
	/**
	 * Turns a bit string (String of 1s and 0s) into a byte[] of the given length.
	 * @param str Bit string
	 * @param numBytes Number of bytes that will be returned. str.length() must
	 * be at least 8 times this.
	 * @return byte[] representing the bit string.
	 */
	public static byte[] bitStringToArray(String str, int numBytes) {
		if (str.length() / 8 < numBytes)
			throw new IllegalArgumentException("String length " + str.length()
					+ " is too short to make " + numBytes + " bytes.");
		byte[] out = new byte[numBytes];
		
		for (int i = 0; i < out.length; i++) {//for every 8 bits in the string
			for (int j = 0, mul = 128; j < 7 && i * 8 + j < str.length(); j++, mul /= 2) {
				//go through each bit and add to byte[]. Note that this initially
				//multiplies by 128 then 64, etc so that the individual bits
				//are aligned with the bit string.
				out[i] += (str.charAt(i * 8 + j)=='0' ? 0 : 1) * mul;
			}
		}
		return out;
	}
	
	/**
	 * Creates a list of indices that should be compared so that the number
	 * of bits that are compared in a string is approximately equivalent to the
	 * ratio parameter, in percent. 
	 * @param ratio Percentage of indices that will be in the compared bits.
	 * @param length Length of strings that will be compared.
	 * @return 
	 */
	public static List<Integer> sampleIndices(int ratio, int length) {
		if (ratio == 0)
			return Arrays.asList();
		double r = 100.0 / ratio;//figure out every how many bits of the string should be in the sample
		if (r < 2)
			r = 2;//prevent the sample from containing every bit
		int everyN = (int)r;
		List<Integer> out = new ArrayList<>();
		for (int i = 0; i < length; i += everyN) {
			out.add(i);
		}
		return out;
	}
}