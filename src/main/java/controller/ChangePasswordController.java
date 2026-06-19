package controller;

import dao.PasswordEncryption;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Model;
import model.User;

import java.sql.SQLException;

public class ChangePasswordController {

    @FXML private PasswordField newPasswordField;
    @FXML private Button        submitButton;
    @FXML private Label         statusLabel;

    private User  user;
    private Model model;

    public void setUser(User user) { this.user = user; }

    public void setModel(Model model) { this.model = model; }

    @FXML
    public void initialize() {
        submitButton.setOnAction(e -> handleChangePassword());
    }

    private void handleChangePassword() {
        String newPassword = newPasswordField.getText();

        if (newPassword.isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        String encryptedPassword = PasswordEncryption.encrypt(newPassword);

        try {
            model.getUserDao().updatePassword(user.getUsername(), encryptedPassword);
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Password updated successfully.");
            // Close the dialog after a brief success message.
            ((Stage) submitButton.getScene().getWindow()).close();
        } catch (SQLException ex) {
            showError("Failed to update password: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        statusLabel.setTextFill(Color.RED);
        statusLabel.setText(message);
    }
}
