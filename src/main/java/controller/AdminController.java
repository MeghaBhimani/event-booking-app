package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import model.Event;
import model.EventGroup;
import model.Model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminController {

    @FXML private TreeView<Event> groupedEventsTreeView;
    @FXML private Label           statusLabel;
    @FXML private Button          logoutButton;
    @FXML private Button          viewOrdersButton;
    @FXML private Button          viewAddButton;

    private final Model model;
    private final Stage stage;

    public AdminController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }


    @FXML
    public void initialize() {
        loadGroupedEvents();
        logoutButton.setOnAction(e    -> handleLogout());
        viewOrdersButton.setOnAction(e -> handleViewOrders());
        viewAddButton.setOnAction(e    -> handleAddEvent());

        // Clicking a leaf event opens the edit view.
        groupedEventsTreeView.setOnMouseClicked(mouseEvent -> {
            TreeItem<Event> selected =
                    groupedEventsTreeView.getSelectionModel().getSelectedItem();
            if (selected != null
                    && selected.getValue() != null
                    && isLeafEvent(selected.getValue())) {
                openUpdateEventView(selected.getValue());
            }
        });

        setupCellFactory();
    }


    private void loadGroupedEvents() {
        try {
            List<EventGroup> groups = model.getEventDao().getGroupedEvents();

            // Invisible root — setShowRoot(false) hides this row.
            Event rootSentinel = new Event("ROOT", "", "", 0, 0, 0, true);
            TreeItem<Event> root = new TreeItem<>(rootSentinel);
            root.setExpanded(true);

            for (EventGroup group : groups) {
                // Group header — displays only the shared title.
                Event groupSentinel = new Event("GROUP_NODE", group.getTitle(),
                        "", 0, 0, 0, true);
                TreeItem<Event> groupItem = new TreeItem<>(groupSentinel);
                groupItem.setExpanded(true);

                for (Event ev : group.getVariations()) {
                    groupItem.getChildren().add(new TreeItem<>(ev));
                }
                root.getChildren().add(groupItem);
            }

            groupedEventsTreeView.setRoot(root);
            groupedEventsTreeView.setShowRoot(false);

        } catch (SQLException ex) {
            statusLabel.setText("Failed to load events: " + ex.getMessage());
        }
    }


    private void setupCellFactory() {
        groupedEventsTreeView.setCellFactory(tv -> new TreeCell<>() {

            private final Label  label        = new Label();
            private final Button toggleButton = new Button();
            private final Button deleteButton = new Button("Delete");
            private final Region spacer       = new Region();
            private final HBox   hbox         = new HBox(10, label, spacer,
                    toggleButton, deleteButton);

            {
                HBox.setHgrow(spacer, Priority.ALWAYS);
                hbox.setStyle("-fx-padding: 4px 10px; -fx-alignment: center-left;");

                toggleButton.setOnAction(e -> {
                    Event ev = getTreeItem().getValue();
                    if (ev != null && isLeafEvent(ev)) {
                        toggleEventEnabled(ev);
                    }
                });

                deleteButton.setOnAction(e -> {
                    Event ev = getTreeItem().getValue();
                    if (ev != null && isLeafEvent(ev)) {
                        confirmAndDelete(ev);
                    }
                });
            }

            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || "ROOT".equals(item.getTitle())) {
                    setGraphic(null);
                    setText(null);
                } else if ("GROUP_NODE".equals(item.getTitle())) {
                    // Group header: show title as plain text.
                    setText(item.getVenue()); // getVenue() stores the group title
                    setGraphic(null);
                } else {
                    // Leaf event: render the detail HBox.
                    label.setText(String.format(
                            "Venue: %s  |  Day: %s  |  Price: $%d  |  Capacity: %d  |  %s",
                            item.getVenue(), item.getDay(), item.getPrice(),
                            item.getTotalTickets(),
                            item.isEnabled() ? "Enabled" : "Disabled"));

                    if (item.isEnabled()) {
                        toggleButton.setText("Disable");
                        toggleButton.setStyle(
                                "-fx-background-color: #003a8e; -fx-text-fill: white;");
                    } else {
                        toggleButton.setText("Enable");
                        toggleButton.setStyle(
                                "-fx-background-color: #27ae60; -fx-text-fill: white;");
                    }
                    deleteButton.setStyle(
                            "-fx-background-color: #e74c3c; -fx-text-fill: white;");

                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
    }

    private void toggleEventEnabled(Event ev) {
        boolean newStatus = !ev.isEnabled();
        try {
            model.getEventDao().setEventEnabled(
                    ev.getTitle(), ev.getVenue(), ev.getDay(), newStatus);
            loadGroupedEvents();
            statusLabel.setText("Event " + (newStatus ? "enabled" : "disabled") + ".");
        } catch (SQLException ex) {
            statusLabel.setText("Failed to update event status: " + ex.getMessage());
        }
    }

    /** Shows a confirmation dialog and deletes the event if confirmed. */
    private void confirmAndDelete(Event ev) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete \"" + ev.getTitle() + "\" at " + ev.getVenue()
                + " on " + ev.getDay() + "?");
        confirm.setContentText(
                "This is permanent. Existing bookings will NOT be refunded automatically.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                model.getEventDao().deleteEvent(ev.getTitle(), ev.getVenue(), ev.getDay());
                loadGroupedEvents();
                statusLabel.setText("Event deleted.");
            } catch (SQLException ex) {
                statusLabel.setText("Failed to delete event: " + ex.getMessage());
            }
        }
    }

    private void handleAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/AddEventView.fxml"));
            AddEventController ctrl = new AddEventController(stage, model);
            loader.setController(ctrl);
            Pane root = loader.load();

            Stage current = (Stage) groupedEventsTreeView.getScene().getWindow();
            current.setScene(new Scene(root));
            current.setTitle("Add New Event");
            current.show();
            // Note: loadGroupedEvents() is NOT called here because the scene is
            // being replaced. The tree will be rebuilt when the admin navigates back.
        } catch (IOException ex) {
            statusLabel.setText("Failed to open Add Event view: " + ex.getMessage());
        }
    }

    private void openUpdateEventView(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/UpdateEventView.fxml"));
            UpdateEventController ctrl = new UpdateEventController(stage, model);
            loader.setController(ctrl);
            Pane root = loader.load();
            ctrl.setEvent(event);

            Stage current = (Stage) groupedEventsTreeView.getScene().getWindow();
            current.setScene(new Scene(root));
            current.setTitle("Edit Event");
            current.show();
            // Note: loadGroupedEvents() is NOT called here because the scene is
            // being replaced; the tree is rebuilt on return to the admin view.
        } catch (IOException ex) {
            statusLabel.setText("Failed to open Edit Event view: " + ex.getMessage());
        }
    }

    private void handleViewOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/OrderView.fxml"));
            AdminOrderController ctrl = new AdminOrderController(stage, model);
            loader.setController(ctrl);
            Pane root = loader.load();
            ctrl.loadAllOrders();

            Stage current = (Stage) groupedEventsTreeView.getScene().getWindow();
            current.setScene(new Scene(root));
            current.setTitle("All Orders");
            current.show();
        } catch (IOException ex) {
            statusLabel.setText("Failed to open Orders view: " + ex.getMessage());
        }
    }

    public void handleLogout() {
        model.setCurrentUser(null);
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/LoginView.fxml"));
            LoginController ctrl = new LoginController(stage, model);
            loader.setController(ctrl);
            Pane root = loader.load();
            ctrl.showStage(root);
        } catch (IOException ex) {
            statusLabel.setText("Navigation error: " + ex.getMessage());
        }
    }

    public void showStage(Pane root) {
        stage.setScene(new Scene(root));
        stage.setTitle("Admin Dashboard");
        stage.setResizable(false);
        stage.show();
    }

    private boolean isLeafEvent(Event ev) {
        return !"ROOT".equals(ev.getTitle()) && !"GROUP_NODE".equals(ev.getTitle());
    }
}
