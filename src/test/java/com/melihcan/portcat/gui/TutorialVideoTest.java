package com.melihcan.portcat.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TutorialVideoTest {

    @Test
    void acceptsHttpAndHttpsLinks() {
        assertTrue(TutorialVideo.isWebLink("https://www.youtube.com/watch?v=abc123"));
        assertTrue(TutorialVideo.isWebLink("http://example.com/video.mp4"));
    }

    @Test
    void rejectsOtherLinks() {
        assertFalse(TutorialVideo.isWebLink("file:///etc/passwd"));
        assertFalse(TutorialVideo.isWebLink("javascript:alert(1)"));
        assertFalse(TutorialVideo.isWebLink("not a link"));
        assertFalse(TutorialVideo.isWebLink("https://"));
    }
}
