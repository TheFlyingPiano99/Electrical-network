module ElectricalNetwork {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;

    opens gui to javafx.fxml;
    exports gui;
}