package com.melihcan.portcat.gui;

import com.melihcan.portcat.model.Connection;
import com.melihcan.portcat.service.ConnectionService;
import com.melihcan.portcat.service.Scheduler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.List;

public class Gui extends Application {

    private final ListView<Connection> listView = new ListView<>();
    private final ConnectionService connectionService = new ConnectionService();
    private final Label placeholder = new Label();
    private Scheduler scheduler;
    private Toolbar toolbar;
    private Animations animations;
    private boolean running = false;
    private int lastActiveCount = -1;

    @Override
    public void start(Stage stage) {
        stage.setTitle("PortCat " + appVersion());

        TutorialVideo tutorial = new TutorialVideo(getHostServices(), TutorialVideo.TUTORIAL_URL);

        toolbar = new Toolbar(
                this::toggleScheduler,
                () -> ExportDialog.show(stage, listView.getItems()),
                this::clear,
                () -> tutorial.show(stage),
                connectionService::setDnsEnabled);
        animations = new Animations(toolbar.getOutput());

        listView.setCellFactory(lv -> new ConnectionCell());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #30332E");
        root.setPadding(new Insets(20, 50, 20, 50));
        root.setTop(toolbar);
        root.setCenter(listView);
        root.setBottom(createBottomBar());

        Scene scene = new Scene(root);
        var cssResource = getClass().getResource("/app.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.err.println("WARNUNG: app.css nicht gefunden - Styles werden nicht geladen");
        }

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
        animations.startRedPulse();  // Output pulsiert am Anfang rot

        scheduler = new Scheduler(
                connectionService::getConnections,
                connections -> Platform.runLater(() -> updateUi(connections)));
    }

    @Override
    public void stop() {
        System.out.println("Application.stop() aufgerufen");
        if (scheduler != null) {
            scheduler.terminate();
        }
    }

    /** Version aus dem Jar-Manifest (gesetzt beim Build), sonst "dev" beim Start aus der IDE. */
    private static String appVersion() {
        String version = Gui.class.getPackage().getImplementationVersion();
        return version == null ? "dev" : "v" + version;
    }

    private static HBox createBottomBar() {
        Label credits = new Label("by Melih Can Abay");
        credits.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #a9a9a9;");

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        HBox bottomBar = new HBox(leftSpacer, credits, rightSpacer);
        bottomBar.setMinHeight(60);
        bottomBar.setPrefHeight(60);
        bottomBar.setPadding(new Insets(20, 20, 20, 20));
        return bottomBar;
    }

    private void clear() {
        toolbar.getOutput().setText("Output: Cleared!");
        listView.getItems().clear();
        placeholder.setText("Cleared!");
        listView.setPlaceholder(placeholder);
    }

    private void updateUi(List<Connection> connections) {
        animations.stopLoading();

        listView.setItems(FXCollections.observableArrayList(connections));

        int count = connections.size();
        toolbar.getOutput().setText("Output: Aktive Verbindungen: " + count);

        if (lastActiveCount != -1 && count != lastActiveCount) {
            animations.flashBlue();
        }
        lastActiveCount = count;
    }

    private void toggleScheduler() {
        running = !running;

        if (running) {
            System.out.println("Starte Scheduler...");
            animations.startLoading();
            scheduler.start();
        } else {
            System.out.println("Stoppe Scheduler...");
            animations.stopLoading();
            scheduler.stop();
            toolbar.getOutput().setText("Output: Stopped");
        }
        toolbar.setRunning(running);
        animations.togglePulse();
    }
}
