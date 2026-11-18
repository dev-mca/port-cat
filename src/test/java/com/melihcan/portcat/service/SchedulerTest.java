package com.melihcan.portcat.service;

import com.melihcan.portcat.model.Connection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerTest {

    /** Simuliert einen langsamen Aufruf, der (wie Prozess-IO oder DNS) Interrupts ignoriert. */
    private static List<Connection> slowLoad(CountDownLatch started, long millis) {
        started.countDown();
        long end = System.currentTimeMillis() + millis;
        while (System.currentTimeMillis() < end) {
            Thread.onSpinWait();
        }
        return List.of();
    }

    @Test
    void terminateDoesNotBlockWhileRefreshIsRunning() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        Scheduler scheduler = new Scheduler(() -> slowLoad(started, 2000), c -> { });

        scheduler.start();
        assertTrue(started.await(2, TimeUnit.SECONDS));

        long t0 = System.nanoTime();
        scheduler.terminate();
        long blockedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);

        assertTrue(blockedMs < 200, "terminate() blockierte " + blockedMs + " ms");
    }

    @Test
    void resultOfRunningRefreshIsDroppedAfterStop() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        AtomicInteger updates = new AtomicInteger();
        Scheduler scheduler = new Scheduler(() -> slowLoad(started, 300), c -> updates.incrementAndGet());

        scheduler.start();
        assertTrue(started.await(2, TimeUnit.SECONDS));
        scheduler.stop();

        Thread.sleep(600);  // laufender Durchlauf ist jetzt fertig
        assertEquals(0, updates.get());
        scheduler.terminate();
    }

    @Test
    void terminateCanBeCalledTwice() {
        Scheduler scheduler = new Scheduler(List::of, c -> { });
        scheduler.start();
        scheduler.terminate();
        scheduler.terminate();
    }
}
