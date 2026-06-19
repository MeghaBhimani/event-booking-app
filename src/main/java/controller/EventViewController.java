package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Cart;
import model.Event;

public class EventViewController {

    @FXML private Label     titleLabel;
    @FXML private Label     venueLabel;
    @FXML private Label     dayLabel;
    @FXML private Label     priceLabel;
    @FXML private Label     availableSeatsLabel;
    @FXML private TextField quantityField;
    @FXML private Label     statusLabel;
    @FXML private Button    addToCartButton;

    private Event event;
    private Cart  cart;

    private Stage currentStage;
    private Stage parentStage;

    public void setEvent(Event event) {
        this.event = event;
        this.cart  = Cart.getInstance();

        titleLabel.setText(event.getTitle());
        venueLabel.setText("Venue: " + event.getVenue());
        dayLabel.setText("Day: " + event.getDay());
        priceLabel.setText(String.format("Price: $%.2f", (double) event.getPrice()));
        availableSeatsLabel.setText("Available Seats: " + event.getAvailableSeats());

        boolean canBook = event.isEnabled() && event.getAvailableSeats() > 0;
        addToCartButton.setDisable(!canBook);

        if (!event.isEnabled()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("This event is currently unavailable.");
        } else if (event.getAvailableSeats() == 0) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Sold out.");
        } else {
            statusLabel.setText("");
        }
    }

    public void setStages(Stage currentStage, Stage parentStage) {
        this.currentStage = currentStage;
        this.parentStage  = parentStage;
    }

    @FXML
    private void handleBack() {
        if (currentStage != null) currentStage.close();
        if (parentStage  != null) parentStage.show();
    }

    @FXML
    private void handleAddToCart() {
        String qtyText = quantityField.getText().trim();
        int qty;

        try {
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            showError("Please enter a valid number.");
            return;
        }

        if (qty <= 0) {
            showError("Quantity must be greater than zero.");
            return;
        }

        if (qty > event.getAvailableSeats()) {
            showError("Only " + event.getAvailableSeats() + " seat(s) available.");
            return;
        }

        boolean added = cart.addOrUpdateEvent(event, qty);
        if (added) {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText(qty + " seat(s) added to cart.");
            quantityField.clear();
        } else {
            showError("Not enough seats available (including items already in cart).");
        }
    }

    private void showError(String message) {
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText(message);
    }
}
