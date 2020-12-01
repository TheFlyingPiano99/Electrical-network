package main.java.gui;

import java.net.URL;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class
 * @author Simon Zoltán
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
		final URL url = Paths.get("windowlayout.fxml").toUri().toURL();
		FXMLLoader loader = new FXMLLoader();
		Parent content = loader.load(url);
		stage.setScene(new Scene(content));
		stage.show();
		stage.setTitle("Áramkör szimulátor");
		stage.getScene().setOnKeyPressed(
			event-> {
				if (MainController.mainController != null) {
					MainController.mainController.handleKeyboardPressed(event);
				}
			}
		);
		
		
	}	

}
