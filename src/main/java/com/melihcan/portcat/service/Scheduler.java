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
    private long generation = 0;               // erhöht bei jedem start/stop, verwirft alte Durchläufe

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
            if (scheduler.isShutdown()) {
                System.err.println("Scheduler wurde bereits beendet und kann nicht neu gestartet werden");
                return;
            }
            // Prüfen ob bereits läuft
            if (future != null && !future.isCancelled() && !future.isDone()) {
                System.out.println("Scheduler läuft bereits!");
                return;
            }

            long runId = ++generation;
            future = scheduler.scheduleWithFixedDelay(() -> refresh(runId), 0, 1, TimeUnit.SECONDS);

            System.out.println("Scheduler gestartet");
        }
    }

    private void refresh(long runId) {
        try {
            List<Connection> connections = loader.get();
            // Nach stop()/terminate() keine Ergebnisse mehr ausliefern, auch wenn der Durchlauf
            // schon lief (z.B. PowerShell- oder DNS-Aufruf, der nicht auf Interrupts reagiert)
            synchronized (lock) {
                if (runId != generation || Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
            onUpdate.accept(connections);
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Verbindungen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        synchronized (lock) {
            generation++;
            if (future != null) {
                future.cancel(false);
                future = null;
                System.out.println("Scheduler gestoppt");
            }
        }
    }

    /**
     * Beendet den Scheduler endgültig. Blockiert nicht: ein laufender Durchlauf wird unterbrochen
     * und sein Ergebnis verworfen. Der Thread ist ein Daemon und hält die JVM nicht am Leben.
     */
    public void terminate() {
        synchronized (lock) {
            generation++;
            if (future != null) {
                future.cancel(true);
                future = null;
            }
        }
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
            System.out.println("Scheduler beendet");
        }
    }
}
