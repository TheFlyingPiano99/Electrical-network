package main.java.gui;

import java.net.URL;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.network.Network;

public class App extends Application {
	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		final URL url = Paths.get("windowlayout.fxml").toUri().toURL();
		FXMLLoader loader = new FXMLLoader();
		Parent content = loader.load(url);
		stage.setScene(new Scene(content));
		stage.show();
		stage.setTitle("Áramkör szimulátor");
	}	

}
