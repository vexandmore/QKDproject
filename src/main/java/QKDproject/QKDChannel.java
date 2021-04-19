package QKDproject;

import static QKDproject.QKDAlice.bitStringToArray;
import static QKDproject.QKDAlice.keepAtIndices;
import static QKDproject.QKDAlice.removeAtIndices;
import static QKDproject.QKDAlice.sampleIndices;
import QKDproject.exception.KeyExchangeFailure;
import java.io.*;
import java.util.List;
import java.util.Random;

/**
 * Eavesdropper. Sits between Alice and Bob.
 * @author Marc
 */
public class QKDChannel {
	private QKDBob2 bob;
	private QKDAlice2 alice;
	private String eve_bases = "", eve_results = "";
	private String eve_key = "";
	private List<Integer> matchingMeasured = null;
	private static PyScript python;
	private boolean eavesdropping;
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

	public QKDChannel(boolean eavesdropping) {
		this.eavesdropping = eavesdropping;
	}
	
	public void setEavesdropping(boolean eavesdropping) {
		this.eavesdropping = eavesdropping;
	}
	
	public void setup(QKDAlice2 alice, QKDBob2 bob) {
		this.alice = alice;
		alice.channel = this;
		this.bob = bob;
		bob.alice = this;
	}
	
	protected void passCircuitsToBob(String circuits) throws KeyExchangeFailure, IOException {
		if (!eavesdropping) {
			bob.passCircuits(circuits);
		} else {
			interceptCircuits(circuits);
		}
	}
	
	private void interceptCircuits(String circuits) throws IOException, KeyExchangeFailure {
		boolean keyMade = false;
		Random rand = new Random();
		//make measurement bases
		eve_bases = "";
		int bitsSent = (int) ((QKDAlice2.KEY_SIZE * 8 * 2.5) / (1 - (alice.getSecurityLevel() / 100.0)));
		for (int i = 0; i < bitsSent; i++) {
			//if (i % 2 == 0) {
			//	eve_bases += "2";
			//} else {
				eve_bases += rand.nextBoolean() ? '1' : '0';
			//}
		}
		//make measurements
		String[] pythonOutput = getPython().getResults(eve_bases + " " + circuits).split(" ", 2);
		eve_results = pythonOutput[0];
		String new_circuits = pythonOutput[1];
		if (eve_results.length() != eve_bases.length()) {
			throw new KeyExchangeFailure("Error running python code,"
					+ " result was unexpected length. Verify the anaconda setup.");
		}
		//Pass on new circuits to Bob
		bob.passCircuits(new_circuits);
	}
	
	private static PyScript getPython() throws IOException {
		if (python == null)
			python = new PyScript(SCRIPT_LOCATION, "quantum");
		return python;
	}
	
	
	
	//Methods Bob calls
	public int getSecurityLevel() {
		return alice.getSecurityLevel();
	}
	public List<Integer> measuredSameIndices(String bases) {
		if (eavesdropping) {
			matchingMeasured = alice.measuredSameIndices(bases);
			return matchingMeasured;
		} else {
			return alice.measuredSameIndices(bases);
		}
	}
	public boolean samplesMatch(String sample, List<Integer> sampleIndices) {
		if (eavesdropping) {
			String eve_matching = QKDAlice.keepAtIndices(matchingMeasured, eve_results);
			this.eve_key = QKDAlice.removeAtIndices(sampleIndices, eve_matching);
			System.out.println("e: " + eve_key);
		}
		return alice.samplesMatch(sample, sampleIndices);
	}
	public void makeKey() throws KeyExchangeFailure {
		alice.makeKey();
	}
}
