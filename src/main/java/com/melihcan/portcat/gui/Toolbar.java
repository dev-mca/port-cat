package com.melihcan.portcat.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

/**
 * Obere Leiste: Logo und Titel links, DNS-Schalter, Output-Label und Buttons rechts.
 */
public class Toolbar extends HBox {

    private final Label output = new Label("Output: ");
    private final Button startStopBtn = new Button("Start");
    private boolean dnsWarningShown = false;

    public Toolbar(Runnable onStartStop, Runnable onSave, Runnable onClear, Consumer<Boolean> onDnsToggle) {
        setMinHeight(85);
        setPrefHeight(85);
        setAlignment(Pos.CENTER);

        output.getStyleClass().add("output");

        Label title = new Label("PortCat - TCP-Connections");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: #FFFBFC;");

        sizeButton(startStopBtn, 150);
        startStopBtn.getStyleClass().addAll("startStopBtn", "startState");
        startStopBtn.setOnAction(e -> onStartStop.run());

        Button btnSave = createButton("Save state as CSV", 200, "btnSave", onSave);
        Button btnClear = createButton("Clear", 110, "btnClear", onClear);
        Button btnTutorial = createButton("Tutorial", 150, "btnTutorial", TutorialVideo::showTutorialVideo);

        CheckBox dnsCheckBox = createDnsCheckBox(onDnsToggle);

        // links: Logo + Titel
        HBox leftPart = new HBox(10, createLogo(), title);
        leftPart.setAlignment(Pos.CENTER);

        // rechts: DNS-Toggle + Output + Buttons
        HBox rightPart = new HBox(10, dnsCheckBox, output, startStopBtn, btnSave, btnClear, btnTutorial);
        rightPart.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(leftPart, spacer, rightPart);
    }

    public Label getOutput() {
        return output;
    }

    /** Passt Text und Style des Start/Stop-Buttons an. */
    public void setRunning(boolean running) {
        startStopBtn.setText(running ? "Stop" : "Start");
        String add = running ? "stopState" : "startState";
        String remove = running ? "startState" : "stopState";
        startStopBtn.getStyleClass().remove(remove);
        if (!startStopBtn.getStyleClass().contains(add)) {
            startStopBtn.getStyleClass().add(add);
        }
    }

    private static ImageView createLogo() {
        ImageView logoView = new ImageView();
        var stream = Toolbar.class.getResourceAsStream("/logo.png");
        if (stream != null) {
            logoView.setImage(new Image(stream));
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            logoView.setFitHeight(150);
        } else {
            System.err.println("WARNUNG: Logo nicht gefunden in /resources/logo.png");
        }
        return logoView;
    }

    private static Button createButton(String text, double width, String styleClass, Runnable action) {
        Button button = new Button(text);
        sizeButton(button, width);
        button.getStyleClass().add(styleClass);
        button.setOnAction(e -> action.run());
        return button;
    }

    private static void sizeButton(Button button, double width) {
        button.setMinSize(width, 40);
        button.setPrefSize(width, 40);
        button.setMaxSize(width, 40);
    }

    private CheckBox createDnsCheckBox(Consumer<Boolean> onDnsToggle) {
        CheckBox dnsCheckBox = new CheckBox("DNS-Auflösung");
        dnsCheckBox.setStyle("-fx-text-fill: #FFFBFC; -fx-font-size: 13px; -fx-font-weight: bold;");
        dnsCheckBox.setSelected(false);  // Standardmäßig AUS

        dnsCheckBox.setOnAction(e -> {
            boolean enabled = dnsCheckBox.isSelected();

            // Einmaliger Warning-Dialog beim ersten Aktivieren
            if (enabled && !dnsWarningShown) {
                showDnsWarning();
                dnsWarningShown = true;
            }

            onDnsToggle.accept(enabled);
        });
        return dnsCheckBox;
    }

    private void showDnsWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(getScene().getWindow());
        alert.setTitle("DNS-Auflösung");
        alert.setHeaderText("Achtung: Performance-Warnung");
        alert.setContentText(
                "DNS-Auflösung kann die Ladezeit deutlich verlängern!\n\n"
                        + "Bei vielen Verbindungen (100+) kann es mehrere Minuten dauern, "
                        + "bis Verbindungen angezeigt werden,\n"
                        + "da jede IP-Adresse über das Netzwerk aufgelöst werden muss.\n\n"
                        + "Für die Anzahl aktiven Verbindungen über 50 oder generell den normalen Nutzen "
                        + "empfehlen wir DNS DEAKTIVIERT zu lassen.");
        alert.showAndWait();
    }
}
