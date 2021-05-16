package QKDproject.visualization;

import javafx.application.Platform;
import javafx.scene.layout.VBox;

/**
 * Allows a protocol to send info so that the protocol can be visualized. The
 * methods it contains can be called from any thread.
 * @author Marc
 */
public class Visualizer {
	private VBox vis1, vis2;

	public Visualizer(VBox vis1, VBox vis2) {
		this.vis1 = vis1;
		this.vis2 = vis2;
	}
	
	public void addBits(String bits, String bases) {
		Platform.runLater(() -> {
			vis1.getChildren().add(new Qubit(bits, bases));
			vis2.getChildren().add(new Qubit(bits, bases));
		});
	}
	
	public void addMeasurement(String bases, String outcome) {
		
	}
	
}
