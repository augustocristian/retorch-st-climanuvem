package epigijon.climanuvem.e2e.functional.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Navigation helpers for the ClimaNuvem web UI.
 * <p>
 * Every method drives a browser action (URL load or click) and does NOT wait for
 * the resulting page — callers must follow each navigation call with the appropriate
 * {@link Waiter} method so that the test fails with a clear message if the page
 * never loads.
 */
public class Navigation {

    private final WebDriver driver;
    private final String frontendUrl;
    private final WebDriverWait wait;

    public Navigation(WebDriver driver, String frontendUrl) {
        this.driver      = driver;
        this.frontendUrl = frontendUrl;
        this.wait        = new WebDriverWait(driver, Duration.ofSeconds(Waiter.LONG_WAIT_SECONDS));
    }

    // ── URL navigation ────────────────────────────────────────────────────────

    /** Navigate to the root of the frontend (Welcome screen). */
    public void goToWelcome() {
        driver.get(frontendUrl);
    }

    // ── Welcome-screen actions ────────────────────────────────────────────────

    /**
     * Clicks "Iniciar Sesión" on the Welcome screen to open the Login form.
     */
    public void clickLoginButton() {
        clickByPartialText("Iniciar Sesión");
    }

    /**
     * Clicks "Continuar como invitado" on the Welcome screen.
     * Triggers Firebase anonymous authentication and navigates to Home.
     */
    public void clickAnonymousLogin() {
        clickByPartialText("Continuar como invitado");
    }

    // ── Login-form actions ────────────────────────────────────────────────────

    /** Types into the email input on the Login / Register form. */
    public void enterEmail(String email) {
        fillInput("Correo electrónico", email);
    }

    /** Types into the password input on the Login / Register form. */
    public void enterPassword(String password) {
        fillInput("Contraseña", password);
    }

    /** Clicks "Iniciar Sesión" submit button on the Login form. */
    public void submitLogin() {
        clickByPartialText("Iniciar Sesión");
    }

    /** Clicks "¿No tienes cuenta? Regístrate" on the Login form. */
    public void clickRegisterLink() {
        clickByPartialText("Regístrate");
    }

    // ── Register-form actions ─────────────────────────────────────────────────

    /** Types into the username input on the Register form. */
    public void enterUsername(String username) {
        fillInput("Nombre de usuario", username);
    }

    // ── Home-screen actions ───────────────────────────────────────────────────

    /** Clicks the "Analizar Imagen" quick-action card on the Home screen. */
    public void clickAnalyzeImage() {
        clickByPartialText("Analizar Imagen");
    }

    /** Clicks the "Historial" quick-action card on the Home screen. */
    public void clickHistory() {
        clickByPartialText("Historial");
    }

    /** Clicks the "Cerrar Sesión" quick-action card on the Home screen. */
    public void clickLogout() {
        clickByPartialText("Cerrar Sesión");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void clickByPartialText(String text) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(., '" + text + "')]")));
        el.click();
    }

    private void fillInput(String placeholder, String value) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='" + placeholder + "']")));
        input.clear();
        input.sendKeys(value);
    }
}
