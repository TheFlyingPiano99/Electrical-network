package gui;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import math.Coordinate;
import network.*;

public class MainController {
	
	static MainController mainController = null;
	
	private Network network = new Network();
	private Component     grabbedComponent = null;
	private ComponentNode grabbedNode = null;
	private Component selectedComponent = null;
	AudioPlayer audioPlayer = new AudioPlayer();

	boolean snapToGrid = true;
	Boolean simulating = null;
	double totalTimeSec = 0;

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
	private Slider volumeSlider;

	@FXML
	private Button btnAudioMode;

    @FXML
    private ListView<String> lvLeftListView;

    @FXML
    private Canvas circuitCanvas;

    @FXML
    private Canvas scopeCanvas;

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

	@FXML
	private AnchorPane middlePane;

	@FXML
	private AnchorPane rightTopPane;

	@FXML
	private Button scopeModeToggleBtn;

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
		audioPlayer.startPlayback();
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
		audioPlayer.pausePlayback();
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
		DrawingHelper.resetScope(scopeCanvas);
		audioPlayer.stopPlayback();
		totalTimeSec = 0;
	}

    //Button actions:--------------------------------------------------------------------------

    @FXML
    void btnStartAction(ActionEvent event) { miStartAction(event); }

    @FXML
    void btnPauseAction(ActionEvent event) {
    	miPauseAction(event);
    }

    @FXML
    void btnStopAction(ActionEvent event) {
    	miStopAction(event);
    }

	@FXML
	void btnScopeModeToggleAction(ActionEvent event) { DrawingHelper.toggleScopeMode(); };
    
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
		assert volumeSlider != null : "fx:id=\"volumeSlider\" was not injected: check your FXML file 'windowlayout.fxml'.";
		assert btnAudioMode != null : "fx:id=\"btnAudioMode\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lvLeftListView != null : "fx:id=\"lvLeftListView\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert circuitCanvas != null : "fx:id=\"circuitCanvas\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert scopeCanvas != null : "fx:id=\"scopeCanvas\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert lblPropertiesTitle != null : "fx:id=\"lblPropertiesTitle\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert leftStatus != null : "fx:id=\"leftStatus\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert x3 != null : "fx:id=\"x3\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert x4 != null : "fx:id=\"x4\" was not injected: check your FXML file 'windowlayout.fxml'.";
        assert rightStatus != null : "fx:id=\"rightStatus\" was not injected: check your FXML file 'windowlayout.fxml'.";
		assert middlePane != null : "fx:id=\"middlePane\" was not injected: check your FXML file 'windowlayout.fxml'.";
		assert rightTopPane != null : "fx:id=\"rightTopPane\" was not injected: check your FXML file 'windowlayout.fxml'.";
		assert scopeModeToggleBtn != null : "fx:id=\"scopeModeToggleBtn\" was not injected: check your FXML file 'windowlayout.fxml'.";

        mainController = this;
        
        lvLeftListView.getItems().add("Feszültségforrás");
		lvLeftListView.getItems().add("Szinuszos feszültségforrás");
		lvLeftListView.getItems().add("Négyszög feszültségforrás");
		lvLeftListView.getItems().add("Háromszög feszültségforrás");
		lvLeftListView.getItems().add("Fűrészfog feszültségforrás");
        lvLeftListView.getItems().add("Ellenállás");
        lvLeftListView.getItems().add("Vezeték");
        lvLeftListView.getItems().add("Kondenzátor");
        lvLeftListView.getItems().add("Induktor");
        lvLeftListView.getItems().add("Analóg voltmérő");
        lvLeftListView.getItems().add("Analóg ampermérő");
        lvLeftListView.getItems().add("Áramforrás");
        lvLeftListView.getItems().add("Földelés");
        lvLeftListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
    	leftStatus.setText("Szimuláció leállítva.");    		
    	rightStatus.setText("Hibás kapcsolás!");

		// Canvas Width / Height binding:

		circuitCanvas.widthProperty().bind(middlePane.widthProperty());
		circuitCanvas.heightProperty().bind(middlePane.heightProperty());

		scopeCanvas.widthProperty().bind(rightTopPane.widthProperty());
		scopeCanvas.heightProperty().bind(rightTopPane.heightProperty());

		//Keyboard:
    	
		circuitCanvas.setOnKeyPressed(
			event-> {
				handleKeyboardPressed(event);
			}
		);
		
		lvLeftListView.setOnKeyPressed(
			event-> {
				handleKeyboardPressed(event);
			}
		);


    	
		//Mouse:
    	
        lvLeftListView.setOnMouseClicked(
    		event ->  {
                ObservableList selectedItems = lvLeftListView.getSelectionModel().getSelectedIndices();
                for(Object o : selectedItems){
                    //System.out.println("o = " + o + " (" + o.getClass() + ")");
                }
            }
        );


        lvLeftListView.setOnDragDetected(
    		event -> {
    			//System.out.println("src DragDetected");
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
 
        circuitCanvas.setOnDragOver(
    		event -> {
    			//System.out.println("target DragOver");
    	        Dragboard dragboard = event.getDragboard();
    	        if (dragboard.hasString())
    	        {
    	            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
    	        }
    	        
    	        event.consume();        			
    		}
		);
    
        circuitCanvas.setOnDragDropped(
    		event -> {
       			Dragboard dragboard = event.getDragboard();
    	        if (dragboard.hasString())
    	        {
    	            event.setDropCompleted(true);
    	            String str = dragboard.getString();
    	            if (str.equals("Feszültségforrás")) {
    	            	network.dropComponent(new DCVoltageSource(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
					else if (str.equals("Szinuszos feszültségforrás")) {
						network.dropComponent(new SinusoidalVoltageSource(), new Coordinate((int)event.getX(), (int)event.getY()));
					}
					else if (str.equals("Négyszög feszültségforrás")) {
						network.dropComponent(new SquareVoltageSource(), new Coordinate((int)event.getX(), (int)event.getY()));
					}
					else if (str.equals("Háromszög feszültségforrás")) {
						network.dropComponent(new TriangleVoltageSource(), new Coordinate((int)event.getX(), (int)event.getY()));
					}
					else if (str.equals("Fűrészfog feszültségforrás")) {
						network.dropComponent(new SawtoothVoltageSource(), new Coordinate((int)event.getX(), (int)event.getY()));
					}
    	            else if (str.equals("Ellenállás")) {
    	            	network.dropComponent(new Resistance(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	            else if (str.equals("Vezeték")) {
    	            	network.dropComponent(new Wire(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	            else if (str.equals("Kondenzátor")) {
    	            	network.dropComponent(new Capacitor(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	            else if (str.equals("Induktor")) {
    	            	network.dropComponent(new Inductor(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	            else if (str.equals("Analóg voltmérő")) {
    	            	network.dropComponent(new AnalogVoltmeter(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	            else if (str.equals("Analóg ampermérő")) {
    	            	network.dropComponent(new AnalogeAmmeter(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	            else if (str.equals("Áramforrás")) {
    	            	network.dropComponent(new CurrentSource(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	            else if (str.equals("Földelés")) {
    	            	network.dropComponent(new Ground(), new Coordinate((int)event.getX(), (int)event.getY()));
    	            }
    	         
    	            selectedComponent = network.getSelected();
    	            destroyPropertyView();
    	            buildPropertyView(selectedComponent);
					audioPlayer.setSelectedComponent(selectedComponent);
    	        } else {
    	            event.setDropCompleted(false);
    	            //System.out.println("Failed!");
    	        }
    	        event.consume();        			
    		}
		);
        
        circuitCanvas.setOnMousePressed(
    		event -> {
    			if (event.getButton() ==  MouseButton.PRIMARY) {
    				Coordinate cursorPos = new Coordinate((int)event.getX(), (int)event.getY());
    				grabbedNode = network.getNodeAtPos(cursorPos);
    				if (grabbedNode != null) {
    					network.grabComponentNode(grabbedNode, cursorPos);
    				} else {
    					grabbedComponent = network.getComponentAtPos(cursorPos);
    					if (grabbedComponent != null) {
    						network.grabComponent(grabbedComponent, cursorPos);
            				if (null != network.getSelected() &&
            						(selectedComponent == null || selectedComponent != network.getSelected())) {
                				selectedComponent = network.getSelected();
                				destroyPropertyView();
            					buildPropertyView(selectedComponent);
            				}
							audioPlayer.setSelectedComponent(selectedComponent);
    					}
    				}
    			}
    		}
        );
        
        circuitCanvas.setOnMouseDragged(
    		event -> {
    			Coordinate cursorPos = new Coordinate((int)event.getX(), (int)event.getY());
    			if (grabbedNode != null) {
        			//System.out.println(String.format("#1 xCanvas MouseMoved %d", System.currentTimeMillis()));
    				network.dragComponentNode(grabbedNode, cursorPos);
    			} else if (grabbedComponent != null) {
					//System.out.println(String.format("#2 xCanvas MouseMoved %d", System.currentTimeMillis()));
					network.dragComponent(grabbedComponent, cursorPos);
    			}
    	        event.consume();        			
    		}
        );
        

        circuitCanvas.setOnMouseReleased(
    		event -> {
    			if (grabbedNode != null) {
    				network.releaseComponentNode(grabbedNode);
    				grabbedNode = null;
    			} else if (grabbedComponent != null) {
					network.releaseComponent(grabbedComponent);
					grabbedComponent = null;
					audioPlayer.setSelectedComponent(selectedComponent);
    			}
    		}
        );

        circuitCanvas.setOnMouseExited(
    		event -> {
    			//System.out.println(String.format("xCanvas MouseExited %d", System.currentTimeMillis()));
    		}
        );

		volumeSlider.setMin(0);
		volumeSlider.setMax(100);
		volumeSlider.setBlockIncrement(1);
		volumeSlider.valueProperty().addListener(
			(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
				if (!Objects.equals(oldValue, newValue)) {
					audioPlayer.setMasterVolume((double)newValue / 100.0);
				}
			}
		);
		volumeSlider.setValue(100);
		audioPlayer.setMasterVolume(1);

		btnAudioMode.setOnAction(
			(event) -> {
				AudioPlayer.PlaybackMode newMode = audioPlayer.toogleMode();
				String text = "";
				switch (newMode) {
					case AudioPlayer.PlaybackMode.CURRENT -> text = "    Áram    ";
					case AudioPlayer.PlaybackMode.VOLTAGE_DROP -> text = " Feszültség ";
					case AudioPlayer.PlaybackMode.INPUT_POTENTIAL -> text = "Potenciál (be)";
					case AudioPlayer.PlaybackMode.OUTPUT_POTENTIAL -> text = "Potenciál (ki)";
				}
				btnAudioMode.setText(text);
			}
		);
		btnAudioMode.setText(" Feszültség ");
		audioPlayer.setPlaybackMode(AudioPlayer.PlaybackMode.VOLTAGE_DROP);

		// Timer
        
        Duration duration = Duration.millis(50);
        Timeline timeline = new Timeline(new KeyFrame(
            duration,
            ae -> {
				synchronized (network.getMutexObj()) {
					network.evaluate();
					if (network.isValid()) {
						rightStatus.setText("Helyes kapcsolás.");
					} else {
						rightStatus.setText("Hibás kapcsolás!");
					}
					DrawingHelper.updateCanvasContent(circuitCanvas, network, totalTimeSec, ((simulating != null && simulating) ? duration.toSeconds() : 0));
					DrawingHelper.updateScopeImage(scopeCanvas, network, totalTimeSec, (simulating != null && simulating == Boolean.TRUE));
					if (simulating != null && simulating) {
						totalTimeSec += duration.toSeconds();
					}
				}
            }
        ));



        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        
		DrawingHelper.resetScope(scopeCanvas);

		audioPlayer.initializePlayback();
    }



	//PropertyView:
    
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
							audioPlayer.setSelectedComponent(component);
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
					network.cancelSelection();
        			network.removeComponent(selectedComponent);
        			destroyPropertyView();
					selectedComponent = null;
					audioPlayer.setSelectedComponent(null);
    			}
    			break;
    		case ESCAPE:
    			if (selectedComponent != null) {
    				network.cancelSelection();
        			destroyPropertyView();
    				selectedComponent = null;
					audioPlayer.setSelectedComponent(null);
    			}
    			break;
    		case G:
    			if (snapToGrid || network.isSnapToGrid()) {
    				this.snapToGrid = false;
    				network.setSnapToGrid(false);
				}
    			else if (!snapToGrid && !network.isSnapToGrid()) {
    				this.snapToGrid = true;
    				network.setSnapToGrid(true);
    			}
    			break;
    		default:
    			break;
    	} 
    }

	public void handleCloseRequest(WindowEvent event)
	{
		audioPlayer.terminatePlayback();
		System.out.println("Exiting");
	}

}
