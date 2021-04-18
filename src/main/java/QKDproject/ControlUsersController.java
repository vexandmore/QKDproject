package QKDproject;

import javafx.fxml.FXML;
import javafx.scene.layout.*;
import java.util.*;
import java.util.regex.Pattern;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.collections.ListChangeListener.Change;
import javafx.event.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.stage.Stage;

/**
 * JavaFX Controller for the Control Users window.
 * @author Marc
 */
public class ControlUsersController {
	private ObservableList<User> users = FXCollections.observableList(new ArrayList<>());
	private HashMap<User, HashMap<User, Chat>> chatInstances = new HashMap<>();
	private HashMap<Pair<User>, EncryptionParameters> encryptionSettings = new HashMap<>();
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
		//Add HashMaps in chatInstances
		chatInstances.put(newUser, new HashMap<>());
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
			} else {
				//make comm channel and users
				CommunicationChannel channel = new CommunicationChannel();
				//Load guis and make Chat instances
				FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatMockup.fxml"));
				Parent root = loader.load();
				ChatController controller1 = loader.getController();
				Scene scene = new Scene(root);
				Stage stage = new Stage();
				stage.setTitle("Chat window 1: " + u1.getName());
				stage.setScene(scene);
				stage.show();
				//load gui 2
				FXMLLoader loader2 = new FXMLLoader(getClass().getResource("ChatMockup.fxml"));
				Stage stage2 = new Stage();
				Parent root2 = loader2.load();
				ChatController controller2 = loader2.getController();
				Scene scene2 = new Scene(root2);
				stage2.setTitle("Chat window 2: " + u2.getName());
				stage2.setScene(scene2);
				stage2.show();
				//connect it all up
				Chat chat1 = new Chat(u1, u2, protocols[0], controller1, channel);
				Chat chat2 = new Chat(u2, u1, protocols[1], controller2, channel);
				//test code
				/*ChangeListener<String> listener = (ObservableValue<? extends String> val, String oldval, String newval) -> {
					System.out.println(newval);
				};
				chat1.latestMessageProperty().addListener(listener);*/
				//test code
				
				channel.addListener(chat1);
				channel.addListener(chat2);

				chatInstances.get(u1).put(u2, chat1);
				chatInstances.get(u2).put(u1, chat2);
			}
		} catch (Exception e) {
			new Alert(Alert.AlertType.ERROR, "Error creating chat windows: "
					+ e.getMessage()).showAndWait();
		}
	}
}

/**
 * Encapsulates the set of GUI components used to parameterize security.
 * Intended for there to be one instance of these per User.
 * @author Marc
 */
class EncryptionGuis {
	private final ComboBox userSelector, typeSelector;
	private final CheckBox eavesdropperSelector;
	private final TextField securitySelector;
	private final Button applyBtn;
	public final Node[] nodesArr;
	private final static Pattern numberPattern = Pattern.compile("\\d+\\.\\d+|\\d+");
	
	public EncryptionGuis(User thisUser, ObservableList<User> otherUsers) {
		Label thisUserLabel = new Label(thisUser.getName());
		this.userSelector = new ComboBox();
		this.userSelector.setItems(otherUsers);
		typeSelector = new ComboBox();
		typeSelector.setItems(FXCollections.observableArrayList(EncryptionParameters.EncryptionType.values()));
		eavesdropperSelector = new CheckBox();
		applyBtn = new Button("Apply");
		securitySelector = new TextField();
		nodesArr = new Node[] {thisUserLabel, userSelector, typeSelector, 
			securitySelector, eavesdropperSelector, applyBtn};
	}
	
	public void setOnApply(EventHandler<ActionEvent> eh) {
		applyBtn.setOnAction(eh);
	}
	
	public void setOnUserSelect(EventHandler<ActionEvent> eh) {
		userSelector.setOnAction(eh);
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
				(EncryptionParameters.EncryptionType) (typeSelector.getSelectionModel().getSelectedItem()),
				(boolean) eavesdropperSelector.selectedProperty().get(),
				Double.parseDouble(securitySelector.getText())));
	}
	
	public User getSelected() {
		return (User) userSelector.getSelectionModel().getSelectedItem();
	}
}

/**
 * Represents an unordered pair. Cannot contain null elements.
 */
final class Pair<T> {
	public final T u1, u2;
	public Pair(T u1, T u2) {
		this.u1 = Objects.requireNonNull(u1);
		this.u2 = Objects.requireNonNull(u2);
	}
	@Override
	public int hashCode() {
		return u1.hashCode() + u2.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Pair))
			return false;
		final Pair other = (Pair) obj;
		return (u1.equals(other.u1) && u2.equals(other.u2) ||
				u1.equals(other.u2) && u2.equals(other.u1));
	}
}

