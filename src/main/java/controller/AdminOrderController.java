package controller;

import dao.OrderDao;
import dao.OrderDaoImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Model;
import model.Order;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class AdminOrderController {

    @FXML private TableView<Order>              orderTableView;
    @FXML private TableColumn<Order, String>    colOrderNumber;
    @FXML private TableColumn<Order, String>    colTimestamp;
    @FXML private TableColumn<Order, String>    colEvent;
    @FXML private TableColumn<Order, Integer>   colQuantity;
    @FXML private TableColumn<Order, Double>    colTotal;
    @FXML private Button                        backbutton;
    @FXML private Button                        exportButton;

    private ObservableList<Order> orders;
    private final Model model;
    private final Stage stage;

    public AdminOrderController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        colOrderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colEvent.setCellValueFactory(new PropertyValueFactory<>("title"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("price"));

        exportButton.setOnAction(e -> exportOrdersToFile());
        backbutton.setOnAction(e   -> handleBack());
    }

    public void loadAllOrders() {
        OrderDao orderDao = new OrderDaoImpl();
        try {
            List<Order> orderList = orderDao.getAllOrders();
            orders = FXCollections.observableArrayList(orderList);
            orderTableView.setItems(orders);
        } catch (SQLException ex) {
            System.err.println("Failed to load all orders: " + ex.getMessage());
        }
    }

    private void exportOrdersToFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export All Orders");
        chooser.setInitialFileName("all_orders.txt");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = chooser.showSaveDialog(orderTableView.getScene().getWindow());
        if (file == null) return; // user cancelled

        try {
            OrderDao orderDao = new OrderDaoImpl();
            List<Order> orderList = orderDao.getAllOrders();

            try (PrintWriter writer = new PrintWriter(file)) {
                for (Order order : orderList) {
                    writer.printf("%s,%s,%s,%s,%d,%.2f%n",
                            order.getOrderNumber(),
                            order.getUserId() != null ? order.getUserId() : "unknown",
                            order.getTimestamp(),
                            order.getTitle(),
                            order.getQuantity(),
                            order.getPrice());
                }
            }

            showInfo("Orders exported successfully to:\n" + file.getAbsolutePath());

        } catch (IOException | SQLException ex) {
            showError("Export failed: " + ex.getMessage());
        }
    }

    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/AdminView.fxml"));
            AdminController ctrl = new AdminController(stage, model);
            loader.setController(ctrl);
            Pane root = loader.load();
            ctrl.showStage(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Complete");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Export Failed");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
