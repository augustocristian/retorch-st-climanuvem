package epigijon.climanuvem.e2e.functional.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for the Login form.
 * <p>
 * Constructing this object waits until the email input is visible.
 */
public class LoginPage extends BasePage {

    private static final By EMAIL_INPUT      = inputByPlaceholder("Correo electrónico");
    private static final By PASSWORD_INPUT   = inputByPlaceholder("Contraseña");
    private static final By FORGOT_PASSWORD  = byPartialText("Olvidaste tu contraseña");
    private static final By REGISTER_LINK    = byPartialText("Regístrate");

    public LoginPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public boolean isEmailInputPresent()      { return isPresent(EMAIL_INPUT);     }
    public boolean isPasswordInputPresent()   { return isPresent(PASSWORD_INPUT);  }
    public boolean isForgotPasswordPresent()  { return isPresent(FORGOT_PASSWORD); }
    public boolean isRegisterLinkPresent()    { return isPresent(REGISTER_LINK);   }
    public String  getEmailValue()            { return inputValue(EMAIL_INPUT);     }

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Types into the email field. Returns {@code this} for fluent chaining. */
    public LoginPage enterEmail(String email) {
        fill(EMAIL_INPUT, email);
        return this;
    }

    /** Types into the password field. Returns {@code this} for fluent chaining. */
    public LoginPage enterPassword(String password) {
        fill(PASSWORD_INPUT, password);
        return this;
    }

    /** Clicks "¿No tienes cuenta? Regístrate" and waits for the Register form. */
    public RegisterPage clickRegisterLink() {
        click(REGISTER_LINK);
        return new RegisterPage(driver);
    }
}
