package controller;

import Test.Validation;
import dao.EventDao;
import dao.EventDaoImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Event;
import model.Model;

import java.io.IOException;
import java.util.List;

public class AddEventController {

    @FXML private TextField titleField;
    @FXML private TextField venueField;
    @FXML private TextField dayField;
    @FXML private TextField priceField;
    @FXML private TextField capacityField;
    @FXML private Button    saveButton;
    @FXML private Button    cancelButton;
    @FXML private Label     statusLabel;

    private final Stage stage;
    private final Model model;

    public AddEventController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        saveButton.setOnAction(e   -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }

    private void handleSave() {
        String title       = titleField.getText().trim();
        String venue       = venueField.getText().trim();
        String day         = dayField.getText().trim();
        String priceStr    = priceField.getText().trim();
        String capacityStr = capacityField.getText().trim();

        // All fields are required.
        if (!Validation.isNotEmpty(title)    || !Validation.isNotEmpty(venue)
                || !Validation.isNotEmpty(day) || !Validation.isNotEmpty(priceStr)
                || !Validation.isNotEmpty(capacityStr)) {
            showError("All fields are required.");
            return;
        }

        if (!Validation.isValidPrice(priceStr)) {
            showError("Price must be a non-negative integer.");
            return;
        }

        if (!Validation.isValidCapacity(capacityStr)) {
            showError("Capacity must be a positive integer.");
            return;
        }

        int price    = Integer.parseInt(priceStr);
        int capacity = Integer.parseInt(capacityStr);

        try {
            EventDao eventDao = new EventDaoImpl();
            List<Event> all = eventDao.getAllEventsIncludingDisabled();

            if (Validation.isDuplicateEvent(title, venue, day, all)) {
                showError("An event with this title, venue, and day already exists.");
                return;
            }

            eventDao.createEvent(title, venue, day, price, 0, capacity, true);
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Event \"" + title + "\" added successfully.");
            clearFields();

        } catch (Exception ex) {
            showError("Failed to add event: " + ex.getMessage());
        }
    }

    private void handleCancel() {
        navigateToAdmin();
    }

    private void navigateToAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/AdminView.fxml"));
            AdminController ctrl = new AdminController(
                    (Stage) cancelButton.getScene().getWindow(), model);
            loader.setController(ctrl);
            Pane root = loader.load();
            ctrl.showStage(root);
        } catch (IOException ex) {
            showError("Navigation error: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        statusLabel.setTextFill(Color.RED);
        statusLabel.setText(message);
    }

    private void clearFields() {
        titleField.clear();
        venueField.clear();
        dayField.clear();
        priceField.clear();
        capacityField.clear();
    }
}
