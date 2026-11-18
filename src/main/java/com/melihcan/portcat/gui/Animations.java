package com.melihcan.portcat.gui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Animationen des Output-Labels: roter/grüner Puls, blaues Aufblitzen und Lade-Punkte.
 */
//Puls-Effekte mithilfe von KI geschrieben
public class Animations {

    private static final String LOADING_TEXT = "Output: Lade Daten";

    private final Label label;

    private Timeline redPulse;
    private Timeline greenPulse;
    private boolean isGreenActive = false;
    private Timeline flashTimeline;
    private Timeline loadingAnimation;
    private boolean isLoading = false;

    public Animations(Label label) {
        this.label = label;
    }

    /** Startzustand: Label pulsiert rot. */
    public void startRedPulse() {
        DropShadow glow = createGlow(Color.rgb(180, 40, 40, 1.0));
        label.setEffect(glow);

        redPulse = createPulse(glow, Color.rgb(180, 40, 40, 0.25), Color.rgb(255, 57, 57, 0.85));
        redPulse.play();
    }

    /** Wechselt zwischen rotem und grünem Puls. */
    public void togglePulse() {
        DropShadow glow = createGlow(Color.rgb(57, 255, 20, 1.0));
        label.setEffect(glow);

        if (redPulse != null) {
            redPulse.stop();
        }
        if (greenPulse != null) {
            greenPulse.stop();
        }

        if (isGreenActive) {
            redPulse = createPulse(glow, Color.rgb(180, 40, 40, 0.25), Color.rgb(255, 57, 57, 0.85));
            redPulse.play();
            isGreenActive = false;
        } else {
            greenPulse = createPulse(glow, Color.rgb(57, 255, 20, 0.25), Color.rgb(57, 255, 20, 0.85));
            greenPulse.play();
            isGreenActive = true;
        }
    }

    /** Lässt das Label kurz blau aufblitzen. */
    public void flashBlue() {
        if (flashTimeline != null) {
            flashTimeline.stop();
        }

        label.getStyleClass().add("flash-blue");
        flashTimeline = new Timeline(
                new KeyFrame(Duration.millis(250), e -> label.getStyleClass().remove("flash-blue")));
        flashTimeline.setCycleCount(1);
        flashTimeline.play();
    }

    /** Zeigt "Lade Daten . . ." mit animierten Punkten an. */
    public void startLoading() {
        if (loadingAnimation != null) {
            loadingAnimation.stop();
        }

        isLoading = true;
        int[] dotCount = {0};

        loadingAnimation = new Timeline(
                new KeyFrame(Duration.millis(400), e -> {
                    if (!isLoading) {
                        return;
                    }
                    int dots = dotCount[0]++ % 4;
                    label.setText(LOADING_TEXT + " .".repeat(dots));
                }));

        loadingAnimation.setCycleCount(Animation.INDEFINITE);
        loadingAnimation.play();
    }

    public void stopLoading() {
        isLoading = false;

        if (loadingAnimation != null) {
            loadingAnimation.stop();
            loadingAnimation = null;
        }
    }

    private static DropShadow createGlow(Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setOffsetX(0);
        glow.setOffsetY(0);
        glow.setRadius(2);
        glow.setSpread(0.0);
        return glow;
    }

    private static Timeline createPulse(DropShadow glow, Color from, Color to) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 6, Interpolator.EASE_BOTH),
                        new KeyValue(glow.spreadProperty(), 0.05, Interpolator.EASE_BOTH),
                        new KeyValue(glow.colorProperty(), from, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(0.75),
                        new KeyValue(glow.radiusProperty(), 22, Interpolator.EASE_BOTH),
                        new KeyValue(glow.spreadProperty(), 0.35, Interpolator.EASE_BOTH),
                        new KeyValue(glow.colorProperty(), to, Interpolator.EASE_BOTH)));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);
        return timeline;
    }
}
