package epigijon.climanuvem.e2e.functional.tests.e2e;

import epigijon.climanuvem.e2e.functional.common.BaseLoggedClass;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/**
 * E2E tests for the Welcome (root) screen of the ClimaNuvem web app.
 * <p>
 * No authentication is required — the Welcome screen is the first page shown
 * to any visitor. These tests verify that the core branding elements and the
 * two main call-to-action buttons are present and visible.
 */
class TestWelcomeScreen extends BaseLoggedClass {

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Welcome screen shows the ClimaNuvem app title")
    void testAppTitleIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(text(),'ClimaNuvem')]")).size() > 0,
                "App title 'ClimaNuvem' must be visible on the Welcome screen");
    }

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Welcome screen shows the app tagline")
    void testTaglineIsVisible() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(text(),'Meteorólogo de bolsillo')]")).size() > 0,
                "Tagline 'Meteorólogo de bolsillo' must be visible");
    }

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Welcome screen shows the 'Iniciar Sesión' login button")
    void testLoginButtonIsPresent() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Iniciar Sesión')]")).size() > 0,
                "'Iniciar Sesión' button must be present on the Welcome screen");
    }

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Welcome screen shows the 'Continuar como invitado' anonymous-login button")
    void testAnonymousLoginButtonIsPresent() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Continuar como invitado')]")).size() > 0,
                "'Continuar como invitado' button must be present on the Welcome screen");
    }

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Clicking 'Iniciar Sesión' on the Welcome screen navigates to the Login form")
    void testLoginButtonNavigatesToLoginPage() {
        navigation.clickLoginButton();
        waiter.waitForLoginPage();
        Assertions.assertTrue(
                driver.findElements(By.cssSelector("input[placeholder='Correo electrónico']")).size() > 0,
                "After clicking 'Iniciar Sesión', the email input must appear");
    }
}
