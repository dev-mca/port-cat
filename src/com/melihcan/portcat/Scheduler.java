package com.melihcan.portcat;

import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private final Gui gui;

    public Scheduler(Gui gui) {
        this.gui = gui;
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(() -> {
            // läuft im Worker-Thread
            List<Connection> daten = gui.ladeDaten();
            Platform.runLater(() -> gui.updateUi(daten));
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}