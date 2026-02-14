package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;



import javax.print.attribute.standard.Media;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

//Mithilfe von KI generiert
public final class SaveAsCSV {

    private SaveAsCSV() { /* Utility‑Klasse – kein Instanz */ }

    /** ---------------------------------------------------------
     *  Öffentlicher Aufruf, den du in deiner GUI nutzt
     *  ------------------------------------------------------ */
    public static void saveAsCsv(Window owner, ObservableList<Connection> data) {
        if (data == null || data.isEmpty()) {
            showWarningDialog(owner, "Keine Daten zum Exportieren vorhanden.");
            return;
        }

        File file = openSaveDialog(owner);
        if (file == null) {                     // Nutzer hat abgebrochen
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                file.toPath(), StandardCharsets.UTF_8)) {
            writeCsv(writer, data);
            System.out.println("CSV erfolgreich geschrieben nach: "
                    + file.getAbsolutePath());

            // VERBESSERUNG: Erfolgs-Dialog
            showSuccessDialog(owner, "CSV erfolgreich gespeichert:\n" + file.getName());

        } catch (IOException ex) {
            ex.printStackTrace();

            // VERBESSERUNG: Fehler-Dialog statt nur printStackTrace
            showErrorDialog(owner, "Fehler beim Speichern der CSV-Datei", ex.getMessage());
        }
    }

    /** ---------------------------------------------------------
     *  Kernlogik – schreibt die eigentliche CSV‑Datei
     *  ------------------------------------------------------ */
    public static void writeCsv(Writer writer, List<Connection> data) throws IOException {
        // Kopfzeile - UPDATED: Direction hinzugefügt
        writer.write("LocalAddress,LocalPort,RemoteAddress,RemotePort,State,Direction");
        writer.write(System.lineSeparator());

        // Datenzeilen
        for (Connection c : data) {
            writer.write(csvEscape(c.getLocalAddress()));
            writer.write(',');
            writer.write(Integer.toString(c.getLocalPort()));
            writer.write(',');
            writer.write(csvEscape(c.getRemoteAddress()));
            writer.write(',');
            writer.write(csvEscape(c.getRemotePort()));
            writer.write(',');
            writer.write(csvEscape(c.getState()));
            writer.write(',');
            writer.write(csvEscape(c.getDirection())); // UPDATED: Direction hinzugefügt
            writer.write(System.lineSeparator());
        }
        writer.flush();
    }

    /* ---------------------------------------------------------
     *  Private Hilfsmethoden
     *  ------------------------------------------------------ */

    /** Öffnet einen {@link FileChooser} und liefert das gewählte File. */
    private static File openSaveDialog(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("CSV‑Datei speichern");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV‑Dateien", "*.csv"));
        chooser.setInitialFileName("connections.csv");
        return chooser.showSaveDialog(owner);
    }

    private static String csvEscape(String field) {
        if (field == null) {
            return "";
        }
        boolean mustQuote = field.contains(",") ||
                field.contains("\"") ||
                field.contains("\n") ||
                field.contains("\r");
        if (!mustQuote) {
            return field;
        }
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (char c : field.toCharArray()) {
            if (c == '"') {
                sb.append("\"\"");          // Verdoppeln
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    // NEUE METHODEN: Dialog-Anzeige

    private static void showSuccessDialog(Window owner, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle("Erfolgreich gespeichert");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showErrorDialog(Window owner, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle("Fehler beim Speichern");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static void showWarningDialog(Window owner, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.initOwner(owner);
        alert.setTitle("Warnung");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}