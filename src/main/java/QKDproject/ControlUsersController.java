package QKDproject;

import javafx.fxml.FXML;
import javafx.scene.layout.*;
import java.util.*;
import java.util.ArrayList;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.collections.ListChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.scene.Node;

/**
 * JavaFX Controller for the Control Users window.
 * @author Marc
 */
public class ControlUsersController {
	private ObservableList<User> users = FXCollections.observableList(new ArrayList<>());
	private HashMap<User, HashMap<User, EncryptionParameters>> encryptionSettings = new HashMap<>();
	private HashMap<User, EncryptionGuis> guiComponents = new HashMap<>();
	@FXML private GridPane grid;
	@FXML private Button addButton;
	private int numUsers = 0;
	
	public ControlUsersController() {	
	}
	
	@FXML
	private void initialize() {
	}
	
	@FXML private void newUser(){
		//Get username
		TextInputDialog dialog = new TextInputDialog();
		dialog.setContentText("Enter user name");
		dialog.showAndWait().ifPresent(username -> {
			//Add user to grid if non-empty username given
			if (username.trim().equals(""))
				return;
			//Create User and related Collections
			User newUser = new User(username);
			ObservableList<User> otherUsers = FXCollections.observableList(new ArrayList<>(users));
			users.add(newUser);
			users.addListener((Change<? extends User> c) -> {
				if (c.next()) {
					otherUsers.addAll(c.getAddedSubList());
				}
			});
			//Create and place dropdown
			numUsers++;
			GridPane.setRowIndex(addButton, numUsers+1);//Move add button
			grid.add(new Label(newUser.getName()), 0, numUsers);
			ComboBox<User> userCombo = new ComboBox<>();
			grid.add(userCombo, 1, numUsers);
			userCombo.setItems(otherUsers);
			//Add HashMap in the encryptionSettings HashMap
			encryptionSettings.put(newUser, new HashMap<>());
			//Create and place GUI components for changing the encryption settings
			guiComponents.put(newUser, new EncryptionGuis(userCombo, encryptionSettings.get(newUser)));
			grid.addRow(numUsers, guiComponents.get(newUser).nodesArr);
			
			//Set the action for the user dropdown
			userCombo.setOnAction((ActionEvent ae) -> {
				User selected = userCombo.getSelectionModel().getSelectedItem();
				//Set gui components to default state or the current encryption 
				//settings between newUser and selected
				if (encryptionSettings.get(newUser).containsKey(selected)) {
					guiComponents.get(newUser).setState(encryptionSettings.get(newUser).get(selected));
				} else {
					guiComponents.get(newUser).reset();
				}
				
			});
		});
	}
}

enum EncryptionType {
		QKD,
		QKA
}

/**
 * Encapsulates the set of GUI components used to parameterize security.
 * Intended for there to be one instance of these per User.
 * @author Marc
 */
class EncryptionGuis {
	private final ComboBox typeSelector;
	private final CheckBox eavesdropperSelector;
	private final TextField securitySelector;
	private final Button applyBtn;
	public final List<Node> nodes;
	public final Node[] nodesArr;
	
	public EncryptionGuis(ComboBox<User> userCombo, HashMap<User, EncryptionParameters> hm) {
		typeSelector = new ComboBox();
		typeSelector.setItems(FXCollections.observableArrayList(EncryptionType.QKD, EncryptionType.QKA));
		eavesdropperSelector = new CheckBox();
		applyBtn = new Button("Apply");
		securitySelector = new TextField();
		nodesArr = new Node[] {typeSelector, securitySelector, eavesdropperSelector, applyBtn};
		nodes = Arrays.asList(nodesArr);
		//Make code to change encryption parameters when apply button is pressed
		applyBtn.setOnAction((eh) -> {
			EncryptionParameters newParams = new EncryptionParameters(
					(EncryptionType)(typeSelector.getSelectionModel().getSelectedItem()), 
					(boolean) eavesdropperSelector.selectedProperty().get(), 
					Double.parseDouble(securitySelector.getText()));
			hm.put(userCombo.getValue(), newParams);
		});
	}
	
	public void reset() {
		typeSelector.getSelectionModel().clearSelection();
		eavesdropperSelector.selectedProperty().set(false);
		securitySelector.clear();
	}
	
	public void setState(EncryptionParameters params) {
		this.typeSelector.getSelectionModel().select(params.type);
		this.eavesdropperSelector.selectedProperty().set(params.eavesdropped);
		this.securitySelector.setText(params.security + "");
	}
	
	public Optional<EncryptionParameters> getState() {
		if (typeSelector.getSelectionModel().getSelectedIndex() == -1
				|| securitySelector.getText().equals("")) {
			return Optional.empty();
		}
		return Optional.of(new EncryptionParameters(
				(EncryptionType) (typeSelector.getSelectionModel().getSelectedItem()),
				(boolean) eavesdropperSelector.selectedProperty().get(),
				Double.parseDouble(securitySelector.getText())));
	}
}

/**
 * Encapsulates the current encryption parameters between two users.
 * @author Marc
 */
class EncryptionParameters {	
	public final EncryptionType type;
	public final boolean eavesdropped;
	public final double security;

	public EncryptionParameters(EncryptionType type, boolean eavesdropped, double security) {
		this.type = type;
		this.eavesdropped = eavesdropped;
		this.security = security;
	}
}