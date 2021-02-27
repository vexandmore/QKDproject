/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QKDproject;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Marc
 */
public class ChatController {
	@FXML public GridPane chatGrid;
	@FXML public ScrollPane scrollPane;
	
	public ChatController() {
	}
	public void initialize() {
		/*ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(50);
		chatGrid.getColumnConstraints().addAll(col1, col1);*/
	}
}
