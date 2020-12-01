package main.java.gui;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import javafx.scene.control.TextFormatter;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import main.java.math.Coordinate;
import main.java.network.Component;
import main.java.network.ComponentNode;
import main.java.network.ComponentProperty;
import main.java.network.Network;
import main.java.network.Resistance;
import main.java.network.VoltageSource;
import main.java.network.Wire;

public class MainController {
	
	static MainController mainController = null;
	
	private DrawingHelper helper;
	private Network network = new Network();
	private Component     grabbedComponent = null;
	private ComponentNode grabbedNode = null;
	private Component selectedComponent = null;
	private int idx = 0;
	
	Boolean simulating = null; 

//FXML items:-----------------------------------------------------------------	
	
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
    private Label leftStatus;

    @FXML
    private Font x3;

    @FXML
    private Color x4;

    @FXML
    private Label rightStatus;

    @FXML
    private GridPane propertyGrid;
  
    
//Menu item actions:------------------------------------------------------------------------------------------
    
    /**
     * Show about window.
     * @param event
     */
    @FXML
    void miAboutAction(ActionEvent event) {
    	Dialog dlg = new Alert(AlertType.NONE, "Áramkör szimulátor\nSimon Zoltán, 2020", ButtonType.OK);
    	dlg.show();
    }

    /**
     * Clear canvas.
     * @param event
     */
    @FXML
    void miNewAction(ActionEvent event) {
    	network.clear();
    	helper.updateCanvasContent(xCanvas, network);
    	selectedComponent = null;
    	grabbedNode = null;
    	grabbedComponent = null;
    	simulating = null;
    	destroyPropertyView();
    }

    /**
     * Load from file.
     * Show "open file" dialog, for file selection.
     * @param event
     */
    @FXML
    void miOpenAction(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Fájl megnyitás...");
        fileChooser.getExtensionFilters().addAll(
        		new ExtensionFilter("Az összes fájl", "*.*"),
        		new ExtensionFilter("Szöveges fájlok", "*.txt"));
        
        String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
        fileChooser.setInitialDirectory(new File(currentPath));
        
        File f = fileChooser.showOpenDialog(App.globalStage);
        if (f != null && f.exists()) {
        	String fileName = f.getAbsolutePath();
        	network.load(fileName);
        	network.draw(xCanvas.getGraphicsContext2D());
        }
    }

    /**
     * Quit application.
     * @param event
     */
    @FXML
    void miQuitAction(ActionEvent event) {
    	Platform.exit();
    }

    /**
     * Show "file save as" dialog window.
     * @param event
     */
    @FXML
    void miSaveAction(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Fájl mentés...");
        fileChooser.getExtensionFilters().addAll(
        		new ExtensionFilter("Az összes fájl", "*.*"),
        		new ExtensionFilter("Szöveges fájlok", "*.txt"));
        
        String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
        fileChooser.setInitialDirectory(new File(currentPath));
        
        File f = fileChooser.showSaveDialog(null);
        if (f != null) {
        	String fileName = f.getAbsolutePath();
        	network.save(fileName);
        }
    }

    /**
     * Start simulation.
     * @param event
     */
    @FXML
    void miStartAction(ActionEvent event) {
    	simulating = true;
    	leftStatus.setText("Szimuláció folyamatban.");
    }

    /**
     * Pause simulation.
     * @param event
     */
    @FXML
    void miPauseAction(ActionEvent event) {
    	if (simulating != null && simulating == true) {
        	leftStatus.setText("Szimuláció szüneteltetve.");    		
    	}
    	simulating = false;
    }

    /**
     * Stop simulation.
     * @param event
     */
    @FXML
    void miStopAction(ActionEvent event) {
    	network.reset();
    	simulating = null;
    	leftStatus.setText("Szimuláció leállítva.");    		
    }

    //Button actions:--------------------------------------------------------------------------
    
    /**
     * @see miStartAction
     * @param event
     */
    @FXML
    void btnStartAction(ActionEvent event) {
    	miStartAction(event);
    }

    /**
     * @see miPauseAction
     * @param event
     */
    @FXML
    void btnPauseAction(ActionEvent event) {
    	miPauseAction(event);
    }

    /**
     * @see miStopAction 
     * @param event
     */
    @FXML
    void btnStopAction(ActionEvent event) {
    	miStopAction(event);
    }
    
//Initialize:----------------------------------------------------------------------------------------------
    
    
    /**
     * Initializes javaFX controller.
     * HUN: Inicializálja a javaFX kontrollert.
     */
	public void initialize() {
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
        assert leftStatus != null : "fx:id=\"leftStatus\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert x3 != null : "fx:id=\"x3\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert x4 != null : "fx:id=\"x4\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert rightStatus != null : "fx:id=\"rightStatus\" was not injected: check your FXML file 'windowlayout.fxml'.";

        helper = new DrawingHelper();
        
        mainController = this;
        
        lvLeftListView.getItems().add("Feszültségforrás");
        lvLeftListView.getItems().add("Ellenállás");
        lvLeftListView.getItems().add("Vezeték");
        lvLeftListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
    	leftStatus.setText("Szimuláció leállítva.");    		
    	rightStatus.setText("Hibás kapcsolás!");    		
    	
//Keyboard:---------------------------------------------------------------------------------------
    	
		xCanvas.setOnKeyPressed(
			event-> {
				handleKeyboardPressed(event);
			}
		);
		
		lvLeftListView.setOnKeyPressed(
			event-> {
				handleKeyboardPressed(event);
			}
		);
		
		lvRightListView.setOnKeyPressed(
			event-> {
				handleKeyboardPressed(event);
			}
		);

    	
//Mouse:------------------------------------------------------------------------------------------
    	
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
    	            String str = dragboard.getString();
    	            if (str.equals("Feszültségforrás")) {
    	            	network.dropComponent(new VoltageSource(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	            else if (str.equals("Ellenállás")) {
    	            	network.dropComponent(new Resistance(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	            else if (str.equals("Vezeték")) {
    	            	network.dropComponent(new Wire(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	            selectedComponent = network.getSelected();
    	            destroyPropertyView();
    	            buildPropertyView(selectedComponent);
    	            helper.updateCanvasContent(xCanvas, network);
    	            System.out.println("Successfuly dropped " + dragboard.getString());
    	        } else {
    	            event.setDropCompleted(false);
    	            System.out.println("Failed!");
    	        }
    	        event.consume();        			
    		}
		);
        
        xCanvas.setOnMousePressed(
    		event -> {
    			if (event.getButton() ==  MouseButton.PRIMARY) {
    				Coordinate cursorPos = new Coordinate((int)event.getX(), (int)event.getY());
    				grabbedNode = network.getNodeAtPos(cursorPos);
    				if (grabbedNode != null) {
    					network.grabComponentNode(grabbedNode, cursorPos);
        				helper.updateCanvasContent(xCanvas, network);
    				} else {
    					grabbedComponent = network.getComponentAtPos(cursorPos);
    					if (grabbedComponent != null) {
    						network.grabComponent(grabbedComponent, cursorPos);
            				helper.updateCanvasContent(xCanvas, network);
            				if (null != network.getSelected() &&
            						(selectedComponent == null || selectedComponent != network.getSelected())) {
                				selectedComponent = network.getSelected();
                				destroyPropertyView();
            					buildPropertyView(selectedComponent);
            				}
            				
    					}
    				}
    			}
    		}
        );
        
        xCanvas.setOnMouseDragged(
    		event -> {
    			Coordinate cursorPos = new Coordinate((int)event.getX(), (int)event.getY());
    			if (grabbedNode != null) {
        			System.out.println(String.format("#1 xCanvas MouseMoved %d", System.currentTimeMillis()));
    				network.dragComponentNode(grabbedNode, cursorPos);
    				helper.updateCanvasContent(xCanvas, network);
    			} else if (grabbedComponent != null) {
        			System.out.println(String.format("#2 xCanvas MouseMoved %d", System.currentTimeMillis()));
					network.dragComponent(grabbedComponent, cursorPos);
    				helper.updateCanvasContent(xCanvas, network);
    			}
    	        event.consume();        			
    		}
        );
        

        xCanvas.setOnMouseReleased(
    		event -> {
    			if (grabbedNode != null) {
    				network.releaseComponentNode(grabbedNode);
    				grabbedNode = null;
    				helper.updateCanvasContent(xCanvas, network);
    			} else if (grabbedComponent != null) {
					network.releaseComponent(grabbedComponent);
					grabbedComponent = null;
    				helper.updateCanvasContent(xCanvas, network);
    			}
    		}
        );

        xCanvas.setOnMouseExited(
    		event -> {
    			System.out.println(String.format("xCanvas MouseExited %d", System.currentTimeMillis()));
    		}
        );
        
//Timer:----------------------------------------------------------------------------------------
        
        Duration duration = Duration.millis(50);
        Timeline timeline = new Timeline(new KeyFrame(
            duration,
            ae -> {
        		try {
					if (simulating != null && simulating && network != null) {
						network.simulate(duration);
						if (network.isValid()) {
							rightStatus.setText("Helyes kapcsolás.");
						}
						else {
					    	rightStatus.setText("Hibás kapcsolás!");    		
						}
					}
				} catch (Exception e) {
					System.out.println("simulate error");
					e.printStackTrace();
				}
            }
        ));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
    
//PropertyView:---------------------------------------------------------------------------------
    
	/**
	 * Build property view of the given component.
	 * @param component	{@link Component} to build view of.
	 */
    public void buildPropertyView(Component component) {    	
    	int row = 0;
    	if (component.getProperties() != null) {
    		Insets insets = new Insets(0,0,0,5);
        	for (Entry<String, ComponentProperty> entry : component.getProperties().entrySet()) {
        		ComponentProperty prop = entry.getValue();
        		prop.nameN = new Label(prop.name);
        		prop.nameN.setPadding(insets);
        		propertyGrid.add(prop.nameN, 0, row);
        		
        		prop.valueN = new TextField(prop.value);
        		prop.valueN.setEditable(prop.editable);
        		if (prop.editable) {
        			prop.valueN.textProperty().addListener((observable, oldValue, newValue) -> {
        				if (newValue != null  && !newValue.equals(oldValue)) {
        					prop.value = prop.valueN.getText().trim();
        					component.updatePropertyModel();
        				}
        			});
        		}
        		
        		
        		propertyGrid.add(prop.valueN, 1, row);
        		prop.unitN = new Label(prop.unit);
        		prop.unitN.setPadding(insets);
        		propertyGrid.add(prop.unitN, 2, row);
        		row++;
    		}
    	}
    }
    
    /**
     * Destroy currently active property view.
     */
    public void destroyPropertyView() {
    	Iterator<Node> it = propertyGrid.getChildren().iterator();
    	while (it.hasNext()) {
    		it.next();
			it.remove();
		}
    }    
    
    /**
     * Process keyboardPressed events.
     * @param event
     */
    public void handleKeyboardPressed(KeyEvent event) {
    	switch (event.getCode()) {
    		case ENTER:
    			break;
    		case DELETE:
    			if (selectedComponent != null) {
        			network.removeComponent(selectedComponent);
        			destroyPropertyView();
        			helper.updateCanvasContent(xCanvas, network);
    				selectedComponent = null;
    			}
    			break;
    		case ESCAPE:
    			if (selectedComponent != null) {
    				network.cancelSelection();
        			destroyPropertyView();
        			helper.updateCanvasContent(xCanvas, network);
    				selectedComponent = null;
    			}
    			break;
    		default:
    			break;
    	} 
    }   
    
    
}
