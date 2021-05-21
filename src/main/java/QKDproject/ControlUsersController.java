
package QKDproject;

import QKDproject.util.Pair;
import QKDproject.visualization.*;
import javafx.fxml.FXML;
import javafx.scene.layout.*;
import java.util.*;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.collections.ListChangeListener.Change;
import javafx.event.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.input.*;

/**
 * JavaFX Controller for the Control Users window.
 * @author Marc and Raphael
 */
public class ControlUsersController {
	private ObservableList<User> users = FXCollections.observableList(new ArrayList<>());
	private HashMap<User, HashMap<User, Chat>> chatInstances = new HashMap<>();
	private HashMap<Pair<User>, EncryptionParameters> encryptionSettings = new HashMap<>();
	private HashMap<User, EncryptionGuis> guiComponents = new HashMap<>();
	private HashMap<User, UserWindowController> userWindows = new HashMap<>();
	
	private HashMap<Pair<User>, Visualizer> visualizers = new HashMap<>();
	
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
		//Add HashMap in chatInstances and create user window
		chatInstances.put(newUser, new HashMap<>());
		userWindows.put(newUser, UserWindowController.create(newUser));
		//Create and place GUI components for changing the encryption settings
		EncryptionGuis gui = new EncryptionGuis(newUser, otherUsers);
		guiComponents.put(newUser, gui);
		grid.addRow(numUsers, gui.nodesArr);
		//Make Apply button actually change the encryption settings
		gui.setOnApply(eh -> {
			gui.getState().ifPresentOrElse(params -> {
				User selectedUser = gui.getSelected();
				//Change encryption settings
				encryptionSettings.put(new Pair<>(newUser, selectedUser), params);
				//Change/make chat windows
				setEncryption(params, newUser, selectedUser);
				//Update GUI, in case the reflexive case is selected
				if (newUser.equals(guiComponents.get(selectedUser).getSelected())) {
					guiComponents.get(selectedUser).setState(params);
				}
			}, () -> {
				new Alert(Alert.AlertType.ERROR,
						"Invalid settings for encryption").showAndWait();
			});
		});
                
		//Set the action for the user dropdown
		gui.setOnUserSelect((ActionEvent ae) -> {
			User selected = gui.getSelected();
			//Set gui components to default state or the current encryption 
			//settings between newUser and selected
			if (encryptionSettings.containsKey(new Pair<>(newUser, selected))) {
				guiComponents.get(newUser).setState(encryptionSettings.get(new Pair<>(newUser, selected)));
			} else {
				guiComponents.get(newUser).reset();
			}
		});
                //Set the action to re-open the user window if it has been closed.
                //(This is a mainly a quality of life improvement)
                gui.setOnOpen(eh -> {
                        userWindows.get(newUser).openUserWindow();
                });

	}
	
	private boolean validUsername(String username) {
		if (username.equals(""))
			return false;
		for (User u : users)
			if (u.getName().equals(username))
				return false;
		return true;
	}
        
	private void setEncryption(EncryptionParameters params, User u1, User u2) {
		try {
			Protocol[] protocols = params.makeProtocols();
			if (chatInstances.containsKey(u1) && chatInstances.get(u1).containsKey(u2)) {
				//modify existing chat windows
				chatInstances.get(u1).get(u2).changeProtocol(protocols[0]);
				chatInstances.get(u2).get(u1).changeProtocol(protocols[1]);
				if (protocols[0] instanceof Visualizable && protocols[1] instanceof Visualizable) {
					Visualizer v = visualizers.get(new Pair<>(u1, u2));
					((Visualizable)protocols[0]).setVisualizer(v);
					((Visualizable)protocols[1]).setVisualizer(v);
				}
			} else {
				//make comm channel and users
				CommunicationChannel channel = new CommunicationChannel();
				//Load guis and make Chat instances
				FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatMockup.fxml"));
				Parent root = loader.load();
				ChatController controller1 = loader.getController();				
				
				//load gui 2
				FXMLLoader loader2 = new FXMLLoader(getClass().getResource("ChatMockup.fxml"));
				Parent root2 = loader2.load();
				ChatController controller2 = loader2.getController();
				
				//connect the chats
				Chat chat1 = new Chat(u1, u2, protocols[0], controller1, channel);
				Chat chat2 = new Chat(u2, u1, protocols[1], controller2, channel);
				//add visualizer
				Visualizer v = new Visualizer(controller1.getVisPane(), controller2.getVisPane());
				visualizers.put(new Pair<>(u1, u2), v);
				if (protocols[0] instanceof Visualizable) {
					((Visualizable)protocols[0]).setVisualizer(v);
					((Visualizable)protocols[1]).setVisualizer(v);
				}
				
				//show the windows
				userWindows.get(u1).addChat(root, controller1.backButton, chat1);
				userWindows.get(u2).addChat(root2, controller2.backButton, chat2);
				
				channel.addListener(chat1);
				channel.addListener(chat2);

				chatInstances.get(u1).put(u2, chat1);
				chatInstances.get(u2).put(u1, chat2);
			}
		} catch (Exception e) {
			new Alert(Alert.AlertType.ERROR, "Error creating chat windows: "
					+ e.getMessage()).showAndWait();
			e.printStackTrace();
		}
	}
}

/**
 * Encapsulates the set of GUI components used to configure security.
 * Intended for there to be one instance of these per User.
 * @author Marc
 */
class EncryptionGuis {
	private final ComboBox<User> userSelector;
	private final ComboBox<EncryptionParameters.EncryptionType> typeSelector;
	private final CheckBox eavesdropperSelector;
	private final Slider securitySelector;
	private final Button applyBtn;
        private final Button userWindowBtn;
	public final Node[] nodesArr;
	private final static ObservableList<EncryptionParameters.EncryptionType> encryptionTypes = 
			FXCollections.observableArrayList(EncryptionParameters.EncryptionType.values());
	
	/**
	 * Creates an EncryptionGuis with the given user and list of other users.
	 * @param thisUser Primary user for this GUI.
	 * @param otherUsers Observable list of other users thisUser could talk to.
	 */
	public EncryptionGuis(User thisUser, ObservableList<User> otherUsers) {
		Label thisUserLabel = new Label(thisUser.getName());
		userSelector = new ComboBox<>();
		userSelector.setItems(otherUsers);
		typeSelector = new ComboBox<>();
		typeSelector.setItems(encryptionTypes);
		eavesdropperSelector = new CheckBox();
		applyBtn = new Button("Apply");
        userWindowBtn = new Button("User Window");
				
		securitySelector = new Slider(0, 1, 0.5);
		securitySelector.setVisible(false);
		securitySelector.setShowTickMarks(true);
		securitySelector.setShowTickLabels(true);
		typeSelector.setOnAction(ae -> {
			if (typeSelector.getSelectionModel().getSelectedItem() != null) {
				double maxSecurity = typeSelector.getSelectionModel().getSelectedItem().maxSecurityValue;
				securitySelector.setMax(maxSecurity);
				securitySelector.setMajorTickUnit(maxSecurity / 5);
				securitySelector.setVisible(true);
			}
		});
		nodesArr = new Node[] {thisUserLabel, userSelector, typeSelector, securitySelector, eavesdropperSelector, applyBtn, userWindowBtn};
    }
	
	/**
	 * Set the action that occurs when the apply button is pressed.
	 * @param eh Event handler for the apply button.
	 */
        public void setOnOpen(EventHandler<ActionEvent> eh) {
                userWindowBtn.setOnAction(eh);
        }
        
	public void setOnApply(EventHandler<ActionEvent> eh) {
		applyBtn.setOnAction(eh);
	}
	
	/**
	 * Set the action that occurs when another user to communicate with is selected.
	 * @param eh Event handler.
	 */
	public void setOnUserSelect(EventHandler<ActionEvent> eh) {
		userSelector.setOnAction(eh);
	}
	
	/**
	 * Resets this to an empty state.
	 */
	public void reset() {
		typeSelector.getSelectionModel().clearSelection();
		eavesdropperSelector.selectedProperty().set(false);
		securitySelector.setVisible(false);
	}
	
	/**
	 * Make the GUI components reflect the state of the given parameters.
	 * @param params Encryption parameters to make this display.
	 */
	public void setState(EncryptionParameters params) {
		this.typeSelector.getSelectionModel().select(params.type);
		this.eavesdropperSelector.selectedProperty().set(params.eavesdropped);
		
		double maxSecurity = typeSelector.getSelectionModel().getSelectedItem().maxSecurityValue;
		securitySelector.setMax(maxSecurity);
		securitySelector.setMajorTickUnit(maxSecurity / 5);
		this.securitySelector.setValue(params.security);
		this.securitySelector.setVisible(true);
	}
	
	/**
	 * Get the encryption parameters that this gui components represent (or
	 * an empty Optional if they don't represent valid encryption parameters).
	 * @return Optional of the encryption parameters represented by this, or an empty.
	 */
	public Optional<EncryptionParameters> getState() {
		if (typeSelector.getSelectionModel().getSelectedIndex() == -1
				|| userSelector.getSelectionModel().getSelectedIndex() == -1) {
			return Optional.empty();
		}
		return Optional.of(new EncryptionParameters(
				typeSelector.getSelectionModel().getSelectedItem(),
				eavesdropperSelector.selectedProperty().get(),
				securitySelector.getValue()));
	}
	
	public User getSelected() {
		return userSelector.getSelectionModel().getSelectedItem();
	}
}

