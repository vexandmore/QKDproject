package QKDproject.visualization;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 *
 * @author Marc
 */
public class Qubit extends GridPane {
	private int rowNum = 0;
	
	public Qubit(String bits, String bases) {
		
		if (bits.length() != bases.length())
			throw new IllegalArgumentException("bits and bases must be same length");
		for (int i = 0; i < bits.length(); i++) {
			char bit = bases.charAt(i) == '0' ? bits.charAt(i) : bits.charAt(i) == '0' ? '+' : '-';
			this.getColumnConstraints().add(fixedWidthColumn());
			this.add(new Text(bit + ""), i, rowNum);
		}
		rowNum++;
	}
	
	private ColumnConstraints fixedWidthColumn() {
		ColumnConstraints col = new ColumnConstraints();
		col.setMinWidth(8);
		return col;
	}
}