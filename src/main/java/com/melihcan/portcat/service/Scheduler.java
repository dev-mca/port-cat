package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;
import com.melihcan.portcat.gui.Gui;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.*;

public class Scheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> future;   // <- merken!
    private final Gui gui;

    public Scheduler(Gui gui) {
        this.gui = gui;
    }

    public void start() {
        // nicht mehrfach starten
        if (future != null && !future.isCancelled() && !future.isDone()) return;

        future = scheduler.scheduleWithFixedDelay(() -> {
            List<Connection> daten = gui.ladeDaten();
            Platform.runLater(() -> gui.updateUi(daten));
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        if (future != null) {
            future.cancel(false);   // Task stoppen, Executor bleibt am Leben
            future = null;
        }
    }

    public void terminate() {
        scheduler.shutdownNow();    // nur beim App-Ende
    }
}