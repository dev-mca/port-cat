package com.melihcan.portcat.gui;

import com.melihcan.portcat.service.ConnectionParser;
import com.melihcan.portcat.service.SaveAsCSV;
import com.melihcan.portcat.service.Scheduler;
import javafx.application.Application;

import javafx.collections.*;

import javafx.geometry.*;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.*;
import javafx.scene.image.Image;
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

import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import javafx.scene.media.Media;

import java.awt.*;
import java.util.List;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Gui extends Application {

    /* ---------------------------------------------------
        Felder
     --------------------------------------------------- */
    private final ListView<Connection> listView = new ListView<>();
    private final ConnectionParser parser = new ConnectionParser();
    private Scheduler scheduler;
    private Label output;
    private final Label placeholder = new Label();
    private int lastActiveCount = -1;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Timeline loadingAnimation;
    private final AtomicBoolean isLoading = new AtomicBoolean(false);
    private boolean dnsWarningShown = false;  // Für einmaligen Warning-Dialog


    /* ---------------------------------------------------
        Start/Einstieg
     --------------------------------------------------- */
    @Override
    public void start(Stage stage) {
        stage.setTitle("PortCat v1.0");

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
            System.err.println("WARNUNG: Logo nicht gefunden in /resources/logo.png");
        }

        /* ---------------------------------------------------
           Labels & Buttons
        --------------------------------------------------- */
        output = new Label("Output: ");
        output.getStyleClass().add("output");

        Label title = new Label("PortCat - TCP-Connections");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: #FFFBFC;");

        Label credits = new Label("by Melih Can Abay");
        credits.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #a9a9a9;");


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
                btnSave.getScene().getWindow(),
                listView.getItems()));

        CheckBox dnsCheckBox = new CheckBox("DNS-Auflösung");
        dnsCheckBox.setStyle(
                "-fx-text-fill: #FFFBFC; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold;"
        );
        dnsCheckBox.setSelected(false);  // Standardmäßig AUS

        dnsCheckBox.setOnAction(e -> {
            boolean enabled = dnsCheckBox.isSelected();

            // Einmaliger Warning-Dialog beim ersten Aktivieren
            if (enabled && !dnsWarningShown) {
                showDnsWarning(stage);
                dnsWarningShown = true;
            }

            parser.setDnsEnabled(enabled);
            System.out.println("DNS-Auflösung " + (enabled ? "AN" : "AUS"));
        });

        Button btnTutorial = new Button("Tutorial");
        btnTutorial.setMinSize(150, 40);
        btnTutorial.setPrefSize(150, 40);
        btnTutorial.setMaxSize(150, 40);
        btnTutorial.getStyleClass().add("btnTutorial");

        btnTutorial.setOnAction(e -> showTutorialVideo());


        /* ---------------------------------------------------
           TopBar
        --------------------------------------------------- */
        HBox topBar = new HBox();
        topBar.setMinHeight(85);
        topBar.setPrefHeight(85);
        topBar.setAlignment(Pos.CENTER);

        // links: Logo + Titel
        HBox leftPart = new HBox(10, logoView, title);
        leftPart.setAlignment(Pos.CENTER);

        // rechts: DNS-Toggle + Output + Buttons
        HBox rightPart = new HBox(10, dnsCheckBox, output, startStopBtn, btnSave, btnClear, btnTutorial);
        rightPart.setAlignment(Pos.CENTER);

        // Spacer in der Mitte
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(leftPart, spacer, rightPart);

        /* ---------------------------------------------------
           BottomBar
        --------------------------------------------------- */
        HBox bottomBar = new HBox();
        bottomBar.setMinHeight(60);
        bottomBar.setPrefHeight(60);
        bottomBar.setPadding(new Insets(20, 20, 20, 20));

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        bottomBar.getChildren().addAll(leftSpacer, credits, rightSpacer);



        /* ---------------------------------------------------
           Root-Layout
        --------------------------------------------------- */
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #30332E");
        root.setPadding(new Insets(20, 50, 20, 50));
        root.setTop(topBar);
        root.setBottom(bottomBar);
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

        // Null-Check für CSS-Datei
        var cssResource = getClass().getResource("/app.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.err.println("WARNUNG: app.css nicht gefunden - Styles werden nicht geladen");
        }

        stage.setScene(scene);


        stage.centerOnScreen();


        stage.show();
        initRedPulse(output);  // ROT-Start!

        //scheduler instanziieren
        scheduler = new Scheduler(this);

        // Schließen-Handler
        stage.setOnCloseRequest(e -> {
            System.out.println("Fenster wird geschlossen - beende Scheduler");
            if (scheduler != null) {
                scheduler.terminate();
            }
        });
    }

    /* ---------------------------------------------------
        Hilfsmethoden
     --------------------------------------------------- */

    @Override
    public void stop() {
        System.out.println("Application.stop() aufgerufen");
        if (scheduler != null) {
            scheduler.terminate();
        }
    }

    /**
     * NEU: Zeigt Warning-Dialog bei DNS-Aktivierung
     */
    private void showDnsWarning(Stage owner) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(owner);
        alert.setTitle("DNS-Auflösung");
        alert.setHeaderText("Achtung: Performance-Warnung");
        alert.setContentText(
                "DNS-Auflösung kann die Ladezeit deutlich verlängern!\n\n" +
                        "Bei vielen Verbindungen (100+) kann es mehrere Minuten dauern bis Verbindungen angezeigt werden,\n" +
                        "da jede IP-Adresse über das Netzwerk aufgelöst werden muss.\n\n" +
                        "Für die Anzahl aktiven Verbindungen über 50 oder generell den normalen Nutzen empfehlen wir DNS DEAKTIVIERT zu lassen."
        );
        alert.showAndWait();
    }

    private Timeline redPulse;
    private Timeline greenPulse;
    private boolean isGreenActive = false;


    //Methode für Puls-Effekte mithilfe von KI geschrieben
    private void startBtnAnimation(Label lbl) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(57, 255, 20, 1.0));
        glow.setOffsetX(0);
        glow.setOffsetY(0);
        glow.setRadius(2);
        glow.setSpread(0.0);
        lbl.setEffect(glow);

        // Alte Animation stoppen
        if (redPulse != null) redPulse.stop();
        if (greenPulse != null) greenPulse.stop();

        // Toggle-Logik
        if (isGreenActive) {
            // Wechsel zu ROT
            redPulse = createRedPulse(glow);
            redPulse.play();
            isGreenActive = false;
        } else {
            // Wechsel zu GRÜN
            greenPulse = createGreenPulse(glow);
            greenPulse.play();
            isGreenActive = true;
        }
    }

    private Timeline createGreenPulse(DropShadow glow) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 6, Interpolator.EASE_BOTH),
                        new KeyValue(glow.spreadProperty(), 0.05, Interpolator.EASE_BOTH),
                        new KeyValue(glow.colorProperty(), Color.rgb(57, 255, 20, 0.25), Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.seconds(0.75),
                        new KeyValue(glow.radiusProperty(), 22, Interpolator.EASE_BOTH),
                        new KeyValue(glow.spreadProperty(), 0.35, Interpolator.EASE_BOTH),
                        new KeyValue(glow.colorProperty(), Color.rgb(57, 255, 20, 0.85), Interpolator.EASE_BOTH)
                )
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);

        return timeline;
    }


    private Timeline createRedPulse(DropShadow glow) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 6, Interpolator.EASE_BOTH),
                        new KeyValue(glow.spreadProperty(), 0.05, Interpolator.EASE_BOTH),
                        new KeyValue(glow.colorProperty(), Color.rgb(180, 40, 40, 0.25), Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.seconds(0.75),
                        new KeyValue(glow.radiusProperty(), 22, Interpolator.EASE_BOTH),
                        new KeyValue(glow.spreadProperty(), 0.35, Interpolator.EASE_BOTH),
                        new KeyValue(glow.colorProperty(), Color.rgb(255, 57, 57, 0.85), Interpolator.EASE_BOTH)
                )
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);

        return timeline;
    }



    //Mithilfe von KI generiert

    private Timeline flashTimeline;
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
        // Stoppe Lade-Animation wenn Daten ankommen
        stopLoadingAnimation();

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
        boolean vorher = running.getAndSet(!running.get());

        if (!vorher) {
            System.out.println("Starte Scheduler...");

            // Starte Lade-Animation
            startLoadingAnimation();

            scheduler.start();
            startStopBtn.setText("Stop");
            startStopBtn.getStyleClass().remove("startState");
            if (!startStopBtn.getStyleClass().contains("stopState")) {
                startStopBtn.getStyleClass().add("stopState");
            }
            startBtnAnimation(output);
        } else {
            System.out.println("Stoppe Scheduler...");

            // Stoppe Lade-Animation
            stopLoadingAnimation();

            scheduler.stop();
            startStopBtn.setText("Start");
            output.setText("Output: Stopped");
            startStopBtn.getStyleClass().remove("stopState");
            if (!startStopBtn.getStyleClass().contains("startState")) {
                startStopBtn.getStyleClass().add("startState");
            }
            startBtnAnimation(output);
        }
    }

    /**
     * Startet die Lade-Animation mit Punkten
     */
    private void startLoadingAnimation() {
        if (loadingAnimation != null) {
            loadingAnimation.stop();
        }

        isLoading.set(true);
        AtomicInteger dotCount = new AtomicInteger(0);

        loadingAnimation = new Timeline(
                new KeyFrame(Duration.millis(400), e -> {
                    if (!isLoading.get()) return;

                    int dots = dotCount.getAndIncrement() % 4;
                    StringBuilder text = new StringBuilder("Output: Lade Daten");

                    for (int i = 0; i < dots; i++) {
                        text.append(" .");
                    }

                    output.setText(text.toString());
                })
        );

        loadingAnimation.setCycleCount(Animation.INDEFINITE);
        loadingAnimation.play();
    }

    /**
     * Stoppt die Lade-Animation
     */
    private void stopLoadingAnimation() {
        isLoading.set(false);

        if (loadingAnimation != null) {
            loadingAnimation.stop();
            loadingAnimation = null;
        }
    }



    private void initRedPulse(Label lbl) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(180, 40, 40, 1.0));
        glow.setOffsetX(0); glow.setOffsetY(0); glow.setRadius(2); glow.setSpread(0.0);
        lbl.setEffect(glow);

        redPulse = createRedPulse(glow);
        redPulse.play();  //Startet IMMER mit ROT
    }

    private static void showTutorialVideo() {
        try {
            String videoPath = Gui.class.getResource("/portcat_tutorial.mp4").getPath();

            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows: Nutze Desktop API
                Desktop.getDesktop().open(new java.io.File(videoPath));
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Linux/Unix/Mac: Nutze xdg-open bzw. open
                String command = os.contains("mac") ? "open" : "xdg-open";
                new ProcessBuilder(command, videoPath).start();
            } else {
                throw new Exception("Betriebssystem nicht unterstützt");
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fehler");
            alert.setContentText("Video konnte nicht geöffnet werden: " + e.getMessage());
            alert.showAndWait();
        }
    }

}