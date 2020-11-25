package network;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class Main extends Application {
	
	public static void main(String[] args) throws Exception {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Simulator sim = new Simulator();

		FlowPane root = new FlowPane();
        
        Button startB = new Button();
        startB.setText("Start");
        root.getChildren().add(startB);
        
        Button stopB = new Button();
        stopB.setText("Stop");
        root.getChildren().add(stopB);
        
        
        
        Scene scene = new Scene(root, 1024, 768);

		stage.setTitle("Network");
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}
	
}
