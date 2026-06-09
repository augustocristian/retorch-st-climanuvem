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
    @DisplayName("BASE - Existing email with empty password is rejected")
    void baseExistingEmailWithEmptyPasswordIsRejected() {
        requireConfiguredEmailLogin();

        LoginPage loginPage = onWelcomePage()
                .clickLoginButton()
                .login(existingLoginEmail, "");

        Assertions.assertTrue(
                loginPage.waitForLoginFailure().hasLoginErrorOrValidation(),
                "Existing email with empty password must remain on Login and show validation or an error");
    }

    @Test
    @DisplayName("2 - Guest login reaches Home")
    void guestLoginReachesHome() {
        HomePage homePage = loginAsGuest();

        Assertions.assertTrue(
                homePage.isWelcomeMessageVisible(),
                "Guest login must reach the Home screen");
    }

    @Test
    @DisplayName("3 - Google login provider flow starts")
    void googleLoginProviderFlowStarts() {
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
    @DisplayName("4 - Unknown email with empty password is rejected")
    void unknownEmailWithEmptyPasswordIsRejected() {
        LoginPage loginPage = onWelcomePage()
                .clickLoginButton()
                .login(unknownLoginEmail, "");

        Assertions.assertTrue(
                loginPage.waitForLoginFailure().hasLoginErrorOrValidation(),
                "Unknown email with empty password must remain on Login and show validation or an error");
    }

    @Test
    @DisplayName("5 - Empty email and empty password are rejected")
    void emptyEmailAndEmptyPasswordAreRejected() {
        LoginPage loginPage = onWelcomePage()
                .clickLoginButton()
                .login("", "");

        Assertions.assertTrue(
                loginPage.waitForLoginFailure().hasLoginErrorOrValidation(),
                "Empty credentials must remain on Login and show validation");
    }

    @Test
    @DisplayName("6 - Existing email with correct password reaches Home")
    void existingEmailWithCorrectPasswordReachesHome() {
        requireConfiguredEmailLogin();

        HomePage homePage = onWelcomePage()
                .clickLoginButton()
                .login(existingLoginEmail, existingLoginPassword)
                .waitForHome();

        Assertions.assertTrue(
                homePage.isWelcomeMessageVisible(),
                "Existing email with correct password must reach the Home screen");
    }

    @Test
    @DisplayName("7 - Existing email with incorrect password is rejected")
    void existingEmailWithIncorrectPasswordIsRejected() {
        requireConfiguredEmailLogin();

        LoginPage loginPage = onWelcomePage()
                .clickLoginButton()
                .login(existingLoginEmail, wrongLoginPassword);

        Assertions.assertTrue(
                loginPage.waitForLoginFailure().hasLoginErrorOrValidation(),
                "Existing email with incorrect password must remain on Login and show an error");
    }
}
