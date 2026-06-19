package controller;

import Test.Validation;
import dao.EventDaoImpl;
import dao.OrderDao;
import dao.OrderDaoImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import model.Cart;
import model.CartItem;
import model.Model;

import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller for the Shopping Cart screen.
 *
 * <p>Allows the user to:
 * <ul>
 *   <li>Review all events added to the cart.</li>
 *   <li>Edit the quantity of any line item inline.</li>
 *   <li>Remove individual items.</li>
 *   <li>Proceed to checkout (confirmation code + day validation).</li>
 * </ul>
 *
 * <p><strong>Key invariant:</strong> every mutation to the UI's
 * {@code cartItems} list is mirrored to the {@link Cart} singleton so that
 * the in-memory state remains consistent with what the user sees. This ensures
 * that logout saves the correct cart and that seat-availability checks use
 * up-to-date data.
 */
public class CartController {

    @FXML private TableView<CartItem>           cartTableView;
    @FXML private TableColumn<CartItem, String> colEventName;
    @FXML private TableColumn<CartItem, String> colDay;
    @FXML private TableColumn<CartItem, Integer> colQuantity;
    @FXML private TableColumn<CartItem, Double>  colPrice;
    @FXML private TableColumn<CartItem, Void>    colRemove;
    @FXML private Label                          totalPriceLabel;
    @FXML private Button                         checkoutButton;
    @FXML private Button                         backbutton;

    private Cart cart;

    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    private Model model;

    @FXML
    public void initialize() {
        colEventName.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDay.setCellValueFactory(new PropertyValueFactory<>("day"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Allow inline quantity editing.
        cartTableView.setEditable(true);
        colQuantity.setCellFactory(TextFieldTableCell.forTableColumn(
                new IntegerStringConverter()));
        colQuantity.setOnEditCommit(this::handleQuantityEdit);

        addRemoveButtonColumn();

        cartTableView.setItems(cartItems);
        checkoutButton.setOnAction(e -> handleCheckout());
        backbutton.setOnAction(e    -> handleBack());
    }

    public void setCart(Cart cart) {
        this.cart = cart;
        cartItems = FXCollections.observableArrayList(cart.getItems());
        cartTableView.setItems(cartItems);
        updateTotalPrice();
    }

    public void setModel(Model model) {
        this.model = model;
    }

    private void handleQuantityEdit(TableColumn.CellEditEvent<CartItem, Integer> event) {
        CartItem item   = event.getRowValue();
        int      newQty = event.getNewValue();
        int      delta  = newQty - item.getQuantity();

        if (newQty <= 0) {
            showAlert("Quantity must be at least 1.");
            cartTableView.refresh();
            return;
        }

        if (!cart.canAddQuantity(item.getEvent(), delta)) {
            showAlert("Not enough seats available for \"" + item.getEvent().getTitle() + "\".");
            cartTableView.refresh();
            return;
        }

        // Update the CartItem (which is the same object held in the Cart map).
        item.setQuantity(newQty);
        updateTotalPrice();
        cartTableView.refresh();
    }

    private void addRemoveButtonColumn() {
        colRemove.setCellFactory(param -> new TableCell<CartItem, Void>() {
            private final Button removeBtn = new Button("Remove");

            {
                removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                removeBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    // Sync removal with the Cart singleton so logout saves correctly.
                    cart.removeItem(item.getEvent().getTitle());
                    cartItems.remove(item);
                    updateTotalPrice();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            // CartItem.getPrice() already returns quantity × unit price.
            total += item.getPrice();
        }
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
        totalPriceLabel.setText("Total: " + currency.format(total));
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert("Your cart is empty.");
            return;
        }

        // Step 1 – Show total and ask for confirmation.
        double total = cartItems.stream().mapToDouble(CartItem::getPrice).sum();
        boolean confirmed = showConfirmation(
                "Order total: " + NumberFormat.getCurrencyInstance(Locale.US).format(total)
                        + "\n\nProceed to payment?");
        if (!confirmed) return;

        // Step 2 – Validate confirmation code.
        String code = promptText("Enter your 6-digit confirmation code:");
        if (!Validation.isValidConfirmationCode(code)) {
            showAlert("Invalid code. Please enter exactly 6 digits (e.g. 230134).");
            return;
        }

        // Step 3 – Validate event days.
        for (CartItem item : cartItems) {
            if (!Validation.isEventDayValid(item.getDay())) {
                showAlert("Cannot book \"" + item.getTitle() + "\" on " + item.getDay()
                        + ".\nOnly events from today onwards can be booked.");
                return;
            }
        }

        // Step 4 – Persist the order.
        try {
            OrderDao orderDao = new OrderDaoImpl();
            int lastOrder     = orderDao.getLastOrderNumber();
            String orderNumber = String.format("%04d", lastOrder + 1);
            orderDao.insertOrder(orderNumber, List.copyOf(cartItems), total,
                    model.getCurrentUserId());
        } catch (SQLException ex) {
            showAlert("Failed to save order: " + ex.getMessage());
            return;
        }

        // Step 5 – Decrement seat counts (transactional; rolls back on failure).
        try {
            new EventDaoImpl().processCheckout(cartItems);
        } catch (SQLException ex) {
            showAlert("Booking failed: " + ex.getMessage()
                    + "\nPlease try again or adjust your cart.");
            return;
        }

        // Step 6 – Clear cart, notify user, then return to Home.
        cart.clear();
        cartItems.clear();
        updateTotalPrice();
        cartTableView.refresh();
        showInfo("Payment successful! Your booking is confirmed.");
        handleBack();
    }

    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/HomeView.fxml"));
            HomeController ctrl = new HomeController(
                    (Stage) backbutton.getScene().getWindow(), model);
            loader.setController(ctrl);
            Pane root = loader.load();
            ctrl.showStage(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message,
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Checkout");
        alert.showAndWait();
        return alert.getResult() == ButtonType.YES;
    }

    private String promptText(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmation Code");
        dialog.setHeaderText(prompt);
        Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
