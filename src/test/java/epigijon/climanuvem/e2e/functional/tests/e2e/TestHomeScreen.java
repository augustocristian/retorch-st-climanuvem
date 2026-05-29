package epigijon.climanuvem.e2e.functional.tests.e2e;

import epigijon.climanuvem.e2e.functional.common.BaseLoggedClass;
import epigijon.climanuvem.e2e.functional.pages.HomePage;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E tests for the Home screen.
 * Each test logs in as an anonymous guest before asserting.
 */
class TestHomeScreen extends BaseLoggedClass {

    private HomePage homePage;

    @BeforeEach
    void authenticate() {
        homePage = loginAsGuest();
    }

    @AccessMode(resID = "frontend",    concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend",     concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Home screen shows welcome message and all four quick-action cards")
    void testHomeElementsAreVisible() {
        Assertions.assertAll(
                () -> Assertions.assertTrue(homePage.isWelcomeMessageVisible(), "Welcome message must be visible"),
                () -> Assertions.assertTrue(homePage.isAnalyzeCardVisible(),    "'Analizar Imagen' card must be visible"),
                () -> Assertions.assertTrue(homePage.isHistoryCardVisible(),    "'Historial' card must be visible"),
                () -> Assertions.assertTrue(homePage.isLogoutCardVisible(),     "'Cerrar Sesión' card must be visible")
        );
    }

    @AccessMode(resID = "frontend",    concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend",     concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Clicking 'Analizar Imagen' opens the Capture screen")
    void testAnalyzeImageNavigatesToCapturePage() {
        Assertions.assertTrue(
                homePage.clickAnalyzeImage().isCameraOptionVisible(),
                "'Tomar Foto' option must appear on the Capture screen");
    }

    @AccessMode(resID = "frontend",    concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "backend",     concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("Clicking 'Cerrar Sesión' returns to the Welcome screen")
    void testLogoutReturnsToWelcomeScreen() {
        Assertions.assertTrue(
                homePage.clickLogout().isGuestButtonPresent(),
                "'Continuar como invitado' button must reappear after logout");
    }
}
