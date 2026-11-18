package com.melihcan.portcat.gui;

import javafx.application.HostServices;
import javafx.scene.control.Alert;
import javafx.stage.Window;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Öffnet das Tutorial-Video über einen Link im Standard-Browser.
 */
public final class TutorialVideo {

    /** Link zum Tutorial-Video (z.B. YouTube). Leer = noch kein Video hinterlegt. */
    public static final String TUTORIAL_URL = "";

    private final HostServices hostServices;
    private final String url;

    public TutorialVideo(HostServices hostServices, String url) {
        this.hostServices = hostServices;
        this.url = url == null ? "" : url.trim();
    }

    public void show(Window owner) {
        if (url.isEmpty()) {
            showAlert(owner, Alert.AlertType.INFORMATION,
                    "Für dieses Programm ist noch kein Tutorial-Video hinterlegt.");
            return;
        }
        if (!isWebLink(url)) {
            showAlert(owner, Alert.AlertType.ERROR, "Ungültiger Tutorial-Link: " + url);
            return;
        }
        hostServices.showDocument(url);
    }

    static boolean isWebLink(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return uri.getHost() != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static void showAlert(Window owner, Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(owner);
        alert.setTitle("Tutorial");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
