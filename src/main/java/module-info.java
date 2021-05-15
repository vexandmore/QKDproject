
module QKDproject {
	requires jasypt;
	requires javafx.fxml;
	requires javafx.controls;
	requires javafx.controlsEmpty;
	requires javafx.graphics;
	requires javafx.graphicsEmpty;
	requires javafx.base;
	requires javafx.baseEmpty;
	requires java.logging;
	
	opens QKDproject to javafx.graphics, javafx.fxml;
	exports QKDproject;
}
