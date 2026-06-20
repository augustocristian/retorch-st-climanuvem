package epigijon.climanuvem.e2e.functional.tests.e2e;

import epigijon.climanuvem.e2e.functional.common.BaseLoggedClass;
import epigijon.climanuvem.e2e.functional.pages.HomePage;
import epigijon.climanuvem.e2e.functional.pages.LoginPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Selenium system tests for the login functionality, derived from the Base
 * Choice table for guest, Google, email and password alternatives.
 */
class TestLoginSystem extends BaseLoggedClass {

    @Test
    @DisplayName("BASE - Guest and Google login flows are available")
    void guestAndGoogleLoginFlowsAreAvailable() {
        HomePage guestHomePage = loginAsGuest();
        Assertions.assertTrue(
                guestHomePage.isWelcomeMessageVisible(),
                "Guest login must reach the Home screen");
        guestHomePage.clickLogout();

        LoginPage loginPage = onWelcomePage().clickLoginButton();

        Assertions.assertAll(
                () -> Assertions.assertTrue(
                        loginPage.isGoogleLoginPresent(),
                        "Google login option must be available"),
                () -> Assertions.assertTrue(
                        loginPage.clickGoogleLoginStartsProvider(),
                        "Clicking Google login must start the provider flow")
        );
    }

    @Test
    @DisplayName("2 - Existing email with correct password reaches Home")
    void existingEmailWithCorrectPasswordReachesHome() {
        requireConfiguredEmailLogin();

        HomePage emailHomePage = onWelcomePage()
                .clickLoginButton()
                .login(existingLoginEmail, existingLoginPassword)
                .waitForHome();
        Assertions.assertTrue(
                emailHomePage.isWelcomeMessageVisible(),
                "Existing email with correct password must reach the Home screen");
    }

    @Test
    @DisplayName("3 - Invalid email/password login attempts are rejected")
    void invalidEmailPasswordLoginAttemptsAreRejected() {
        requireConfiguredEmailLogin();

        Assertions.assertAll(
                () -> assertLoginRejected(existingLoginEmail, "",
                        "Existing email with empty password must remain on Login and show validation or an error"),
                () -> assertLoginRejected(unknownLoginEmail, "",
                        "Unknown email with empty password must remain on Login and show validation or an error"),
                () -> assertLoginRejected("", "",
                        "Empty credentials must remain on Login and show validation"),
                () -> assertLoginRejected(existingLoginEmail, wrongLoginPassword,
                        "Existing email with incorrect password must remain on Login and show an error")
        );
    }

    private void assertLoginRejected(String email, String password, String message) {
        LoginPage loginPage = onWelcomePage()
                .clickLoginButton()
                .login(email, password);

        Assertions.assertTrue(
                loginPage.waitForLoginFailure().hasLoginErrorOrValidation(),
                message);
    }
}
