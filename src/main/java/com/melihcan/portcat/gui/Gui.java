package com.melihcan.portcat.gui;

import com.melihcan.portcat.service.ConnectionParser;
import com.melihcan.portcat.service.SaveAsCSV;
import com.melihcan.portcat.service.Scheduler;
import javafx.application.Application;

import javafx.collections.*;

import javafx.geometry.*;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyValue;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import javafx.stage.Stage;
import com.melihcan.portcat.model.Connection;

import java.util.List;

public class Gui extends Application {

    /* ---------------------------------------------------
        Felder
     --------------------------------------------------- */
    private final ListView<Connection> listView = new ListView<>();
    private final ConnectionParser parser = new ConnectionParser();
    private Scheduler scheduler;
    private Label output;
    private final Label placeholder = new Label();
    private boolean running = false;
    private int lastActiveCount = -1;
    private Timeline flashTimeline;
    private Timeline greenPulse;


    /* ---------------------------------------------------
        Start/Einstieg
     --------------------------------------------------- */
    @Override
    public void start(Stage stage) {
        stage.setTitle("PortCat V4");

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
        output.getStyleClass().add("output");

        Label title = new Label("PortCat - TCP-Connections");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: #FFFBFC;");

        Button btnClear = new Button("Clear");
        btnClear.setMinSize(110, 40);
        btnClear.setPrefSize(110, 40);
        btnClear.setMaxSize(110, 40);
        btnClear.getStyleClass().add("btnClear");

        btnClear.setOnAction(e -> clearBtnLogic());

        Button startStopBtn = new Button("Start");
        startStopBtn.setMinSize(150, 40);
        startStopBtn.setPrefSize(150, 40);
        startStopBtn.setMaxSize(150, 40);
        startStopBtn.getStyleClass().addAll("startStopBtn", "startState");

        startStopBtn.setOnAction(e -> startStopScheduler(startStopBtn));

        Button btnSave = new Button("Save state as CSV");
        btnSave.setMinSize(200, 40);
        btnSave.setPrefSize(200, 40);
        btnSave.setMaxSize(200, 40);
        btnSave.getStyleClass().add("btnSave");

        btnSave.setOnAction(e -> SaveAsCSV.saveAsCsv(
                btnSave.getScene().getWindow(),   // Owner‑Fenster für den Dialog
                listView.getItems()));




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
        HBox rightPart = new HBox(10, output, startStopBtn, btnSave, btnClear);
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
        startSmoothGreenPulse(output);

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

    //Mithilfe von KI generiert
    private void startSmoothGreenPulse(Label lbl) {
        if (greenPulse != null) greenPulse.stop();

        DropShadow green = new DropShadow();
        green.setColor(Color.rgb(57, 255, 20, 1.0));
        green.setOffsetX(0);
        green.setOffsetY(0);
        green.setRadius(2);
        green.setSpread(0.0);

        // Effekt setzen (wichtig: bleibt gesetzt; wird nur "stärker/schwächer")
        lbl.setEffect(green);

        greenPulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(green.radiusProperty(), 6, Interpolator.EASE_BOTH),
                        new KeyValue(green.spreadProperty(), 0.05, Interpolator.EASE_BOTH),
                        new KeyValue(green.colorProperty(), Color.rgb(57, 255, 20, 0.25), Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.seconds(0.75),
                        new KeyValue(green.radiusProperty(), 22, Interpolator.EASE_BOTH),
                        new KeyValue(green.spreadProperty(), 0.35, Interpolator.EASE_BOTH),
                        new KeyValue(green.colorProperty(), Color.rgb(57, 255, 20, 0.85), Interpolator.EASE_BOTH)
                )
        );
        greenPulse.setAutoReverse(true);
        greenPulse.setCycleCount(Animation.INDEFINITE);
        greenPulse.play();
    }

    //Mithilfe von KI generiert
    private void flashBlue(Label lbl) {
        if (flashTimeline != null) flashTimeline.stop();

        lbl.getStyleClass().add("flash-blue");
        flashTimeline = new Timeline(
                new KeyFrame(Duration.millis(250), e -> lbl.getStyleClass().remove("flash-blue"))
        );
        flashTimeline.setCycleCount(1);
        flashTimeline.play();
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

        int count = daten.size();
        output.setText("Output: Aktive Verbindungen: " + count);

        if (lastActiveCount != -1 && count != lastActiveCount) {
            flashBlue(output);
        }
        lastActiveCount = count;
    }

    public void startStopScheduler(Button startStopBtn) {
        running = !running;

        if (running) {
            scheduler.start();
            startStopBtn.setText("Stop");
            startStopBtn.getStyleClass().remove("startState");
            if (!startStopBtn.getStyleClass().contains("stopState")) {
                startStopBtn.getStyleClass().add("stopState");
            }
        } else {
            scheduler.stop();
            startStopBtn.setText("Start");
            startStopBtn.getStyleClass().remove("stopState");
            if (!startStopBtn.getStyleClass().contains("startState")) {
                startStopBtn.getStyleClass().add("startState");
            }
        }
    }


}