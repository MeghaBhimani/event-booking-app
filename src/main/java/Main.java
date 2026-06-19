// References
// JavaFX Documentation: https://openjfx.io/javadoc/17/
// JDBC / SQLite: https://www.sqlitetutorial.net/sqlite-java/
// JavaFX Scene Builder: https://gluonhq.com/products/scene-builder/

import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Event;
import model.Model;


public class Main extends Application {

    private Model model;
    @Override
    public void init() {
        model = new Model();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create tables if they don't exist yet.
            model.setup();

            // Upsert any new events from events.dat into the database.
            Event.addToDatabase(model.getEventDao());

            // Load and show the Login screen.
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/LoginView.fxml"));
            LoginController loginController = new LoginController(primaryStage, model);
            loader.setController(loginController);
            Pane root = loader.load();
            loginController.showStage(root);

        } catch (Exception e) {
            // Show a minimal error window rather than crashing silently.
            Scene scene = new Scene(new Label("Startup error: " + e.getMessage()));
            primaryStage.setTitle("Error");
            primaryStage.setWidth(600);
            primaryStage.setHeight(200);
            primaryStage.setScene(scene);
            primaryStage.show();
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            Event.writeEventsToFile(model.getEventDao());
            System.out.println("Events saved to events.dat on exit.");
        } catch (Exception e) {
            System.err.println("Warning: could not save events to file: "
                    + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
