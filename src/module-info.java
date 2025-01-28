module ElectricalNetwork {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens gui to javafx.fxml;
    exports gui;
}