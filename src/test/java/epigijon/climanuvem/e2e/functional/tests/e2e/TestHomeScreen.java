package epigijon.climanuvem.e2e.functional.tests.e2e;

import epigijon.climanuvem.e2e.functional.common.BaseLoggedClass;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/**
 * E2E tests for the Home screen of the ClimaNuvem web app.
 * <p>
 * Each test logs in as an anonymous guest before verifying Home screen elements.
 * Anonymous login triggers a real Firebase anonymous-auth call; the backend
 * accepts the resulting token because TEST_MODE=true accepts any Bearer token.
 */
class TestHomeScreen extends BaseLoggedClass {

    @BeforeEach
    void authenticateAsGuest() {
        loginAsGuest();
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Home screen shows a welcome message after anonymous login")
    void testWelcomeMessageIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Bienvenido')]")).size() > 0,
                "A welcome message must appear on the Home screen after login");
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Home screen shows the 'Analizar Imagen' quick-action card")
    void testAnalyzeImageCardIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Analizar Imagen')]")).size() > 0,
                "'Analizar Imagen' card must be visible on the Home screen");
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Home screen shows the 'Historial' quick-action card")
    void testHistoryCardIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Historial')]")).size() > 0,
                "'Historial' card must be visible on the Home screen");
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Home screen shows the 'Cerrar Sesión' quick-action card")
    void testLogoutCardIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Cerrar Sesión')]")).size() > 0,
                "'Cerrar Sesión' card must be visible on the Home screen");
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Clicking 'Analizar Imagen' navigates to the Capture screen")
    void testAnalyzeImageNavigatesToCapturePage() {
        navigation.clickAnalyzeImage();
        waiter.waitForCapturePage();
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Tomar Foto')]")).size() > 0,
                "After clicking 'Analizar Imagen', the Capture page must show 'Tomar Foto'");
    }

    @AccessMode(resID = "frontend", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Clicking 'Cerrar Sesión' returns the user to the Welcome screen")
    void testLogoutReturnsToWelcomeScreen() {
        navigation.clickLogout();
        waiter.waitForWelcomePage();
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Continuar como invitado')]")).size() > 0,
                "After logout, the Welcome screen must be shown with the anonymous-login button");
    }
}
