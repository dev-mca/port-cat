package com.melihcan.portcat;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Gui extends Application {

    private TableView<Connection> table = new TableView<>();
    private PortParser parser = new PortParser(); // Unser Helfer für die Daten

    @Override
    public void start(Stage stage) {
        stage.setTitle("PortCat v1");

        Label label = new Label("Active TCP connections:");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Output text
        Label output = new Label("Output:");
        BorderPane root = new BorderPane();
        root.setBottom(output);
        BorderPane.setAlignment(output, Pos.CENTER);
        BorderPane.setMargin(output, new Insets(10));

        TableColumn<Connection, String> colLocalIP = new TableColumn<>("Lokal IP");
        colLocalIP.setCellValueFactory(new PropertyValueFactory<>("localAddress"));
        colLocalIP.setMinWidth(120);

        TableColumn<Connection, Integer> colLocalPort = new TableColumn<>("Lokal Port");
        colLocalPort.setCellValueFactory(new PropertyValueFactory<>("localPort"));

        TableColumn<Connection, String> colRemoteIP = new TableColumn<>("Remote IP");
        colRemoteIP.setCellValueFactory(new PropertyValueFactory<>("remoteAddress"));
        colRemoteIP.setMinWidth(120);

        TableColumn<Connection, Integer> colRemotePort = new TableColumn<>("Remote Port");
        colRemotePort.setCellValueFactory(new PropertyValueFactory<>("remotePort"));

        TableColumn<Connection, String> colState = new TableColumn<>("Status");
        colState.setCellValueFactory(new PropertyValueFactory<>("state"));

        // table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);


        // Alle Spalten zur Tabelle hinzufügen
        table.getColumns().addAll(colLocalIP, colLocalPort, colRemoteIP, colRemotePort, colState);


        Button btnRefresh = new Button("🔄 Verbindungen aktualisieren");
        btnRefresh.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;"); // Ein bisschen Farbe 🎨
        btnRefresh.setOnMouseEntered(e -> btnRefresh.setStyle("-fx-background-color: #3e4951;  -fx-text-fill: white;"));
        btnRefresh.setOnMouseExited(e ->  btnRefresh.setStyle("-fx-background-color: #2196F3;  -fx-text-fill: white;")
        );
        btnRefresh.setOnAction(e -> {String result = ladeDaten();output.setText("Output: " + result);
        });


        // 4. Alles zusammenpacken (VBox stapelt Elemente untereinander)
        VBox vbox = new VBox(10); // 10 ist der Abstand zwischen den Elementen
        vbox.setPadding(new Insets(15));
        vbox.getChildren().addAll(label, btnRefresh, output, table);

        // 5. Erstes Laden der Daten beim Start
        ladeDaten();

        // 6. Szene erstellen und zeigen
        Scene scene = new Scene(vbox, 600, 400);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    // Hilfsmethode: Holt Daten vom Parser und packt sie in die Tabelle
    private String ladeDaten() {
        // Hole die Liste aus unserer Parser-Klasse
        List<Connection> datenListe = parser.getConnections();

        // JavaFX braucht eine spezielle "ObservableList", damit die Tabelle das Update mitbekommt
        ObservableList<Connection> observableListe = FXCollections.observableArrayList(datenListe);

        table.setItems(observableListe);
        return (("Daten aktualisiert! Anzahl: " + datenListe.size()));
    }
}
