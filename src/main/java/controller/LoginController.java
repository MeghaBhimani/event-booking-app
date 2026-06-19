package controller;

import dao.CartDao;
import dao.CartDaoImpl;
import dao.PasswordEncryption;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Cart;
import model.CartItem;
import model.Model;
import model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class LoginController {

    // Admin credentials are fixed by the assignment specification.
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin321";

    @FXML private TextField     name;
    @FXML private PasswordField password;
    @FXML private Label         status;
    @FXML private Button        login;
    @FXML private Button        signup;

    private final Stage stage;
    private final Model model;

    public LoginController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        login.setOnAction(e  -> handleLogin());
        signup.setOnAction(e -> handleSignup());
    }

    private void handleLogin() {
        String enteredName = name.getText().trim();
        String enteredPass = password.getText();

        if (enteredName.isEmpty() || enteredPass.isEmpty()) {
            showError("Username and password are required.");
            clearFields();
            return;
        }

        // Admin path
        if (ADMIN_USERNAME.equals(enteredName) && ADMIN_PASSWORD.equals(enteredPass)) {
            clearFields();
            navigateTo(AdminController.class, "/view/AdminView.fxml",
                    (loader) -> new AdminController(stage, model), "Admin Dashboard");
            return;
        }

        // Normal user path
        try {
            String encryptedPassword = PasswordEncryption.encrypt(enteredPass);
            User user = model.getUserDao().getUser(enteredName, encryptedPassword);

            if (user == null) {
                showError("Invalid username or password.");
            } else {
                model.setCurrentUser(user);
                restoreCart(user.getUsername());
                clearFields();
                navigateToHome();
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }

        clearFields();
    }

    private void handleSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/SignupView.fxml"));
            SignupController ctrl = new SignupController(stage, model);
            loader.setController(ctrl);
            Pane root = loader.load();
            ctrl.showStage(root);
            status.setText("");
            clearFields();
            stage.close();
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/HomeView.fxml"));
            HomeController ctrl = new HomeController(stage, model);
            loader.setController(ctrl);
            Pane root = loader.load();
            ctrl.showStage(root);
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    @FunctionalInterface
    private interface ControllerFactory<T> {
        T create(FXMLLoader loader);
    }

    private void navigateTo(Class<?> controllerClass, String fxmlPath,
                             ControllerFactory<AdminController> factory,
                             String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AdminController ctrl = factory.create(loader);
            loader.setController(ctrl);
            Pane root = loader.load();
            ctrl.showStage(root);
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    private void restoreCart(String userId) {
        try {
            CartDao cartDao = new CartDaoImpl();
            List<CartItem> saved = cartDao.loadCartItems(userId);
            Cart.getInstance().setItems(saved);
        } catch (SQLException ex) {
            // Non-fatal: the user can still shop; their saved cart is just lost.
            System.err.println("Warning: could not restore cart for " + userId
                    + ": " + ex.getMessage());
        }
    }

    private void showError(String message) {
        status.setText(message);
        status.setTextFill(Color.RED);
    }

    private void clearFields() {
        name.clear();
        password.clear();
    }

    public void showStage(Pane root) {
        stage.setScene(new Scene(root));
        stage.setTitle("Login");
        stage.setResizable(false);
        stage.show();
    }
}
