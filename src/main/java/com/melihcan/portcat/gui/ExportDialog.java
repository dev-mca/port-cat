package com.melihcan.portcat.gui;

import com.melihcan.portcat.model.Connection;
import com.melihcan.portcat.service.CsvExporter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Dialog-Ablauf für den CSV-Export: Datei wählen, schreiben, Ergebnis melden. */
public final class ExportDialog {

    private ExportDialog() {
    }

    public static void show(Window owner, List<Connection> data) {
        if (data == null || data.isEmpty()) {
            showAlert(owner, AlertType.WARNING, "Warnung", null, "Keine Daten zum Exportieren vorhanden.");
            return;
        }

        File file = openSaveDialog(owner);
        if (file == null) {
            return; // Nutzer hat abgebrochen
        }

        try {
            CsvExporter.export(file.toPath(), data);
            System.out.println("CSV erfolgreich geschrieben nach: " + file.getAbsolutePath());
            showAlert(owner, AlertType.INFORMATION, "Erfolgreich gespeichert", null,
                    "CSV erfolgreich gespeichert:\n" + file.getName());
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert(owner, AlertType.ERROR, "Fehler beim Speichern",
                    "Fehler beim Speichern der CSV-Datei", ex.getMessage());
        }
    }

    private static File openSaveDialog(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("CSV‑Datei speichern");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV‑Dateien", "*.csv"));
        chooser.setInitialFileName("connections.csv");
        return chooser.showSaveDialog(owner);
    }

    private static void showAlert(Window owner, AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
