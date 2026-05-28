package epigijon.climanuvem.e2e.functional.common;

import epigijon.climanuvem.e2e.functional.utils.Navigation;
import epigijon.climanuvem.e2e.functional.utils.Waiter;
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
 * Sets up a Chrome WebDriver before each test and tears it down afterward.
 * Each test gets a fresh browser session so that state (auth, cookies) never
 * leaks between scenarios.
 * <p>
 * The frontend URL is read from {@code src/test/resources/test.properties}
 * (key {@code FRONTEND_URL}), defaulting to {@code http://localhost:5173}.
 * Override at runtime via {@code -DFRONTEND_URL=…} or the {@code FRONTEND_URL}
 * environment variable.
 * <p>
 * Set {@code -DCI=true} (or the {@code CI} env var) for headless Chrome in CI.
 */
@SuppressWarnings("java:S5786") // must be public — subclasses live in a different package
public class BaseLoggedClass {

    protected static final Logger log = LoggerFactory.getLogger(BaseLoggedClass.class);

    protected static String frontendUrl;
    protected static String tJobName;

    protected WebDriver  driver;
    protected Waiter     waiter;
    protected Navigation navigation;

    private SeleManager seleManager;

    @BeforeAll
    static void setUpClass() throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));

        tJobName = System.getProperty("TJOB_NAME");

        String envUrl = System.getProperty("FRONTEND_URL") != null
                ? System.getProperty("FRONTEND_URL")
                : System.getenv("FRONTEND_URL");
        frontendUrl = envUrl != null ? envUrl : properties.getProperty("FRONTEND_URL", "http://localhost:5173");

        log.info("Frontend URL: {}", frontendUrl);
    }

    @BeforeEach
    void setUpTest() {
        log.info("Setting up browser for E2E test");

        String[] args = isHeadless()
                ? new String[]{"--incognito", "--headless", "--no-sandbox", "--disable-dev-shm-usage",
                               "--disable-blink-features=AutomationControlled"}
                : new String[]{"--incognito", "--disable-blink-features=AutomationControlled"};

        seleManager = new SeleManager(new SelemaConfig())
                .setBrowser("chrome")
                .setArguments(args)
                .setMaximize(true);

        driver    = seleManager.createDriver();
        waiter    = new Waiter(driver);
        navigation = new Navigation(driver, frontendUrl);

        navigation.goToWelcome();
        waiter.waitForWelcomePage();
    }

    @AfterEach
    void tearDownTest() {
        if (seleManager != null && driver != null) {
            seleManager.quitDriver(driver);
            log.info("Browser closed");
        }
    }

    private boolean isHeadless() {
        return "true".equalsIgnoreCase(System.getProperty("CI"))
                || "true".equalsIgnoreCase(System.getenv("CI"));
    }

    /**
     * Performs anonymous login ("Continuar como invitado") and waits for the Home page.
     * Firebase anonymous auth is called; the backend accepts the resulting token
     * because TEST_MODE=true accepts any Bearer token.
     * Call this at the start of any test that requires an authenticated session.
     */
    protected void loginAsGuest() {
        navigation.clickAnonymousLogin();
        waiter.waitForHomePage();
        log.debug("Logged in as anonymous guest");
    }
}
