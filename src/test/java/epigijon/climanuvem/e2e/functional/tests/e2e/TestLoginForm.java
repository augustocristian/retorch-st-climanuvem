package epigijon.climanuvem.e2e.functional.tests.e2e;

import epigijon.climanuvem.e2e.functional.common.BaseLoggedClass;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/**
 * E2E tests for the Login form of the ClimaNuvem web app.
 * <p>
 * These tests verify the structure and UI elements of the Login page.
 * No actual Firebase authentication is performed — tests only interact with
 * the DOM (form fields, buttons, and links).
 */
class TestLoginForm extends BaseLoggedClass {

    @BeforeEach
    void navigateToLogin() {
        navigation.clickLoginButton();
        waiter.waitForLoginPage();
    }

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Login form shows the email input field")
    void testEmailInputIsPresent() {
        Assertions.assertTrue(
                driver.findElements(By.cssSelector("input[placeholder='Correo electrónico']")).size() > 0,
                "Email input must be present on the Login page");
    }

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Login form shows the password input field")
    void testPasswordInputIsPresent() {
        Assertions.assertTrue(
                driver.findElements(By.cssSelector("input[placeholder='Contraseña']")).size() > 0,
                "Password input must be present on the Login page");
    }

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Login form shows the forgot-password link")
    void testForgotPasswordLinkIsPresent() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Olvidaste tu contraseña')]")).size() > 0,
                "Forgot-password link must be present");
    }

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Login form shows the register link")
    void testRegisterLinkIsPresent() {
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Regístrate')]")).size() > 0,
                "Register link must be present on the Login page");
    }

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Login form accepts typed text in the email input")
    void testEmailInputAcceptsText() {
        navigation.enterEmail("test@example.com");
        String value = driver.findElement(
                By.cssSelector("input[placeholder='Correo electrónico']")).getAttribute("value");
        Assertions.assertEquals("test@example.com", value,
                "Email input must reflect typed text");
    }

    @AccessMode(resID = "frontend", concurrency = 5, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Clicking the register link navigates to the Register form")
    void testRegisterLinkNavigatesToRegisterPage() {
        navigation.clickRegisterLink();
        waiter.waitForRegisterPage();
        Assertions.assertTrue(
                driver.findElements(By.cssSelector("input[placeholder='Nombre de usuario']")).size() > 0,
                "After clicking the register link, the username input must appear");
    }
}
