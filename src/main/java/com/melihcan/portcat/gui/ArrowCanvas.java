package com.melihcan.portcat.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class ArrowCanvas extends Canvas {

    private String direction; // "INCOMING" oder "OUTGOING"
    private Color arrowColor = Color.web("#6b7280"); // Default grau

    public ArrowCanvas(double width, double height) {
        super(width, height);
        setStyle("-fx-background-color: transparent;");
    }

    public void setDirection(String direction) {
        this.direction = direction;

        // Farbe basierend auf Richtung
        if ("INCOMING".equals(direction)) {
            arrowColor = Color.web("#10b981"); // Grün
        } else if ("OUTGOING".equals(direction)) {
            arrowColor = Color.web("#f59e0b"); // Orange
        } else {
            arrowColor = Color.web("#6b7280"); // Grau
        }

        drawArrow();
    }

    private void drawArrow() {
        GraphicsContext gc = getGraphicsContext2D();

        // Canvas löschen
        gc.clearRect(0, 0, getWidth(), getHeight());

        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;
        double arrowSize = 20;

        gc.setStroke(arrowColor);
        gc.setFill(arrowColor);
        gc.setLineWidth(2.5);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        if ("INCOMING".equals(direction)) {
            // Pfeil nach LINKS ⬅️
            // Linie von rechts nach links
            gc.strokeLine(centerX + arrowSize, centerY, centerX - arrowSize, centerY);

            // Pfeilspitze (linke Seite)
            gc.strokeLine(centerX - arrowSize, centerY, centerX - arrowSize + 8, centerY - 8);
            gc.strokeLine(centerX - arrowSize, centerY, centerX - arrowSize + 8, centerY + 8);

        } else if ("OUTGOING".equals(direction)) {
            // Pfeil nach RECHTS ➡️
            // Linie von links nach rechts
            gc.strokeLine(centerX - arrowSize, centerY, centerX + arrowSize, centerY);

            // Pfeilspitze (rechte Seite)
            gc.strokeLine(centerX + arrowSize, centerY, centerX + arrowSize - 8, centerY - 8);
            gc.strokeLine(centerX + arrowSize, centerY, centerX + arrowSize - 8, centerY + 8);

        } else {
            // Bidirektionaler Pfeil ↔️
            // Linie oben
            gc.strokeLine(centerX - arrowSize, centerY - 8, centerX + arrowSize, centerY - 8);
            // Linie unten
            gc.strokeLine(centerX - arrowSize, centerY + 8, centerX + arrowSize, centerY + 8);

            // Linke Pfeilspitze
            gc.strokeLine(centerX - arrowSize, centerY - 8, centerX - arrowSize + 7, centerY - 14);
            gc.strokeLine(centerX - arrowSize, centerY - 8, centerX - arrowSize + 7, centerY - 2);

            // Rechte Pfeilspitze
            gc.strokeLine(centerX + arrowSize, centerY + 8, centerX + arrowSize - 7, centerY + 14);
            gc.strokeLine(centerX + arrowSize, centerY + 8, centerX + arrowSize - 7, centerY + 2);
        }
    }
}
