package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

//Mithilfe von KI geschrieben
public class Scheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "PortCat-Scheduler");
                t.setDaemon(true);  // Daemon-Thread verhindert, dass App hängt
                return t;
            });

    private ScheduledFuture<?> future;
    private final Supplier<List<Connection>> loader;
    private final Consumer<List<Connection>> onUpdate;
    private final Object lock = new Object();  // Für Thread-Sicherheit

    /**
     * @param loader   liefert die aktuellen Verbindungen (läuft im Scheduler-Thread)
     * @param onUpdate bekommt das Ergebnis jedes Durchlaufs (ebenfalls im Scheduler-Thread)
     */
    public Scheduler(Supplier<List<Connection>> loader, Consumer<List<Connection>> onUpdate) {
        this.loader = loader;
        this.onUpdate = onUpdate;
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
                    List<Connection> connections = loader.get();
                    onUpdate.accept(connections);
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
