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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class Gui extends Application {

    private TableView<Connection> table = new TableView<>();
    private PortParser parser = new PortParser(); // Unser Helfer für die Daten

    @Override
    public void start(Stage stage) {
        stage.setTitle("PortCat v2");

        BorderPane root = new BorderPane();

        ImageView logoView = new ImageView();

        var stream = getClass().getResourceAsStream("/images/logo.png");
        if (stream != null) {
            Image logo = new Image(stream);
            logoView.setImage(logo);

            logoView.setFitHeight(30);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
        } else {
            System.err.println("Logo nicht gefunden: /images/logo.png");
        }

        Label output = new Label("Output:");

        Button btnRefresh = new Button("🔄 Verbindungen aktualisieren");
        btnRefresh.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;"); // Ein bisschen Farbe 🎨
        btnRefresh.setOnMouseEntered(e -> btnRefresh.setStyle("-fx-background-color: #3e4951;  -fx-text-fill: white;"));
        btnRefresh.setOnMouseExited(e -> btnRefresh.setStyle("-fx-background-color: #2196F3;  -fx-text-fill: white;")
        );
        btnRefresh.setOnAction(e -> {
            output.setText("Output: " + ladeDaten());
        });

        Button btnClear = new Button("Clear");
        btnClear.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;"); // Ein bisschen Farbe 🎨
        btnClear.setOnMouseEntered(e -> btnClear.setStyle("-fx-background-color: #631616;  -fx-text-fill: white;"));
        btnClear.setOnMouseExited(e -> btnClear.setStyle("-fx-background-color: #2196F3;  -fx-text-fill: white;")
        );
        btnClear.setOnAction(e -> {
            output.setText("Output: Cleared!");
        });


        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 14, 8, 14));

        logoView.setFitHeight(42);
        logoView.setPreserveRatio(true);

        Region spacerLeft = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);

        Region spacerRight = new Region();
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        topBar.getChildren().addAll(
                logoView,
                spacerLeft,
                spacerRight,
                output,
                btnRefresh,
                btnClear
        );

        HBox bottomBar = new HBox();

        bottomBar.setPrefHeight(42);

        VBox leftPanel = new VBox();
        VBox rightPanel = new VBox();

        leftPanel.setPrefWidth(50);
        rightPanel.setPrefWidth(50);


        root.setLeft(leftPanel);
        root.setRight(rightPanel);
        root.setTop(topBar);
        root.setBottom(bottomBar);
        root.setCenter(table);


        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

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

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);


    // Alle Spalten zur Tabelle hinzufügen
        table.getColumns().addAll(colLocalIP, colLocalPort, colRemoteIP, colRemotePort, colState);
        ladeDaten();
}
    private String ladeDaten() {
        // Hole die Liste aus unserer Parser-Klasse
        List<Connection> datenListe = parser.getConnections();

        // JavaFX braucht eine spezielle "ObservableList", damit die Tabelle das Update mitbekommt
        ObservableList<Connection> observableListe = FXCollections.observableArrayList(datenListe);

        table.setItems(observableListe);
        return (("Daten aktualisiert! Anzahl: " + datenListe.size()));
    }
}

