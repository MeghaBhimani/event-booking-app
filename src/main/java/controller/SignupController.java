package controller;

import dao.PasswordEncryption;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Model;
import model.User;

import java.sql.SQLException;

public class SignupController {

    @FXML private TextField     username;
    @FXML private PasswordField password;
    @FXML private TextField     preferredName;
    @FXML private Button        signupButton;
    @FXML private Button        loginButton;
    @FXML private Label         status;

    private final Stage stage;
    private final Stage parentStage;
    private final Model model;

    public SignupController(Stage parentStage, Model model) {
        this.stage       = new Stage();
        this.parentStage = parentStage;
        this.model       = model;
    }

    @FXML
    public void initialize() {
        signupButton.setOnAction(e -> handleSignup());
        loginButton.setOnAction(e  -> handleBackToLogin());
    }

    private void handleSignup() {
        String usernameText   = username.getText().trim();
        String passwordText   = password.getText();
        String preferredText  = preferredName.getText().trim();

        if (usernameText.isEmpty() || passwordText.isEmpty() || preferredText.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        try {
            String encryptedPassword = PasswordEncryption.encrypt(passwordText);
            User user = model.getUserDao().createUser(
                    usernameText, encryptedPassword, preferredText);

            if (user != null) {
                status.setText("Account created for " + user.getUsername()
                        + ". You can now log in.");
                status.setTextFill(Color.GREEN);
                clearFields();
            } else {
                showError("Could not create account. Please try again.");
            }
        } catch (SQLException ex) {
            // Primary key violation means the username already exists.
            showError("Username already taken. Please choose a different one.");
        }
    }

    private void handleBackToLogin() {
        stage.close();
        parentStage.show();
    }

    private void showError(String message) {
        status.setText(message);
        status.setTextFill(Color.RED);
    }

    private void clearFields() {
        username.clear();
        password.clear();
        preferredName.clear();
    }

    public void showStage(Pane root) {
        stage.setScene(new Scene(root));
        stage.setTitle("Sign Up");
        stage.setResizable(false);
        stage.show();
    }
}
