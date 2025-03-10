module ElectricalNetwork {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires java.desktop;

    opens gui to javafx.fxml;
    exports gui;
}