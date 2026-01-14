module port.cat {  // <- dein Modul-Name!
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.melihcan.portcat to javafx.fxml;  // dein Package!
    exports com.melihcan.portcat;
}
