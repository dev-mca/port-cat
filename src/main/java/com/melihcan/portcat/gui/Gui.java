package com.melihcan.portcat.gui;

import com.melihcan.portcat.service.PortParser;
import com.melihcan.portcat.service.Scheduler;
import javafx.application.Application;

import javafx.collections.*;

import javafx.geometry.*;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;

import javafx.stage.Stage;
import com.melihcan.portcat.model.Connection;

import java.util.List;

public class Gui extends Application {

    /* ---------------------------------------------------
        Felder
     --------------------------------------------------- */
    private final ListView<Connection> listView = new ListView<>();
    private final PortParser parser = new PortParser();
    private Scheduler scheduler;
    private Label output;
    private final Label placeholder = new Label();
    private boolean running = false;


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
        var stream = getClass().getResourceAsStream("/logo.png");
        if (stream != null) {
            Image logo = new Image(stream);
            logoView.setImage(logo);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            logoView.setFitHeight(150); // kompakt
        } else {
            System.err.println("Logo not found in path!: /resources/logo.png");
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
        btnClear.getStyleClass().add("btnClear");
        btnClear.setOnAction(e -> clearBtnLogic());

        Button startStopBtn = new Button("▶ Start");
        startStopBtn.setPrefSize(95, 40);
        startStopBtn.getStyleClass().addAll("startStopBtn", "startState");

        startStopBtn.setOnAction(e -> startStopScheduler(startStopBtn));


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
        HBox rightPart = new HBox(10, output, startStopBtn, btnClear);
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
        root.setCenter(listView);


        /* ---------------------------------------------------
           Tabellen-Spalten
        --------------------------------------------------- */
        placeholder.setText("Keine Verbindungen gefunden!");
        listView.setCellFactory(lv -> new ConnectionCell());


        /* ---------------------------------------------------
           Scene
        --------------------------------------------------- */
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setScene(scene);

        stage.centerOnScreen();
        stage.show();

        // erste Datenladung
        List<Connection> daten = ladeDaten(); // nur Daten holen
        updateUi(daten);                      // UI initialisieren

        //scheduler instanziieren
        scheduler = new Scheduler(this);
    }

    /* ---------------------------------------------------
        Hilfsmethoden
     --------------------------------------------------- */

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.terminate();
        }
    }

    private void clearBtnLogic() {
        output.setText("Output: Cleared!");
        listView.getItems().clear();
        placeholder.setText("Cleared!");
        listView.setPlaceholder(placeholder);
    }

    public List<Connection> ladeDaten() {
        return parser.getConnections();
    }

    public void updateUi(List<Connection> daten) {
        ObservableList<Connection> observable = FXCollections.observableArrayList(daten);
        listView.setItems(observable);
        output.setText("Output: Aktive Verbindungen: " + daten.size());
    }

    public void startStopScheduler(Button startStopBtn) {
        running = !running;

        if (running) {
            scheduler.start();
            startStopBtn.setText("■ Stop");
            startStopBtn.getStyleClass().remove("startState");
            if (!startStopBtn.getStyleClass().contains("stopState")) {
                startStopBtn.getStyleClass().add("stopState");
            }
        } else {
            scheduler.stop();
            startStopBtn.setText("▶ Start");
            startStopBtn.getStyleClass().remove("stopState");
            if (!startStopBtn.getStyleClass().contains("startState")) {
                startStopBtn.getStyleClass().add("startState");
            }
        }
    }
}