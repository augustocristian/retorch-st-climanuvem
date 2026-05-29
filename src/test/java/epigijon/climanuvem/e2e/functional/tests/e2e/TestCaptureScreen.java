package epigijon.climanuvem.e2e.functional.tests.e2e;

import epigijon.climanuvem.e2e.functional.common.BaseLoggedClass;
import epigijon.climanuvem.e2e.functional.pages.CapturePage;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E tests for the Capture (image-upload) screen.
 * Each test logs in as a guest and navigates to the Capture screen.
 */
class TestCaptureScreen extends BaseLoggedClass {

    private CapturePage capturePage;

    @BeforeEach
    void navigate() {
        capturePage = loginAsGuest().clickAnalyzeImage();
    }

    @AccessMode(resID = "frontend",    concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend",     concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Capture screen shows camera and gallery upload options")
    void testUploadOptionsAreVisible() {
        Assertions.assertAll(
                () -> Assertions.assertTrue(capturePage.isCameraOptionVisible(),  "'Tomar Foto' must be visible"),
                () -> Assertions.assertTrue(capturePage.isGalleryOptionVisible(), "'Galería' must be visible")
        );
    }

    @AccessMode(resID = "frontend",    concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend",     concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Capture screen shows the explainability toggle, formats info, and page header")
    void testCaptureInfoElementsAreVisible() {
        Assertions.assertAll(
                () -> Assertions.assertTrue(capturePage.isExplainabilityVisible(), "Explicabilidad toggle must be visible"),
                () -> Assertions.assertTrue(capturePage.isFormatsInfoVisible(),    "Formats info must be visible"),
                () -> Assertions.assertTrue(capturePage.isPageHeaderVisible(),     "Page header must be visible")
        );
    }
}
