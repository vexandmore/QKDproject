package QKDproject.test;

import QKDproject.*;
import java.io.*;
import java.util.*;

/**
 * Tests the python code for qkd2. Verifies that when alice prepares in the same 
 * bases Bob measures that the measure outcome is correct, and that the number
 * of bases not matching falls within the expected range.
 * @author Marc
 */
public class Qkd2PythonTest {
	public static void main(String[] args) {
		try {
			Tester2 test = new Tester2();
			test.testPython();
		} catch (IOException e) {
			System.err.println("Test failed: " + e.getLocalizedMessage());
		}
	}
}

class Tester2 {

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
	private int numberFails = 0;
	private int numberSuccesses = 0;

	public Tester2() throws IOException {
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
		String aliceCircuits = python.getResults(alice_bits.length() + " " + alice_bits + " " + alice_bases);
		String[] eve = python.getResults(eve_bases + " " + aliceCircuits).split(" ", 2);
		String eveResults = eve[0];
		String eveCircuits = eve[1];
		String bobResults = python.getResults(bob_bases + " " + eveCircuits).split(" ", 2)[0];//bob throws away the new circuits
		assertM(bobResults.length() == eveResults.length(), "results must be same length");

		int numNotMatchingMeasurements = 0;
		int numNotMatchingBases = 0;
		//check if measurements line up when measurement bases line up
		//and that bob's measurements differ sometimes when he measures in a different base from Alice
		for (int i = 0; i < bobResults.length(); i++) {
			if (alice_bases.charAt(i) == eve_bases.charAt(i)
					&& eve_bases.charAt(i) == bob_bases.charAt(i)) {
				assertM(bobResults.charAt(i) == eveResults.charAt(i)
						&& bobResults.charAt(i) == alice_bits.charAt(i), "bob and eve"
						+ " must measure the same as alice prepared when they "
						+ "measure in the same base");
			} else {
				numNotMatchingBases++;
				if (bobResults.charAt(i) != alice_bits.charAt(i)) {
					numNotMatchingMeasurements++;
				}
			}
		}
		assertM(numNotMatchingMeasurements > 0, 
				"bob sometimes measures differently when measures in a different base");
		assertM(numNotMatchingBases > 0, "There should be some measurements in a adifferent base");
		assertM(Math.abs(numNotMatchingBases/(bitsSent+0.0) - 0.75) < 0.1, 
				"measurement bases should be different about 3/4 the time");

		
		
		System.out.println("Test without eavesdropper");
		alice_bases = "";
		alice_bits = "";
		bob_bases = "";
		for (int i = 0; i < bitsSent; i++) {
			alice_bases += rand.nextBoolean() ? '1' : '0';
			alice_bits += rand.nextBoolean() ? '1' : '0';
			bob_bases += rand.nextBoolean() ? '1' : '0';
		}
		
		aliceCircuits = python.getResults(alice_bits.length() + " " + alice_bits + " " + alice_bases);
		bobResults = python.getResults(bob_bases + " " + aliceCircuits).split(" ", 2)[0];//bob throws away his new circuits
		assertM(bobResults.length() == alice_bases.length(), "results should be length of input bases");
		
		numNotMatchingMeasurements = 0;
		numNotMatchingBases = 0;
		//check if measurements line up when measurement bases line up
		//and that bob's measurements sometimes differ when he measures in a different base from Alice
		for (int i = 0; i < bobResults.length(); i++) {
			if (alice_bases.charAt(i) == bob_bases.charAt(i)) {
				assertM(bobResults.charAt(i) == alice_bits.charAt(i), "bob "
						+ " must measure the same as alice encoded when"
						+ " encode and measure in same base");
			} else {
				numNotMatchingBases++;
				if (bobResults.charAt(i) != alice_bits.charAt(i)) {
					numNotMatchingMeasurements++;
				}
			}
		}
		assertM(numNotMatchingMeasurements > 1, "Bob's measurement should differ from alice's prepared bits in some places");
		assertM(numNotMatchingBases > 0, "There should be some measurements in a adifferent base");
		assertM(Math.abs(numNotMatchingBases/(bitsSent+0.0) - 0.5) < 0.1, 
				"measurement bases should be different about 1/2 the time");
		
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
