package QKDproject.visualization;

import QKDproject.util.Pair;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import java.util.*;

/**
 * Allows a protocol to send info so that the protocol can be visualized. The
 * methods it contains can be called from any thread.
 * @author Marc
 */

public class Visualizer {
	private Pair<VBox> vis;
	private Stack<Pair<Qubit>> allBits = new Stack<>();
	
	public Visualizer(VBox vis1, VBox vis2) {
		vis1.spacingProperty().set(5);
		vis2.spacingProperty().set(5);
		vis = new Pair<>(vis1, vis2);
	}
	
	/**
	 * This adds a new box in the visualizer panel, which will represent one
	 * instance of the protocol, with the given initial bits and bases.
	 * @param bits Bits sent
	 * @param bases Bases the bits are encoded in
	 */
	public void addBits(String bits, String bases) {
		Qubit q1 = new Qubit(bits, bases);
		Qubit q2 = new Qubit(bits, bases);
		allBits.add(new Pair<>(q1, q2));
		Platform.runLater(() -> {
			vis.u1.getChildren().add(q1);
			vis.u2.getChildren().add(q2);
		});
	}
	
	/**
	 * This adds a measurement of the bits in the given bases.
	 * @param bases Bases the qubits are measured in
	 * @param outcome The outcome of the measurement
	 */
	public void addMeasurement(String bases, String outcome, String name) {
		Platform.runLater(() -> {
			allBits.peek().u1.addMeasurement(bases, outcome, name);
			allBits.peek().u2.addMeasurement(bases, outcome, name);
		});
	}
	
	/**
	 * Represents the end of the protocol, of a key that has been made.
	 * @param keyBits Bits of the key
	 * @param keyIndices Indices of where, from the measurement, the bits were
	 * taken from. Should be sorted.
	 */
	public void addKey(String keyBits, List<Integer> keyIndices, String name) {
		Platform.runLater(() -> {
			allBits.peek().u1.addKey(keyBits, keyIndices, name);
			allBits.peek().u2.addKey(keyBits, keyIndices, name);
		});
	}
}
