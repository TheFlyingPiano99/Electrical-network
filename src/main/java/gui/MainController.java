package main.java.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MainController {
	
	private DrawingHelper helper; 

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private MenuItem miNew;

    @FXML
    private MenuItem miOpen;

    @FXML
    private MenuItem miSave;

    @FXML
    private MenuItem miQuit;

    @FXML
    private MenuItem miStart;

    @FXML
    private MenuItem miPause;

    @FXML
    private MenuItem miStop;

    @FXML
    private MenuItem miAbout;

    @FXML
    private Button btnStart;

    @FXML
    private Button btnPause;

    @FXML
    private Button btnStop;

    @FXML
    private ListView<String> lvLeftListView;

    @FXML
    private Canvas xCanvas;

    @FXML
    private ListView<?> lvRightListView;

    @FXML
    private Label lblPropertiesTitle;

    @FXML
    private Label lblProperties01;

    @FXML
    private TextField txfProperties01;

    @FXML
    private Label lblProperties0102;

    @FXML
    private Label lblProperties02;

    @FXML
    private TextField txfProperties02;

    @FXML
    private Label lblProperties0202;

    @FXML
    private Label lblProperties03;

    @FXML
    private TextField txfProperties03;

    @FXML
    private Label lblProperties0302;

    @FXML
    private Label leftStatus;

    @FXML
    private Font x3;

    @FXML
    private Color x4;

    @FXML
    private Label rightStatus;

    @FXML
    void btnPauseAction(ActionEvent event) {

    }

    @FXML
    void btnStartAction(ActionEvent event) {

    }

    @FXML
    void btnStopAction(ActionEvent event) {

    }

    @FXML
    void miAboutAction(ActionEvent event) {
    	Dialog dlg = new Alert(AlertType.NONE, "Az alkalmazásról...", ButtonType.OK);
    	dlg.show();
    }

    @FXML
    void miNewAction(ActionEvent event) {

    }

    @FXML
    void miOpenAction(ActionEvent event) {

    }

    @FXML
    void miPauseAction(ActionEvent event) {

    }

    @FXML
    void miQuitAction(ActionEvent event) {
    	Platform.exit();
    }

    @FXML
    void miSaveAction(ActionEvent event) {

    }

    @FXML
    void miStartAction(ActionEvent event) {

    }

    @FXML
    void miStopAction(ActionEvent event) {

    }

    @FXML
    void miTest1Action(ActionEvent event) {
    	helper.test1(xCanvas.getGraphicsContext2D());
    }

    @FXML
    void miTest2Action(ActionEvent event) {
    	helper.test2(xCanvas.getGraphicsContext2D());
    }    

    @FXML
    void miTest3Action(ActionEvent event) {
    	helper.test3(xCanvas.getGraphicsContext2D());
    }    
    
    @FXML
    void initialize() {
        assert miNew != null : "fx:id=\"miNew\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert miOpen != null : "fx:id=\"miOpen\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert miSave != null : "fx:id=\"miSave\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert miQuit != null : "fx:id=\"miQuit\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert miStart != null : "fx:id=\"miStart\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert miPause != null : "fx:id=\"miPause\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert miStop != null : "fx:id=\"miStop\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert miAbout != null : "fx:id=\"miAbout\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert btnStart != null : "fx:id=\"btnStart\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert btnPause != null : "fx:id=\"btnPause\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert btnStop != null : "fx:id=\"btnStop\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lvLeftListView != null : "fx:id=\"lvLeftListView\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert xCanvas != null : "fx:id=\"xCanvas\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lvRightListView != null : "fx:id=\"lvRightListView\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lblPropertiesTitle != null : "fx:id=\"lblPropertiesTitle\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lblProperties01 != null : "fx:id=\"lblProperties01\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert txfProperties01 != null : "fx:id=\"txfProperties01\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lblProperties0102 != null : "fx:id=\"lblProperties0102\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lblProperties02 != null : "fx:id=\"lblProperties02\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert txfProperties02 != null : "fx:id=\"txfProperties02\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lblProperties0202 != null : "fx:id=\"lblProperties0202\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lblProperties03 != null : "fx:id=\"lblProperties03\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert txfProperties03 != null : "fx:id=\"txfProperties03\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lblProperties0302 != null : "fx:id=\"lblProperties0302\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert leftStatus != null : "fx:id=\"leftStatus\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert x3 != null : "fx:id=\"x3\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert x4 != null : "fx:id=\"x4\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert rightStatus != null : "fx:id=\"rightStatus\" was not injected: check your FXML file 'windowlayout.fxml'.";
        
        // 
        lvLeftListView.getItems().add("Feszültségforrás");
        lvLeftListView.getItems().add("Ellenállás");
        lvLeftListView.getItems().add("Vezeték");
        lvLeftListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        lvLeftListView.setOnMouseClicked(
    		event ->  {
                ObservableList selectedItems = lvLeftListView.getSelectionModel().getSelectedIndices();
                for(Object o : selectedItems){
                    System.out.println("o = " + o + " (" + o.getClass() + ")");
                }
            }
        );


        lvLeftListView.setOnDragDetected(
        		event -> {
        			System.out.println("src DragDetected");
                    String selectedItem = lvLeftListView.getSelectionModel().getSelectedItem();
    				if (selectedItem != null && !"".equals(selectedItem.trim())) {
    					Dragboard dragboard = lvLeftListView.startDragAndDrop(TransferMode.COPY_OR_MOVE);
    					ClipboardContent content = new ClipboardContent();
    					content.putString(selectedItem);
    					dragboard.setContent(content);
    				}
        			event.consume();
        		}
        );
 
        xCanvas.setOnDragOver(
        		event -> {
        			System.out.println("target DragOver");
        	        Dragboard dragboard = event.getDragboard();
        	        if (dragboard.hasString())
        	        {
        	            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        	        }
        	        event.consume();        			
        		}
		);
    
        xCanvas.setOnDragDropped(
        		event -> {
        			System.out.println("target DragDropped");
        	        Dragboard dragboard = event.getDragboard();
        	        if (dragboard.hasString())
        	        {
        	            event.setDropCompleted(true);
        	            System.out.println("Successfuly dropped " + dragboard.getString());
        	        } else {
        	            event.setDropCompleted(false);
        	            System.out.println("Failed!");
        	        }
        	        event.consume();        			
        		}
		);
        
        helper = new DrawingHelper();
        
        xCanvas.setOnMousePressed(
        		event -> {
        			if (event.getButton() ==  MouseButton.PRIMARY) {
        				helper.grabComponent(
        						xCanvas,
        						(int)event.getX(),
        						(int)event.getY());

        				System.out.println(String.format("xCanvas MousePressed %d", System.currentTimeMillis()));
        			}
        		}
        );

        xCanvas.setOnMouseReleased(
        		event -> {
        			System.out.println(String.format("xCanvas MouseReleased %d", System.currentTimeMillis()));
        		}
        );

        xCanvas.setOnMouseExited(
        		event -> {
        			System.out.println(String.format("xCanvas MouseExited %d", System.currentTimeMillis()));
        		}
        );
        
    }

}
