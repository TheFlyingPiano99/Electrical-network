module com.example.electricalnetwork {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens main.java.gui to javafx.fxml;
    exports main.java.gui;
}