package epigijon.climanuvem.e2e.functional.common;

import epigijon.climanuvem.e2e.functional.pages.HomePage;
import epigijon.climanuvem.e2e.functional.pages.WelcomePage;
import giis.selema.manager.SelemaConfig;
import giis.selema.manager.SeleManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Base class for the ClimaNuvem browser (E2E) test suite.
 * <p>
 * Manages the Chrome WebDriver lifecycle: a fresh session is created before
 * each test and closed afterward, preventing any state leakage between tests.
 * <p>
 * Entry points for test methods:
 * <ul>
 *   <li>{@link #onWelcomePage()} — navigates to the root URL and returns a
 *       {@link WelcomePage}, which waits internally until the page is ready.</li>
 *   <li>{@link #loginAsGuest()} — convenience shortcut that clicks
 *       "Continuar como invitado" and returns a {@link HomePage}.</li>
 * </ul>
 * <p>
 * Set {@code -DCI=true} (or the {@code CI} env var) for headless Chrome in CI.
 * Override the frontend URL via {@code -DFRONTEND_URL=…} or the env var.
 */
@SuppressWarnings("java:S5786") // must be public — subclasses live in a different package
public class BaseLoggedClass {

    protected static final Logger log = LoggerFactory.getLogger(BaseLoggedClass.class);

    protected static String frontendUrl;

    protected WebDriver driver;
    private   SeleManager seleManager;

    @BeforeAll
    static void setUpClass() throws IOException {
        Properties props = new Properties();
        props.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));

        String envUrl = System.getProperty("FRONTEND_URL") != null
                ? System.getProperty("FRONTEND_URL")
                : System.getenv("FRONTEND_URL");
        frontendUrl = envUrl != null ? envUrl : props.getProperty("FRONTEND_URL", "http://localhost:5173");

        log.info("Frontend URL: {}", frontendUrl);
    }

    @BeforeEach
    void setUpTest() {
        String[] args = isHeadless()
                ? new String[]{"--incognito", "--headless", "--no-sandbox",
                               "--disable-dev-shm-usage",
                               "--disable-blink-features=AutomationControlled"}
                : new String[]{"--incognito",
                               "--disable-blink-features=AutomationControlled"};

        seleManager = new SeleManager(new SelemaConfig())
                .setBrowser("chrome")
                .setArguments(args)
                .setMaximize(true);

        driver = seleManager.createDriver();
        log.info("Browser started");
    }

    @AfterEach
    void tearDownTest() {
        if (seleManager != null && driver != null) {
            seleManager.quitDriver(driver);
            log.info("Browser closed");
        }
    }

    /**
     * Navigates to the frontend root and returns a {@link WelcomePage}.
     * The page object waits internally until the Welcome screen is ready.
     */
    protected WelcomePage onWelcomePage() {
        driver.get(frontendUrl);
        return new WelcomePage(driver);
    }

    /**
     * Clicks "Continuar como invitado" on the Welcome screen and returns a
     * {@link HomePage} once the Home screen has finished loading.
     * Firebase anonymous auth is invoked; the backend accepts the resulting
     * token because {@code TEST_MODE=true} accepts any Bearer token.
     */
    protected HomePage loginAsGuest() {
        return onWelcomePage().clickAnonymousLogin();
    }

    private boolean isHeadless() {
        return "true".equalsIgnoreCase(System.getProperty("CI"))
                || "true".equalsIgnoreCase(System.getenv("CI"));
    }
}
