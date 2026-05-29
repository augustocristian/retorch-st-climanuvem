package epigijon.climanuvem.e2e.functional.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for the Home screen — shown after a successful login.
 * <p>
 * Constructing this object waits until both the welcome card and the
 * "Analizar Imagen" quick-action card are visible, confirming that the
 * authenticated home view has fully rendered.
 */
public class HomePage extends BasePage {

    private static final By WELCOME_MESSAGE  = byPartialText("Bienvenido");
    private static final By ANALYZE_CARD     = byPartialText("Analizar Imagen");
    private static final By HISTORY_CARD     = byPartialText("Historial");
    private static final By LOGOUT_CARD      = byPartialText("Cerrar Sesión");

    public HomePage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_MESSAGE));
        wait.until(ExpectedConditions.visibilityOfElementLocated(ANALYZE_CARD));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public boolean isWelcomeMessageVisible() { return isPresent(WELCOME_MESSAGE); }
    public boolean isAnalyzeCardVisible()    { return isPresent(ANALYZE_CARD);    }
    public boolean isHistoryCardVisible()    { return isPresent(HISTORY_CARD);    }
    public boolean isLogoutCardVisible()     { return isPresent(LOGOUT_CARD);     }

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Navigates to the Capture screen. */
    public CapturePage clickAnalyzeImage() {
        click(ANALYZE_CARD);
        return new CapturePage(driver);
    }

    /** Clicks "Cerrar Sesión" and waits for the Welcome screen to re-appear. */
    public WelcomePage clickLogout() {
        click(LOGOUT_CARD);
        return new WelcomePage(driver);
    }
}
