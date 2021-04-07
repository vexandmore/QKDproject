package QKDproject;

import javafx.fxml.FXML;
import javafx.scene.layout.*;
import java.util.*;
import java.util.regex.Pattern;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.collections.ListChangeListener.Change;
import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * JavaFX Controller for the Control Users window.
 * @author Marc
 */
public class ControlUsersController {
	private ObservableList<User> users = FXCollections.observableList(new ArrayList<>());
	private HashMap<User, HashMap<User, EncryptionParameters>> encryptionSettings = new HashMap<>();
	private HashMap<User, EncryptionGuis> guiComponents = new HashMap<>();
	@FXML private GridPane grid;
	@FXML private TextField usernameField;
	private int numUsers = 0;
	
	public ControlUsersController() {	
	}
	
	@FXML
	private void initialize() {
	}
	
	@FXML
	private void handleKeypress(KeyEvent ke) {
		if (ke.getCode().equals(KeyCode.ENTER))
			newUser();
	}
	
	@FXML
	private void newUser() {
		//Get username
		String username = usernameField.getText().trim();
		usernameField.clear();
		//If given username is empty or corresponds to an existing user, tell user and don't add it.
		if (!validUsername(username)) {
			new Alert(Alert.AlertType.ERROR, "Username is empty or corresponds"
					+ " to an existing user.").showAndWait();
			return;
		}

		//Create User and related Collections [for the dropdown]
		User newUser = new User(username);
		ObservableList<User> otherUsers = FXCollections.observableList(new ArrayList<>(users));
		users.add(newUser);
		users.addListener((Change<? extends User> c) -> {
			if (c.next()) {
				otherUsers.addAll(c.getAddedSubList());
			}
		});

		numUsers++;
		//Create dropdown
		ComboBox<User> userCombo = new ComboBox<>();
		userCombo.setItems(otherUsers);
		//Add HashMap in the encryptionSettings HashMap
		encryptionSettings.put(newUser, new HashMap<>());
		//Create and place GUI components for changing the encryption settings
		EncryptionGuis gui = new EncryptionGuis(newUser, userCombo);
		guiComponents.put(newUser, gui);
		grid.addRow(numUsers, gui.nodesArr);
		//Make Apply button actually change the encryption settings
		gui.setOnApply(eh -> {
			gui.getState().ifPresentOrElse(params -> {
				//Change encryption settings
				encryptionSettings.get(newUser).put(userCombo.getValue(), params);
				encryptionSettings.get(userCombo.getValue()).put(newUser, params);
				//Update GUI, in case the reflexive case is selected
				if (newUser.equals(guiComponents.get(userCombo.getValue()).getSelected())) {
					guiComponents.get(userCombo.getValue()).setState(params);
				}
			}, () -> {
				new Alert(Alert.AlertType.ERROR,
						"Invalid settings for encryption").showAndWait();
			});
		});

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

	}
	
	private boolean validUsername(String username) {
		if (username.equals(""))
			return false;
		for (User u : users)
			if (u.getName().trim().equals(username))
				return false;
		return true;
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
	private final Label thisUserLabel;
	private final ComboBox userSelector, typeSelector;
	private final CheckBox eavesdropperSelector;
	private final TextField securitySelector;
	private final Button applyBtn;
	public final Node[] nodesArr;
	private final static Pattern numberPattern = Pattern.compile("\\d+\\.\\d+|\\d+");
	
	public EncryptionGuis(User thisUser, ComboBox cb) {
		this.thisUserLabel = new Label(thisUser.getName());
		this.userSelector = cb;
		typeSelector = new ComboBox();
		typeSelector.setItems(FXCollections.observableArrayList(EncryptionType.QKD, EncryptionType.QKA));
		eavesdropperSelector = new CheckBox();
		applyBtn = new Button("Apply");
		securitySelector = new TextField();
		nodesArr = new Node[] {thisUserLabel, userSelector, typeSelector, 
			securitySelector, eavesdropperSelector, applyBtn};
	}
	
	public void setOnApply(EventHandler<ActionEvent> eh) {
		applyBtn.setOnAction(eh);
	}
	
	public void reset() {
		typeSelector.getSelectionModel().clearSelection();
		eavesdropperSelector.selectedProperty().set(false);
		securitySelector.clear();
	}
	
	/**
	 * Make the GUI components reflect the state of the given parameters.
	 * @param params Encryption parameters to make this display.
	 */
	public void setState(EncryptionParameters params) {
		this.typeSelector.getSelectionModel().select(params.type);
		this.eavesdropperSelector.selectedProperty().set(params.eavesdropped);
		this.securitySelector.setText(params.security + "");
	}
	
	/**
	 * Get the encryption parameters that this gui components represent (or
	 * an empty Optional if they don't represent valid encryption parameters).
	 * @return Optional of the encryption parameters represented by this, or an empty.
	 */
	public Optional<EncryptionParameters> getState() {
		if (typeSelector.getSelectionModel().getSelectedIndex() == -1
				|| !numberPattern.matcher(securitySelector.getText()).matches()) {
			return Optional.empty();
		}
		return Optional.of(new EncryptionParameters(
				(EncryptionType) (typeSelector.getSelectionModel().getSelectedItem()),
				(boolean) eavesdropperSelector.selectedProperty().get(),
				Double.parseDouble(securitySelector.getText())));
	}
	
	public User getSelected() {
		return (User) userSelector.getSelectionModel().getSelectedItem();
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