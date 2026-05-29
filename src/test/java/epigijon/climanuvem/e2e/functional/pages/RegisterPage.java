package epigijon.climanuvem.e2e.functional.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for the Register form.
 * <p>
 * Constructing this object waits until the username input is visible.
 */
public class RegisterPage extends BasePage {

    private static final By USERNAME_INPUT         = inputByPlaceholder("Nombre de usuario");
    private static final By EMAIL_INPUT            = inputByPlaceholder("Correo electrónico");
    private static final By PASSWORD_INPUT         = inputByPlaceholder("Contraseña");
    private static final By CONFIRM_PASSWORD_INPUT = inputByPlaceholder("Confirmar contraseña");
    private static final By LOGIN_LINK             = byPartialText("Inicia sesión");

    public RegisterPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public boolean isUsernameInputPresent()        { return isPresent(USERNAME_INPUT);         }
    public boolean isEmailInputPresent()           { return isPresent(EMAIL_INPUT);            }
    public boolean isPasswordInputPresent()        { return isPresent(PASSWORD_INPUT);         }
    public boolean isConfirmPasswordInputPresent() { return isPresent(CONFIRM_PASSWORD_INPUT); }
    public boolean isLoginLinkPresent()            { return isPresent(LOGIN_LINK);             }

    // ── Actions ───────────────────────────────────────────────────────────────

    public RegisterPage enterUsername(String username) {
        fill(USERNAME_INPUT, username);
        return this;
    }

    public RegisterPage enterEmail(String email) {
        fill(EMAIL_INPUT, email);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        fill(PASSWORD_INPUT, password);
        return this;
    }

    public RegisterPage enterConfirmPassword(String password) {
        fill(CONFIRM_PASSWORD_INPUT, password);
        return this;
    }

    /** Navigates back to the Login form. */
    public LoginPage clickLoginLink() {
        click(LOGIN_LINK);
        return new LoginPage(driver);
    }
}
