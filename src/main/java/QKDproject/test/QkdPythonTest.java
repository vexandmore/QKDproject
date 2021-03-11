package QKDproject.test;

import QKDproject.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Marc
 */
public class QkdPythonTest {
	public static void main(String[] args) {
		try {
			Tester test = new Tester();
			test.testPython();
		} catch (IOException e) {
			
		}
	}
}

class Tester {

	/**
	 * The path of the python script. Determined at runtime.
	 */
	private static String SCRIPT_LOCATION;

	static {
		try {
			SCRIPT_LOCATION = new File(".").getCanonicalPath()
					+ File.separatorChar + "src" + File.separatorChar + "main"
					+ File.separatorChar + "qkdImplementation.py";
		} catch (IOException e) {
			SCRIPT_LOCATION = "error getting script location";
		}
	}
	/**
	 * Shared instance of python script, used to do the quantum simulation.
	 */
	private static PyScript python;
	private int numberFails = 0;
	private int numberSuccesses = 0;

	public Tester() throws IOException {
		python = new PyScript(SCRIPT_LOCATION, "quantum");
	}

	/**
	 * Tests python code while printing to console results.
	 */
	public void testPython() throws IOException {
		String alice_bits = "", alice_bases = "", bob_bases = "", eve_bases = "";
		Random rand = new Random();
		final int bitsSent = 300;
		for (int i = 0; i < bitsSent; i++) {
			alice_bases += rand.nextBoolean() ? '1' : '0';
			alice_bits += rand.nextBoolean() ? '1' : '0';
			eve_bases += rand.nextBoolean() ? '1' : '0';
			bob_bases += rand.nextBoolean() ? '1' : '0';
		}
		System.out.println("Test with eavesdropper");
		String[] results = python.getResults(alice_bits + " " + alice_bases
				+ " " + eve_bases + " " + bob_bases).split(" ");
		String bobResults = results[0];
		String eveResults = results[1];
		assertM(bobResults.length() == eveResults.length(), "results must be same length");

		int numberNotMatching = 0;
		//check if measurements line up when measurement bases line up
		//and that bob's measurements differ when he measures in a different base from Alice
		for (int i = 0; i < bobResults.length(); i++) {
			if (alice_bases.charAt(i) == eve_bases.charAt(i)
					&& eve_bases.charAt(i) == bob_bases.charAt(i)) {
				assertM(bobResults.charAt(i) == eveResults.charAt(i)
						&& bobResults.charAt(i) == alice_bits.charAt(i), "bob and eve"
						+ " must measure the same when they measure in the"
						+ " same base");
			} else {
				if (bobResults.charAt(i) != alice_bits.charAt(i)) {
					numberNotMatching++;
				}
			}
		}
		assertM(numberNotMatching > 0, 
				"bob sometimes measures differently when measures in a different base");
		

		
		System.out.println("Test without eavesdropper");
		alice_bases = "";
		alice_bits = "";
		bob_bases = "";
		for (int i = 0; i < bitsSent; i++) {
			alice_bases += rand.nextBoolean() ? '1' : '0';
			alice_bits += rand.nextBoolean() ? '1' : '0';
			bob_bases += rand.nextBoolean() ? '1' : '0';
		}
		results = python.getResults(alice_bits + " " + alice_bases + " "
				+ bob_bases).split(" ");
		bobResults = results[0];
		assertM(results.length == 1, "should only have measurements for bob");
		assertM(bobResults.length() == alice_bases.length(), "results should be length of input bases");
		
		numberNotMatching = 0;
		//check if measurements line up when measurement bases line up
		//and that bob's measurements sometimes differ when he measures in a different base from Alice
		for (int i = 0; i < bobResults.length(); i++) {
			if (alice_bases.charAt(i) == bob_bases.charAt(i)) {
				assertM(bobResults.charAt(i) == alice_bits.charAt(i), "bob "
						+ " must measure the same as alice encoded when"
						+ " encode and measure in same base");
			} else {
				if (bobResults.charAt(i) != alice_bits.charAt(i)) {
					numberNotMatching++;
				}
			}
		}
		assertM(numberNotMatching > 1, "Bob's measurement should differ from alice's prepared bits in some places");
		
		System.out.println("Test done, " + this.numberFails + " fails and " + 
				this.numberSuccesses + " successes.");
	}

	private void assertM(boolean assertion, String failMessage) {
		if (!assertion) {
			System.out.println(failMessage);
			this.numberFails++;
		} else {
			this.numberSuccesses++;
		}
	}

}
