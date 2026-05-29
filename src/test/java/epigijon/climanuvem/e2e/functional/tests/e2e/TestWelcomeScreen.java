package epigijon.climanuvem.e2e.functional.tests.e2e;

import epigijon.climanuvem.e2e.functional.common.BaseLoggedClass;
import epigijon.climanuvem.e2e.functional.pages.WelcomePage;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E tests for the Welcome (root) screen.
 * No authentication required.
 */
class TestWelcomeScreen extends BaseLoggedClass {

    @AccessMode(resID = "frontend",    concurrency = 5, sharing = true,  accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Welcome screen shows the app title and tagline")
    void testBrandingIsVisible() {
        WelcomePage page = onWelcomePage();
        Assertions.assertAll(
                () -> Assertions.assertTrue(page.isAppTitleVisible(), "App title must be visible"),
                () -> Assertions.assertTrue(page.isTaglineVisible(),  "Tagline must be visible")
        );
    }

    @AccessMode(resID = "frontend",    concurrency = 5, sharing = true,  accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Welcome screen shows both call-to-action buttons")
    void testCallToActionButtonsArePresent() {
        WelcomePage page = onWelcomePage();
        Assertions.assertAll(
                () -> Assertions.assertTrue(page.isLoginButtonPresent(), "'Iniciar Sesión' button must be present"),
                () -> Assertions.assertTrue(page.isGuestButtonPresent(), "'Continuar como invitado' button must be present")
        );
    }

    @AccessMode(resID = "frontend",    concurrency = 5, sharing = true,  accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Clicking 'Iniciar Sesión' opens the Login form")
    void testLoginButtonNavigatesToLoginPage() {
        Assertions.assertTrue(
                onWelcomePage().clickLoginButton().isEmailInputPresent(),
                "Email input must appear after clicking 'Iniciar Sesión'");
    }
}
