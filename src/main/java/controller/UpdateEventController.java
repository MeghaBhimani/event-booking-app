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
import java.sql.SQLException;

public class UpdateEventController {

    @FXML private TextField titleField;
    @FXML private TextField venueField;
    @FXML private TextField dayField;
    @FXML private TextField priceField;
    @FXML private TextField capacityField;
    @FXML private Button    updateButton;
    @FXML private Button    cancelButton;
    @FXML private Label     statusLabel;

    private final Stage    stage;
    private final Model    model;
    private final EventDao eventDao = new EventDaoImpl();

    private Event originalEvent;

    public UpdateEventController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    public void setEvent(Event event) {
        this.originalEvent = event;
        titleField.setText(event.getTitle());
        venueField.setText(event.getVenue());
        dayField.setText(event.getDay());
        priceField.setText(String.valueOf(event.getPrice()));
        capacityField.setText(String.valueOf(event.getTotalTickets()));
    }

    @FXML
    public void initialize() {
        titleField.setEditable(false); // title is the immutable identifier
        updateButton.setOnAction(e -> handleUpdate());
        cancelButton.setOnAction(e -> handleCancel());
    }

    private void handleUpdate() {
        String newVenue    = venueField.getText().trim();
        String newDay      = dayField.getText().trim();
        String priceStr    = priceField.getText().trim();
        String capacityStr = capacityField.getText().trim();

        // All fields required.
        if (!Validation.isNotEmpty(newVenue) || !Validation.isNotEmpty(newDay)
                || !Validation.isNotEmpty(priceStr)
                || !Validation.isNotEmpty(capacityStr)) {
            showError("All fields are required.");
            return;
        }

        // Validate price and capacity using the shared Validation utility.
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

        // Check for duplicate only if the venue or day actually changed.
        boolean keyChanged = !newVenue.equalsIgnoreCase(originalEvent.getVenue())
                || !newDay.equalsIgnoreCase(originalEvent.getDay());

        if (keyChanged) {
            try {
                Event existing = eventDao.getEvent(
                        originalEvent.getTitle(), newVenue, newDay);
                if (existing != null) {
                    showError("An event with this title, venue, and day already exists.");
                    return;
                }
            } catch (SQLException ex) {
                showError("Database error during duplicate check: " + ex.getMessage());
                return;
            }
        }

        try {
            eventDao.updateEvent(
                    originalEvent.getTitle(),
                    originalEvent.getVenue(),
                    originalEvent.getDay(),
                    newVenue, newDay,
                    price,
                    originalEvent.getTicketsSold(),
                    capacity);

            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Event updated successfully.");

        } catch (SQLException ex) {
            showError("Failed to update event: " + ex.getMessage());
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
}
