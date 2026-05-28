package epigijon.climanuvem.e2e.functional.tests.e2e;

import epigijon.climanuvem.e2e.functional.common.BaseLoggedClass;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/**
 * E2E tests for the Capture (image-upload) screen of the ClimaNuvem web app.
 * <p>
 * Each test logs in as an anonymous guest and navigates to the Capture screen.
 * The tests verify that the two upload options (camera and gallery), the
 * explainability toggle, and the informational text are all present.
 */
class TestCaptureScreen extends BaseLoggedClass {

    @BeforeEach
    void navigateToCapture() {
        loginAsGuest();
        navigation.clickAnalyzeImage();
        waiter.waitForCapturePage();
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Capture screen shows the 'Tomar Foto' camera option")
    void testCameraOptionIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Tomar Foto')]")).size() > 0,
                "'Tomar Foto' card must be visible on the Capture screen");
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Capture screen shows the 'Galería' gallery option")
    void testGalleryOptionIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Galería')]")).size() > 0,
                "'Galería' card must be visible on the Capture screen");
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Capture screen shows the explainability toggle")
    void testExplainabilityToggleIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Explicabilidad')]")).size() > 0,
                "The Explicabilidad toggle must be visible on the Capture screen");
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Capture screen shows the supported formats info message")
    void testFormatsInfoIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Formatos soportados')]")).size() > 0,
                "The 'Formatos soportados' info message must be visible on the Capture screen");
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Capture screen shows the page header 'Analizar Imagen'")
    void testCapturePageHeaderIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Analizar Imagen')]")).size() > 0,
                "The page header 'Analizar Imagen' must be visible on the Capture screen");
    }
}
