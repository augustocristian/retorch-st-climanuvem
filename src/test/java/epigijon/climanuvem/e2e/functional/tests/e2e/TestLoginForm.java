package epigijon.climanuvem.e2e.functional.tests.e2e;

import epigijon.climanuvem.e2e.functional.common.BaseLoggedClass;
import epigijon.climanuvem.e2e.functional.pages.LoginPage;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E tests for the Login form.
 * No authentication performed — tests verify DOM structure and input behaviour.
 */
class TestLoginForm extends BaseLoggedClass {

    private LoginPage loginPage;

    @BeforeEach
    void openLoginPage() {
        loginPage = onWelcomePage().clickLoginButton();
    }

    @AccessMode(resID = "frontend",    concurrency = 5, sharing = true,  accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Login form shows email, password inputs and forgot-password link")
    void testFormElementsArePresent() {
        Assertions.assertAll(
                () -> Assertions.assertTrue(loginPage.isEmailInputPresent(),     "Email input must be present"),
                () -> Assertions.assertTrue(loginPage.isPasswordInputPresent(),  "Password input must be present"),
                () -> Assertions.assertTrue(loginPage.isForgotPasswordPresent(), "Forgot-password link must be present")
        );
    }

    @AccessMode(resID = "frontend",    concurrency = 5, sharing = true,  accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Login form email input accepts and reflects typed text")
    void testEmailInputAcceptsText() {
        loginPage.enterEmail("test@example.com");
        Assertions.assertEquals("test@example.com", loginPage.getEmailValue(),
                "Email input must reflect what was typed");
    }

    @AccessMode(resID = "frontend",    concurrency = 5, sharing = true,  accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @Test
    @DisplayName("Clicking the register link navigates to the Register form")
    void testRegisterLinkNavigatesToRegisterPage() {
        Assertions.assertTrue(
                loginPage.clickRegisterLink().isUsernameInputPresent(),
                "Username input must appear after clicking the register link");
    }
}
