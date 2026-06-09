package epigijon.climanuvem.e2e.functional.tests.e2e;

import epigijon.climanuvem.e2e.functional.common.BaseLoggedClass;
import epigijon.climanuvem.e2e.functional.pages.ProfilePage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Selenium system tests for profile configuration, derived from the hierarchical
 * test design for guest and authenticated sessions.
 */
class TestProfileSystem extends BaseLoggedClass {

    private static final String USERNAME_0 = "";
    private static final String USERNAME_2 = "ab";
    private static final String USERNAME_20 = "perfilPrueba12345678";
    private static final String USERNAME_21 = "perfilPrueba123456789";

    @Test
    @DisplayName("Guest session - light theme can be selected")
    void guestCanSelectLightTheme() {
        ProfilePage profilePage = openGuestProfile().chooseLightTheme();

        Assertions.assertTrue(profilePage.hasStoredTheme("light"),
                "Guest profile must store the light theme preference");
    }

    @Test
    @DisplayName("Guest session - dark theme can be selected")
    void guestCanSelectDarkTheme() {
        ProfilePage profilePage = openGuestProfile().chooseDarkTheme();

        Assertions.assertTrue(profilePage.hasStoredTheme("dark"),
                "Guest profile must store the dark theme preference");
    }

    @Test
    @DisplayName("Guest session - system theme can be selected")
    void guestCanSelectSystemTheme() {
        ProfilePage profilePage = openGuestProfile().chooseSystemTheme();

        Assertions.assertTrue(profilePage.hasStoredTheme("system"),
                "Guest profile must store the system theme preference");
    }

    @Test
    @DisplayName("Guest session - English language can be selected")
    void guestCanSelectEnglishLanguage() {
        ProfilePage profilePage = openGuestProfile().chooseEnglishLanguage();

        Assertions.assertTrue(profilePage.hasStoredLanguage("en"),
                "Guest profile must store the English language preference");
    }

    @Test
    @DisplayName("Guest session - Spanish language can be selected")
    void guestCanSelectSpanishLanguage() {
        ProfilePage profilePage = openGuestProfile().chooseSpanishLanguage();

        Assertions.assertTrue(profilePage.hasStoredLanguage("es"),
                "Guest profile must store the Spanish language preference");
    }

    @Test
    @DisplayName("Guest session - system language can be selected")
    void guestCanSelectSystemLanguage() {
        ProfilePage profilePage = openGuestProfile().chooseSystemLanguage();

        Assertions.assertTrue(profilePage.hasStoredLanguage("system"),
                "Guest profile must store the system language preference");
    }

    @Test
    @DisplayName("Authenticated session - delete account opens confirmation and can be cancelled")
    void authenticatedDeleteAccountShowsConfirmationAndCanBeCancelled() {
        ProfilePage profilePage = openAuthenticatedProfile()
                .openDeleteAccountDialog();

        Assertions.assertTrue(profilePage.isDeleteConfirmVisible(),
                "Delete account must open a confirmation dialog");

        profilePage.cancelDeleteAccount();
        Assertions.assertFalse(profilePage.isDeleteConfirmVisible(),
                "Delete confirmation must close after cancelling");
    }

    @Test
    @DisplayName("Authenticated session - zero-character username is blocked")
    void authenticatedZeroCharacterUsernameIsBlocked() {
        ProfilePage profilePage = openAuthenticatedProfile()
                .setUsername(USERNAME_0);

        Assertions.assertFalse(profilePage.isSaveButtonEnabled(),
                "Zero-character username must keep the save action disabled");
    }

    @Test
    @DisplayName("Authenticated session - two-character username is blocked")
    void authenticatedTwoCharacterUsernameIsBlocked() {
        ProfilePage profilePage = openAuthenticatedProfile()
                .setUsername(USERNAME_2);

        Assertions.assertFalse(profilePage.isSaveButtonEnabled(),
                "Two-character username must keep the save action disabled");
    }

    @Test
    @DisplayName("Authenticated session - twenty-character username can be saved")
    void authenticatedTwentyCharacterUsernameCanBeSaved() {
        ProfilePage profilePage = openAuthenticatedProfile()
                .updateUsername(USERNAME_20)
                .waitForProfileFeedback();

        Assertions.assertTrue(profilePage.isUsernameSectionVisible(),
                "Twenty-character username must be accepted and keep the user on Profile");
    }

    @Test
    @DisplayName("Authenticated session - twenty-one-character username is blocked")
    void authenticatedTwentyOneCharacterUsernameIsBlocked() {
        ProfilePage profilePage = openAuthenticatedProfile()
                .setUsername(USERNAME_21);

        Assertions.assertFalse(profilePage.isSaveButtonEnabled(),
                "Twenty-one-character username must keep the save action disabled");
    }

    @Test
    @DisplayName("Authenticated session - light theme can be selected")
    void authenticatedCanSelectLightTheme() {
        ProfilePage profilePage = openAuthenticatedProfile().chooseLightTheme();

        Assertions.assertTrue(profilePage.hasStoredTheme("light"),
                "Authenticated profile must store the light theme preference");
    }

    @Test
    @DisplayName("Authenticated session - dark theme can be selected")
    void authenticatedCanSelectDarkTheme() {
        ProfilePage profilePage = openAuthenticatedProfile().chooseDarkTheme();

        Assertions.assertTrue(profilePage.hasStoredTheme("dark"),
                "Authenticated profile must store the dark theme preference");
    }

    @Test
    @DisplayName("Authenticated session - system theme can be selected")
    void authenticatedCanSelectSystemTheme() {
        ProfilePage profilePage = openAuthenticatedProfile().chooseSystemTheme();

        Assertions.assertTrue(profilePage.hasStoredTheme("system"),
                "Authenticated profile must store the system theme preference");
    }

    @Test
    @DisplayName("Authenticated session - English language can be selected")
    void authenticatedCanSelectEnglishLanguage() {
        ProfilePage profilePage = openAuthenticatedProfile().chooseEnglishLanguage();

        Assertions.assertTrue(profilePage.hasStoredLanguage("en"),
                "Authenticated profile must store the English language preference");
    }

    @Test
    @DisplayName("Authenticated session - Spanish language can be selected")
    void authenticatedCanSelectSpanishLanguage() {
        ProfilePage profilePage = openAuthenticatedProfile().chooseSpanishLanguage();

        Assertions.assertTrue(profilePage.hasStoredLanguage("es"),
                "Authenticated profile must store the Spanish language preference");
    }

    @Test
    @DisplayName("Authenticated session - system language can be selected")
    void authenticatedCanSelectSystemLanguage() {
        ProfilePage profilePage = openAuthenticatedProfile().chooseSystemLanguage();

        Assertions.assertTrue(profilePage.hasStoredLanguage("system"),
                "Authenticated profile must store the system language preference");
    }

    private ProfilePage openGuestProfile() {
        ProfilePage profilePage = loginAsGuest().clickProfile().waitForGuestProfile();
        Assertions.assertTrue(profilePage.isGuestPreferencesVisible(),
                "Guest profile must show guest preferences");
        return profilePage;
    }

    private ProfilePage openAuthenticatedProfile() {
        ProfilePage profilePage = loginAsProfileUser().clickProfile().waitForAuthenticatedProfile();
        Assertions.assertAll(
                () -> Assertions.assertTrue(profilePage.isUsernameSectionVisible(),
                        "Authenticated profile must show username configuration"),
                () -> Assertions.assertTrue(profilePage.isDeleteAccountVisible(),
                        "Authenticated profile must show delete-account action")
        );
        return profilePage;
    }
}
