package com.melihcan.portcat.gui;

import javafx.scene.control.Alert;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class TutorialVideo {

    public static void showTutorialVideo() {
        try {
            InputStream videoStream = Gui.class.getResourceAsStream("/portcat_tutorial.mp4");

            if (videoStream == null) {
                throw new Exception("Video nicht in JAR gefunden!");
            }

            File tempVideo = File.createTempFile("portcat_tutorial", ".mp4");
            tempVideo.deleteOnExit();

            Files.copy(videoStream, tempVideo.toPath(), StandardCopyOption.REPLACE_EXISTING);
            videoStream.close();

            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                Desktop.getDesktop().open(tempVideo);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                String command = os.contains("mac") ? "open" : "xdg-open";
                new ProcessBuilder(command, tempVideo.getAbsolutePath()).start();
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fehler");
            alert.setContentText("Video konnte nicht geladen werden: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
