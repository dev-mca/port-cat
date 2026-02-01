package com.melihcan.portcat;

import javafx.application.Application;

import javafx.collections.*;

import javafx.concurrent.Task;

import javafx.geometry.*;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.layout.*;

import javafx.stage.Stage;

import javax.swing.*;
import java.util.List;

public class Gui extends Application {

    /* ---------------------------------------------------
        Felder
     --------------------------------------------------- */
    private final TableView<Connection> table = new TableView<>();
    private final PortParser parser = new PortParser();
    private Scheduler scheduler;
    private Label output;
    private final Label placeholder = new Label();


    /* ---------------------------------------------------
        Start/Einstieg
     --------------------------------------------------- */
    @Override
    public void start(Stage stage) {
        stage.setTitle("PortCat v2");

        /* ---------------------------------------------------
           Logo
        --------------------------------------------------- */
        ImageView logoView = new ImageView();
        var stream = getClass().getResourceAsStream("/images/logo.png");
        if (stream != null) {
            Image logo = new Image(stream);
            logoView.setImage(logo);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            logoView.setFitHeight(150); // kompakt
        } else {
            System.err.println("Logo not found in path!: /images/logo.png");
        }

        /* ---------------------------------------------------
           Labels & Buttons
        --------------------------------------------------- */
        output = new Label("Output: ");
        output.setStyle("-fx-text-fill: #fffbfc; -fx-font-weight: bold; -fx-font-size: 17.5px;");

        Label title = new Label("PortCat - TCP-Connections");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: #FFFBFC;");


        Button btnClear = new Button("Clear");
        btnClear.setPrefSize(95, 40);
        styleButton(btnClear, "#F44336", "#D32F2F", "#FFFBFC");  // jetzt wirklich rot!
        btnClear.setOnAction(e -> clearBtnLogic());

        /* ---------------------------------------------------
           TopBar
        --------------------------------------------------- */
        HBox topBar = new HBox();
        topBar.setMinHeight(85);
        topBar.setPrefHeight(85);
        topBar.setAlignment(Pos.CENTER);

        // links: Logo + Titel
        HBox leftPart = new HBox(10, logoView, title);
        leftPart.setAlignment(Pos.CENTER_LEFT);

        // rechts: Output + Buttons
        HBox rightPart = new HBox(10, output, btnClear);
        rightPart.setAlignment(Pos.CENTER_RIGHT);

        // Spacer in der Mitte
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(leftPart, spacer, rightPart);

        /* ---------------------------------------------------
           Root-Layout
        --------------------------------------------------- */
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #30332E");
        root.setPadding(new Insets(20, 50, 50, 50)); // top, right, bottom, left
        root.setTop(topBar);
        root.setCenter(table);


        /* ---------------------------------------------------
           Tabellen-Spalten
        --------------------------------------------------- */
        addTableColumns();
        placeholder.setText("Keine Verbindungen gefunden!");
        table.setPlaceholder(placeholder);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);


        /* ---------------------------------------------------
           Scene
        --------------------------------------------------- */
        Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.centerOnScreen();
        stage.show();

        // erste Datenladung
        List<Connection> daten = ladeDaten(); // nur Daten holen
        updateUi(daten);                      // UI initialisieren

        //scheduler starten
        scheduler = new Scheduler(this);
        scheduler.start();
    }

    /* ---------------------------------------------------
        Hilfsmethoden
     --------------------------------------------------- */
    private void styleButton(Button btn,
                             String normalColor,
                             String hoverColor,
                             String textColor) {

        // Basis-Style (Schriftgrösse & Gewicht)
        String base = "-fx-font-size: 17.5px; -fx-font-weight: bold;";

        // Grundzustand
        btn.setStyle(base +
                "-fx-background-color: " + normalColor + ";" +
                "-fx-text-fill: " + textColor + ";");

        // Hover
        btn.setOnMouseEntered(e ->
                btn.setStyle(base +
                        "-fx-background-color: " + hoverColor + ";" +
                        "-fx-text-fill: " + textColor + ";"));

        // Zurück
        btn.setOnMouseExited(e ->
                btn.setStyle(base +
                        "-fx-background-color: " + normalColor + ";" +
                        "-fx-text-fill: " + textColor + ";"));
    }


    private void addTableColumns() {
        TableColumn<Connection, String> colLocalIP = new TableColumn<>("Lokal IP");
        colLocalIP.setCellValueFactory(new PropertyValueFactory<>("localAddress"));
        colLocalIP.setMinWidth(140);

        TableColumn<Connection, Integer> colLocalPort = new TableColumn<>("Lokal Port");
        colLocalPort.setCellValueFactory(new PropertyValueFactory<>("localPort"));
        colLocalPort.setMinWidth(100);

        TableColumn<Connection, String> colRemoteIP = new TableColumn<>("Remote IP");
        colRemoteIP.setCellValueFactory(new PropertyValueFactory<>("remoteAddress"));
        colRemoteIP.setMinWidth(140);

        TableColumn<Connection, Integer> colRemotePort = new TableColumn<>("Remote Port");
        colRemotePort.setCellValueFactory(new PropertyValueFactory<>("remotePort"));
        colRemotePort.setMinWidth(100);

        TableColumn<Connection, String> colState = new TableColumn<>("Status");
        colState.setCellValueFactory(new PropertyValueFactory<>("state"));
        colState.setMinWidth(120);

        table.getColumns().addAll(colLocalIP, colLocalPort, colRemoteIP,
                colRemotePort, colState);
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.stop();
        }
    }

    private void clearBtnLogic() {
        output.setText("Output: Cleared!");
        table.getItems().clear();
        placeholder.setText("Cleared!");
        table.setPlaceholder(placeholder);
    }

    public List<Connection> ladeDaten() {
        return parser.getConnections();
    }

    public void updateUi(List<Connection> daten) {
        ObservableList<Connection> observable =
                FXCollections.observableArrayList(daten);
        table.setItems(observable);
        output.setText("Output: Aktive Verbindungen: " + daten.size());
    }
}