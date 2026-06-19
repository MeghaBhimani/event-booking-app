package controller;

import dao.CartDao;
import dao.CartDaoImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Cart;
import model.Event;
import model.Model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HomeController {

    @FXML private Label              welcomeLabel;
    @FXML private TableView<Event>   eventTableView;
    @FXML private Button             logoutButton;
    @FXML private Button             viewCartButton;
    @FXML private Button             viewOrdersButton;
    @FXML private Button             changePasswordButton;
    @FXML private Label              statusLabel;

    private final Stage stage;
    private final Model model;

    public HomeController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + model.getCurrentUser().getPreferredName() + "!");

        setupEventTable();
        loadEvents();

        logoutButton.setOnAction(e         -> handleLogout());
        viewCartButton.setOnAction(e        -> navigateToCart());
        viewOrdersButton.setOnAction(e      -> navigateToOrders());
        changePasswordButton.setOnAction(e  -> openChangePasswordDialog());
    }

    private void setupEventTable() {
        TableColumn<Event, String> titleCol = new TableColumn<>("Event Name");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Event, String> venueCol = new TableColumn<>("Venue");
        venueCol.setCellValueFactory(new PropertyValueFactory<>("venue"));

        TableColumn<Event, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(new PropertyValueFactory<>("day"));

        TableColumn<Event, Integer> priceCol = new TableColumn<>("Price ($)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Event, Integer> soldCol = new TableColumn<>("Sold");
        soldCol.setCellValueFactory(new PropertyValueFactory<>("ticketsSold"));

        TableColumn<Event, Integer> totalCol = new TableColumn<>("Capacity");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalTickets"));

        eventTableView.getColumns().addAll(
                titleCol, venueCol, dayCol, priceCol, soldCol, totalCol);

        // Single click on a row opens the event detail view.
        eventTableView.setRowFactory(tv -> {
            TableRow<Event> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    openEventDetail(row.getItem());
                }
            });
            return row;
        });
    }

    private void loadEvents() {
        try {
            List<Event> events = model.getEventDao().getAllEvents();
            eventTableView.getItems().setAll(events);
        } catch (SQLException ex) {
            statusLabel.setText("Failed to load events: " + ex.getMessage());
        }
    }

    private void openEventDetail(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/EventView.fxml"));
            Pane root = loader.load();

            EventViewController ctrl = loader.getController();
            ctrl.setEvent(event);

            Stage eventStage = new Stage();
            eventStage.setTitle("Event Details");
            eventStage.setScene(new Scene(root));
            ctrl.setStages(eventStage, stage);

            stage.hide();
            eventStage.show();
        } catch (IOException ex) {
            statusLabel.setText("Failed to open event details.");
        }
    }

    private void navigateToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/CartView.fxml"));
            CartController ctrl = new CartController();
            loader.setController(ctrl);
            Pane root = loader.load();

            ctrl.setCart(Cart.getInstance());
            ctrl.setModel(model);

            Stage current = (Stage) eventTableView.getScene().getWindow();
            current.setScene(new Scene(root));
            current.setTitle("Shopping Cart");
            current.show();
        } catch (IOException ex) {
            statusLabel.setText("Failed to open cart.");
        }
    }

    private void navigateToOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/OrderView.fxml"));
            OrderController ctrl = new OrderController(stage, model);
            loader.setController(ctrl);
            Pane root = loader.load();
            ctrl.loadOrders();

            Stage current = (Stage) eventTableView.getScene().getWindow();
            current.setScene(new Scene(root));
            current.setTitle("Order History");
            current.show();
        } catch (IOException ex) {
            statusLabel.setText("Failed to open order history.");
        }
    }

    private void openChangePasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ChangePasswordView.fxml"));
            ChangePasswordController ctrl = new ChangePasswordController();
            loader.setController(ctrl);
            Pane pane = loader.load();

            ctrl.setUser(model.getCurrentUser());
            ctrl.setModel(model);

            Stage dialog = new Stage();
            dialog.setTitle("Change Password");
            dialog.setScene(new Scene(pane));
            dialog.initOwner(stage);
            dialog.show();
        } catch (IOException ex) {
            statusLabel.setText("Failed to open Change Password window.");
        }
    }

    public void handleLogout() {
        try {
            CartDao cartDao = new CartDaoImpl();
            cartDao.saveCartItems(
                    model.getCurrentUserId(), Cart.getInstance().getItems());
        } catch (SQLException ex) {
            // Cart save failure is logged but does not block logout.
            System.err.println("Warning: cart could not be saved on logout: "
                    + ex.getMessage());
        }

        model.setCurrentUser(null);
        Cart.getInstance().clear();

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
        stage.setTitle("Dashboard");
        stage.setResizable(false);
        stage.show();
    }
}
