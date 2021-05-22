package QKDproject.visualization;

import QKDproject.Utils;
import javafx.geometry.Insets;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import java.util.*;

/**
 *
 * @author Marc
 */
public class Qubit extends GridPane {
	private int rowNum = 0;
	private String bits, bases;
	private int numBits;
	
	public Qubit(String bits, String bases) {
		if (bits.length() != bases.length())
			throw new IllegalArgumentException("bits and bases must be same length");
		else
			numBits = bits.length();
		
		this.setStyle("-fx-border-width: 2; -fx-border-color: black;");
		this.setPadding(new Insets(5,5,5,5));
		this.bits = bits;
		this.bases = changeBasesToZX(bases);
		for (int i = 0; i < bits.length(); i++) {
			this.getColumnConstraints().add(fixedWidthColumn());
			this.add(new Text(this.bits.charAt(i) + ""), i, 0);
			this.add(new Text(this.bases.charAt(i) + ""), i, 1);
		}
		rowNum = 2;
	}
	
	public void addMeasurement(String bases, String result, String name) {
		Text m = new Text("Measurement from " + name);
		this.add(m, 0, rowNum++);
		GridPane.setColumnSpan(m, numBits);
		
		bases = changeBasesToZX(bases);
		
		for (int i = 0; i < bases.length(); i++) {
			Text base = new Text(bases.charAt(i) + "");
			if (this.bases.charAt(i) != bases.charAt(i))
				base.setFill(Color.RED);
			this.add(base, i, rowNum);
		}
		rowNum++;
		
		for (int i = 0; i < result.length(); i++) {
			this.add(new Text(result.charAt(i) + ""), i, rowNum);
		}
		rowNum++;
	}
	
	private ColumnConstraints fixedWidthColumn() {
		ColumnConstraints col = new ColumnConstraints();
		col.setMinWidth(8);
		return col;
	}
	
	/**
	 * Changes the bases into a form suitable to be shown.
	 * @param bases Bitstring of 0s and 1s
	 * @return The String but with 0s replaced with Zs and other characters 
	 * replaced with Xs
	 */
	private String changeBasesToZX(String bases) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < bases.length(); i++) {
			out.append(bases.charAt(i) == '0' ? 'Z' : 'X');
		}
		return out.toString();
	}
	
	/**
	 * Represents the end of the protocol, of a key that has been made.
	 * @param keyBits Bits of the key
	 * @param keyIndices Indices of where, from the measurement, the bits were
	 * taken from. Should be sorted.
	 */
	public void addKey(String keyBits, List<Integer> keyIndices, String name) {
		if (!Utils.isSorted(keyIndices))
			Collections.sort(keyIndices);
		
		Text m = new Text(name + "'s Key");
		this.add(m, 0, rowNum++);
		GridPane.setColumnSpan(m, numBits);
		
		for (int i = 0; i < keyBits.length(); i++) {
			this.add(new Text(keyBits.charAt(i)+""), keyIndices.get(i), rowNum);
		}
		rowNum++;
	}
}