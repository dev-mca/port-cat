package com.melihcan.portcat;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
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

    /* =======================================================
       Felder
    ======================================================= */
    private final TableView<Connection> table = new TableView<>();
    private final PortParser            parser = new PortParser();

    /* =======================================================
       Einstieg
    ======================================================= */
    @Override
    public void start(Stage stage) {
        stage.setTitle("PortCat v2");

        /* ---------------------------------------------------
           Logo vorbereiten
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
        Label output = new Label("Output:");
        output.setStyle("-fx-text-fill: #fffbfc; -fx-font-weight: bold; -fx-font-size: 17.5px;");


        Label title  = new Label("PortCat - TCP-Connections");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: #FFFBFC;");

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setPrefSize(125, 40);
        styleButton(btnRefresh, "#FFFBFC", "#999394", "#30332e");
        btnRefresh.setOnAction(e -> {
            output.setText("Refreshing...");

            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    return ladeDaten(); // läuft im Background
                }
            };

            task.setOnSucceeded(ev ->
                    output.setText("Output: " + task.getValue())
            );

            new Thread(task).start();
        });

        Button btnClear = new Button("Clear");
        btnClear.setPrefSize(95, 40);
        styleButton(btnClear, "#F44336", "#D32F2F", "#FFFBFC");  // jetzt wirklich rot!
        btnClear.setOnAction(e -> output.setText("Output: Cleared!"));

        /* ---------------------------------------------------
           TopBar (liegt neu im selben Container wie die Tabelle)
        --------------------------------------------------- */
        HBox topBar = new HBox();
        topBar.setMinHeight(85);
        topBar.setPrefHeight(85);
        topBar.setAlignment(Pos.CENTER);    // oder CENTER_LEFT, wie gewünscht
        topBar.setPadding(new Insets(4, 10, 4, 10));
        topBar.setStyle("-fx-background-color: #30332E; "
                + "-fx-border-color: #30332E; -fx-border-width: 0 0 1 0;");

        // links: Logo + Titel
        HBox leftPart = new HBox(10, logoView, title);
        leftPart.setAlignment(Pos.CENTER_LEFT);

        // rechts: Output + Buttons
        HBox rightPart = new HBox(10, output, btnRefresh, btnClear);
        rightPart.setAlignment(Pos.CENTER_RIGHT);

        // Spacer in der Mitte
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(leftPart, spacer, rightPart);

        /* ---------------------------------------------------
           Side-Panels (nur Deko / Begrenzung)
        --------------------------------------------------- */
        VBox leftPanel  = createSidePanel(50);
        VBox rightPanel = createSidePanel(50);

        leftPanel.setStyle("-fx-background-color: #30332E; "
                + "-fx-border-color: #30332E; -fx-border-width: 0 0 1 0;");

        rightPanel.setStyle("-fx-background-color: #30332E; "
                + "-fx-border-color: #30332E; -fx-border-width: 0 0 1 0;");

        /* ---------------------------------------------------
           Tabelle + TopBar als gemeinsamer Block
        --------------------------------------------------- */
        VBox tableContainer = new VBox();
        tableContainer.getChildren().addAll(topBar, table);
        VBox.setVgrow(table, Priority.ALWAYS);     // Tabelle darf wachsen
        HBox.setHgrow(tableContainer, Priority.ALWAYS);

        /* ---------------------------------------------------
           Hauptinhalt
        --------------------------------------------------- */
        HBox mainContent = new HBox();
        mainContent.getChildren().addAll(leftPanel, tableContainer, rightPanel);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        /* ---------------------------------------------------
           Root-Layout
        --------------------------------------------------- */
        BorderPane root = new BorderPane();
        root.setCenter(mainContent);

        // optionale Bottom-Bar
        HBox bottomBar = new HBox();
        bottomBar.setPrefHeight(28);
        bottomBar.setStyle("-fx-background-color: #30332E; "
                + "-fx-border-color: #30332E; -fx-border-width: 0 0 1 0;");
        root.setBottom(bottomBar);

        /* ---------------------------------------------------
           Tabellen-Spalten
        --------------------------------------------------- */
        addTableColumns();
        table.setPlaceholder(new Label("Keine Verbindungen gefunden"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

//Generiert mit ChatGPT o3 für auto-scale Logik
// =======================================================
// Szene anzeigen  (neuer, robuster Block)
// =======================================================
        double designW   = 950;   // Entwurfsbreite
        double designH   = 680;   // Entwurfshöhe
        double minScale  = 0.6;   // kleiner darf es nicht werden

        Group rootGroup   = new Group(root);            // BorderPane -> Group
        StackPane wrapper = new StackPane(rootGroup);   // Group -> StackPane
        StackPane.setAlignment(rootGroup, Pos.TOP_LEFT);
        wrapper.setStyle("-fx-background-color: #30332E;");

// -------------------------------------------------------
// Skalierungsfaktor berechnen – aber nie > 1, nie < minScale
// -------------------------------------------------------
        DoubleProperty scale = new SimpleDoubleProperty(1.0);

        NumberBinding ratio = Bindings.min(
                wrapper.widthProperty().divide(designW),
                wrapper.heightProperty().divide(designH));

        scale.bind(
                Bindings.max(minScale,        // untere Schranke
                        Bindings.min(1.0, ratio)));   // obere Schranke

        rootGroup.scaleXProperty().bind(scale);
        rootGroup.scaleYProperty().bind(scale);

// -------------------------------------------------------
// BorderPane-Grösse an Wrapper binden
// -------------------------------------------------------
        root.prefWidthProperty() .bind(wrapper.widthProperty() .divide(scale));
        root.prefHeightProperty().bind(wrapper.heightProperty().divide(scale));

// -------------------------------------------------------
// Szene / Stage
// -------------------------------------------------------
        Scene scene = new Scene(wrapper, designW, designH);
        stage.setScene(scene);

// Verhindert, dass der User kleiner als das Minimum ziehen kann
        stage.setMinWidth (designW  * minScale);
        stage.setMinHeight(designH  * minScale);

        stage.centerOnScreen();
        stage.show();


        // erste Datenladung
        output.setText("Output: " + ladeDaten());
    }

    /* =======================================================
       Hilfsmethoden
    ======================================================= */
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

    private VBox createSidePanel(double width) {
        VBox panel = new VBox();
        panel.setPrefWidth(width);
        panel.setMinWidth(width);
        panel.setMaxWidth(width);
        panel.setStyle("-fx-background-color: #ffffff;");
        VBox.setVgrow(panel, Priority.ALWAYS);
        return panel;
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

    private String ladeDaten() {
        List<Connection> datenListe = parser.getConnections();
        ObservableList<Connection> observableListe =
                FXCollections.observableArrayList(datenListe);
        table.setItems(observableListe);
        return "Daten aktualisiert! Anzahl: " + datenListe.size();
    }
}