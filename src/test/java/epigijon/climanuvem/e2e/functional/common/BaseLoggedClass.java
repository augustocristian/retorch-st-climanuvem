package epigijon.climanuvem.e2e.functional.common;

import epigijon.climanuvem.e2e.functional.pages.HomePage;
import epigijon.climanuvem.e2e.functional.pages.LoginPage;
import epigijon.climanuvem.e2e.functional.pages.WelcomePage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Base class for the ClimaNuvem browser system tests.
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
 * Override browser/login values via {@code -D...} system properties, matching
 * environment variables, or {@code src/test/resources/test.properties}.
 */
@SuppressWarnings("java:S5786") // must be public — subclasses live in a different package
public class BaseLoggedClass {

    protected static final Logger log = LoggerFactory.getLogger(BaseLoggedClass.class);

    protected static String frontendUrl;
    protected static String existingLoginEmail;
    protected static String existingLoginPassword;
    protected static String unknownLoginEmail;
    protected static String wrongLoginPassword;
    protected static String profileLoginEmail;
    protected static String profileLoginPassword;
    protected static String firebaseWebApiKey;

    protected WebDriver driver;

    @BeforeAll
    static void setUpClass() throws IOException {
        Properties props = new Properties();
        props.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));

        frontendUrl = configuredValue(props, "FRONTEND_URL", "http://localhost:5173");
        existingLoginEmail = configuredValue(props, "LOGIN_EXISTING_EMAIL", "");
        existingLoginPassword = configuredValue(props, "LOGIN_EXISTING_PASSWORD", "");
        unknownLoginEmail = configuredValue(props, "LOGIN_UNKNOWN_EMAIL", "missing-user@example.com");
        wrongLoginPassword = configuredValue(props, "LOGIN_WRONG_PASSWORD", "wrong-password");
        profileLoginEmail = configuredValue(props, "PROFILE_LOGIN_EMAIL", "");
        profileLoginPassword = configuredValue(props, "PROFILE_LOGIN_PASSWORD", "");
        if (profileLoginEmail == null || profileLoginEmail.trim().isEmpty()) {
            profileLoginEmail = existingLoginEmail;
        }
        if (profileLoginPassword == null || profileLoginPassword.trim().isEmpty()) {
            profileLoginPassword = existingLoginPassword;
        }
        firebaseWebApiKey = configuredValue(props, "FIREBASE_WEB_API_KEY", "");

        log.info("Frontend URL: {}", frontendUrl);
    }

    @BeforeEach
    void setUpTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito", "--disable-blink-features=AutomationControlled");
        if (isHeadless()) {
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        }

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        log.info("Browser started");
    }

    @AfterEach
    void tearDownTest() {
        if (driver != null) {
            driver.quit();
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

    protected HomePage loginAsProfileUser() {
        requireConfiguredProfileLogin();

        LoginPage loginPage = onWelcomePage()
                .clickLoginButton()
                .login(profileLoginEmail, profileLoginPassword);
        return loginPage.waitForHome();
    }

    protected void requireConfiguredEmailLogin() {
        if (existingLoginEmail == null || existingLoginEmail.trim().isEmpty()
                || existingLoginPassword == null || existingLoginPassword.trim().isEmpty()) {
            throw new AssertionError("Configure LOGIN_EXISTING_EMAIL and LOGIN_EXISTING_PASSWORD "
                    + "as system properties, environment variables, or test.properties values.");
        }
    }

    protected void requireConfiguredProfileLogin() {
        if (profileLoginEmail == null || profileLoginEmail.trim().isEmpty()
                || profileLoginPassword == null || profileLoginPassword.trim().isEmpty()) {
            throw new AssertionError("Configure PROFILE_LOGIN_EMAIL and PROFILE_LOGIN_PASSWORD "
                    + "or fallback LOGIN_EXISTING_EMAIL and LOGIN_EXISTING_PASSWORD.");
        }
    }

    protected void deleteFirebaseAccountIfConfigured(String email, String password) {
        if (firebaseWebApiKey == null || firebaseWebApiKey.trim().isEmpty()) {
            log.warn("Skipping Firebase account cleanup for {} because FIREBASE_WEB_API_KEY is not configured", email);
            return;
        }

        try {
            String signInResponse = postFirebaseAuth("accounts:signInWithPassword",
                    "{\"email\":\"" + json(email) + "\","
                            + "\"password\":\"" + json(password) + "\","
                            + "\"returnSecureToken\":true}");
            JsonObject signInJson = JsonParser.parseString(signInResponse).getAsJsonObject();
            String idToken = signInJson.get("idToken").getAsString();

            postFirebaseAuth("accounts:delete", "{\"idToken\":\"" + json(idToken) + "\"}");
            log.info("Deleted Firebase account created during test: {}", email);
        } catch (Exception e) {
            log.warn("Could not delete Firebase account created during test: {}", email, e);
        }
    }

    private static String configuredValue(Properties props, String key, String fallback) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        String envValue = System.getenv(key);
        if (envValue != null) {
            return envValue;
        }
        return props.getProperty(key, fallback);
    }

    private String postFirebaseAuth(String method, String body) throws IOException {
        URL url = new URL("https://identitytoolkit.googleapis.com/v1/" + method + "?key=" + firebaseWebApiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = connection.getResponseCode();
        InputStream responseStream = status >= 200 && status < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        String response = new String(readAllBytes(responseStream), StandardCharsets.UTF_8);
        if (status < 200 || status >= 300) {
            throw new IOException("Firebase Auth request failed with HTTP " + status + ": " + response);
        }
        return response;
    }

    private static byte[] readAllBytes(InputStream input) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        try (java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream()) {
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isHeadless() {
        return "true".equalsIgnoreCase(System.getProperty("CI"))
                || "true".equalsIgnoreCase(System.getenv("CI"));
    }
}
