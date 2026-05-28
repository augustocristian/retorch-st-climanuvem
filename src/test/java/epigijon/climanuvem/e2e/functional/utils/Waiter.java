package epigijon.climanuvem.e2e.functional.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Explicit-wait helpers for the ClimaNuvem web UI.
 * <p>
 * LONG_WAIT_SECONDS (30 s) is used for page navigation transitions, especially
 * the anonymous-login flow which makes a real Firebase network call.
 * SHORT_WAIT_SECONDS (10 s) is used for element visibility checks within an
 * already-loaded page.
 */
public class Waiter {

    public static final int LONG_WAIT_SECONDS  = 30;
    public static final int SHORT_WAIT_SECONDS = 10;

    private final WebDriverWait shortWait;
    private final WebDriverWait longWait;

    public Waiter(WebDriver driver) {
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(SHORT_WAIT_SECONDS));
        this.longWait  = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT_SECONDS));
    }

    // Expo/React Native web renders Text as divs and TouchableOpacity as divs
    // with role="button". We locate elements by visible text content.

    private static By byText(String text) {
        return By.xpath("//*[normalize-space(.)='" + text + "']");
    }

    private static By byPartialText(String text) {
        return By.xpath("//*[contains(., '" + text + "')]");
    }

    // ── Page-level waits ─────────────────────────────────────────────────────

    /** Waits until the Welcome page is fully rendered (app logo visible). */
    public void waitForWelcomePage() {
        longWait.until(ExpectedConditions.visibilityOfElementLocated(byText("ClimaNuvem")));
    }

    /** Waits until the anonymous-login flow completes and the Home page is shown. */
    public void waitForHomePage() {
        // The home page shows the welcome card and the four quick-action cards.
        longWait.until(ExpectedConditions.visibilityOfElementLocated(byPartialText("Bienvenido")));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(byPartialText("Analizar Imagen")));
    }

    /** Waits until the Login form is rendered (email input present). */
    public void waitForLoginPage() {
        longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='Correo electrónico']")));
    }

    /** Waits until the Register form is rendered (username input present). */
    public void waitForRegisterPage() {
        longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='Nombre de usuario']")));
    }

    /** Waits until the Capture page is rendered ("Tomar Foto" card visible). */
    public void waitForCapturePage() {
        longWait.until(ExpectedConditions.visibilityOfElementLocated(byPartialText("Tomar Foto")));
    }

    /** Waits until the Profile page is rendered. */
    public void waitForProfilePage() {
        longWait.until(ExpectedConditions.visibilityOfElementLocated(byPartialText("Mi Perfil")));
    }

    // ── Element-level waits ───────────────────────────────────────────────────

    /** Waits for an element containing the given text to be visible and returns it. */
    public org.openqa.selenium.WebElement waitForText(String text) {
        return shortWait.until(ExpectedConditions.visibilityOfElementLocated(byText(text)));
    }

    /** Waits for an element containing partial text to be visible and returns it. */
    public org.openqa.selenium.WebElement waitForPartialText(String text) {
        return shortWait.until(ExpectedConditions.visibilityOfElementLocated(byPartialText(text)));
    }

    /** Waits for an input with the given placeholder to be visible and returns it. */
    public org.openqa.selenium.WebElement waitForInput(String placeholder) {
        return shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='" + placeholder + "']")));
    }

    /** Waits for text to disappear (e.g. a loading indicator). */
    public void waitForTextAbsent(String text) {
        shortWait.until(ExpectedConditions.invisibilityOfElementLocated(byText(text)));
    }
}
