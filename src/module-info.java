module port.cat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.melihcan.portcat to javafx.fxml;
    exports com.melihcan.portcat;
}
