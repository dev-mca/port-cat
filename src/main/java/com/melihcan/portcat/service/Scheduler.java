package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;
import com.melihcan.portcat.gui.Gui;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.*;

public class Scheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "PortCat-Scheduler");
                t.setDaemon(true);  // Daemon-Thread verhindert, dass App hängt
                return t;
            });

    private ScheduledFuture<?> future;
    private final Gui gui;
    private final Object lock = new Object();  // Für Thread-Sicherheit

    public Scheduler(Gui gui) {
        this.gui = gui;
    }

    public void start() {
        synchronized (lock) {
            // Prüfen ob bereits läuft
            if (future != null && !future.isCancelled() && !future.isDone()) {
                System.out.println("Scheduler läuft bereits!");
                return;
            }

            future = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    List<Connection> daten = gui.ladeDaten();
                    Platform.runLater(() -> gui.updateUi(daten));
                } catch (Exception e) {
                    System.err.println("Fehler beim Laden der Verbindungen: " + e.getMessage());
                    e.printStackTrace();
                }
            }, 0, 1, TimeUnit.SECONDS);

            System.out.println("Scheduler gestartet");
        }
    }

    public void stop() {
        synchronized (lock) {
            if (future != null) {
                future.cancel(false);
                future = null;
                System.out.println("Scheduler gestoppt");
            }
        }
    }

    public void terminate() {
        synchronized (lock) {
            if (future != null) {
                future.cancel(true);
                future = null;
            }
        }

        scheduler.shutdownNow();

        // VERBESSERUNG: Warte bis zu 5 Sekunden auf sauberes Beenden
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("WARNUNG: Scheduler konnte nicht innerhalb von 5 Sekunden beendet werden");
                // Force shutdown
                scheduler.shutdownNow();

                // Nochmal warten
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.err.println("FEHLER: Scheduler antwortet nicht - möglicherweise hängende Threads");
                }
            } else {
                System.out.println("Scheduler sauber terminiert");
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupt während Scheduler-Shutdown");
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}