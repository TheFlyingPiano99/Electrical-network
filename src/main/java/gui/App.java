package gui;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class
 * 
 * @author Zoltán Simon
 *
 */
public class App extends Application {

	static Stage globalStage;

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		globalStage = stage;
		final URL url = App.class.getResource("/windowlayout.fxml");
		Parent content = FXMLLoader.load(url);
		stage.setScene(new Scene(content));
		stage.show();
		stage.setTitle("Áramkör szimulátor");
		stage.getScene().setOnKeyPressed(
				event -> {
					if (MainController.mainController != null) {
						MainController.mainController.handleKeyboardPressed(event);
					}
				});
		stage.setOnCloseRequest((event) -> {
			MainController.mainController.handleCloseRequest(event);
		});
	}

}
