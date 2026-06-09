# EPI-ClimaNuvem — Test Suite

Test suite for the [EPI-ClimaNuvem](https://gitlab.com/HP-SCDS/Observatorio/2025-2026/climanuvem/epi-climanuvem) application.

Covers two test layers:
- **API tests** — HTTP-level tests against the FastAPI backend (no browser needed)
- **Login system tests** — Selenium/JUnit tests that drive the Expo web frontend in Chrome

---

## Prerequisites

| Tool | Minimum version |
|---|---|
| Java (JDK) | 8 |
| Maven | 3.8 |
| Docker + Docker Compose | 24 |
| Git | any recent |
| Chrome | any recent (for Selenium tests) |

---

## Quick start

### 1 — Deploy the SUT

The deploy script clones the SUT from GitLab on first run, builds the Docker images, and waits until both the backend and frontend are ready.

```bash
# Linux / macOS
./deploy-local.sh

# Windows PowerShell
./deploy-local.ps1
```

Services started:

| Service | URL |
|---|---|
| Backend (FastAPI) | http://localhost:8000 |
| Frontend (Expo web) | http://localhost:5173 |

### 2 — Run the tests

```bash
# All tests
mvn test

# API tests only
mvn test -Dtest="TestApi*"

# Login system tests only (opens Chrome)
mvn test -Dtest="TestLoginSystem" -DLOGIN_EXISTING_EMAIL="user@example.com" -DLOGIN_EXISTING_PASSWORD="secret"

# Login system tests headless (for CI or no monitor)
mvn test -Dtest="TestLoginSystem" -DLOGIN_EXISTING_EMAIL="user@example.com" -DLOGIN_EXISTING_PASSWORD="secret" -DCI=true

# Account-creation system tests
mvn test -Dtest="TestRegisterSystem" -DLOGIN_EXISTING_EMAIL="user@example.com" -DCI=true

# Profile-configuration system tests
mvn test -Dtest="TestProfileSystem" -DPROFILE_LOGIN_EMAIL="verified-user@example.com" -DPROFILE_LOGIN_PASSWORD="secret" -DCI=true

# Single class
mvn test -Dtest=TestApiCancel
```

### 3 — Tear down

```bash
./deploy-local.sh --down    # Linux / macOS
./deploy-local.ps1 -Down    # Windows PowerShell
```

---

## Configuration

Set login credentials for the Selenium login tests in one of these places, in priority order:

1. Maven system properties, for example `-DLOGIN_EXISTING_EMAIL="user@example.com"`.
2. Environment variables with the same names.
3. `src/test/resources/test.properties`.

`LOGIN_EXISTING_EMAIL` and `LOGIN_EXISTING_PASSWORD` are required for the email-login success case. The negative-test credentials have safe defaults.

| Property | Default | Description |
|---|---|---|
| `SUT_URL` | `http://localhost:8000` | Backend base URL |
| `FRONTEND_URL` | `http://localhost:5173` | Frontend base URL (Selenium tests) |
| `TEST_TOKEN` | `test-token-climanuvem` | Auth token injected by API tests |
| `LOGIN_EXISTING_EMAIL` | _(required for email success cases)_ | Existing email account used by login system tests |
| `LOGIN_EXISTING_PASSWORD` | _(required for email success cases)_ | Correct password for `LOGIN_EXISTING_EMAIL` |
| `LOGIN_UNKNOWN_EMAIL` | `missing-user@example.com` | Unknown email used by negative login tests |
| `LOGIN_WRONG_PASSWORD` | `wrong-password` | Incorrect password used by negative login tests |
| `PROFILE_LOGIN_EMAIL` | `LOGIN_EXISTING_EMAIL` | Verified account used by authenticated profile tests |
| `PROFILE_LOGIN_PASSWORD` | `LOGIN_EXISTING_PASSWORD` | Password for `PROFILE_LOGIN_EMAIL` |
| `FIREBASE_WEB_API_KEY` | _(unset)_ | Optional Firebase Web API key used to delete the account created by `TestRegisterSystem` |
| `TJOB_NAME` | `local` | Separates build outputs in CI |
| `CI` | _(unset)_ | Set to `true` for headless Chrome |

`TestRegisterSystem` creates the successful-registration account with a unique email address and expects the email-verification dialog shown by the app. If `FIREBASE_WEB_API_KEY` is configured, the test deletes that Firebase Auth account at the end; otherwise the unique address prevents future test collisions.

---

## Test architecture

```
src/test/java/epigijon/climanuvem/e2e/functional/
├── common/
│   ├── BaseApiClass.java       HTTP helpers, multipart upload, JSON fixtures
│   └── BaseLoggedClass.java    Selenium browser lifecycle and login configuration
├── pages/                      Page Object Model — one class per screen
│   ├── BasePage.java           Shared wait, click, fill, isPresent helpers
│   ├── WelcomePage.java
│   ├── LoginPage.java
│   ├── RegisterPage.java
│   ├── HomePage.java
│   └── CapturePage.java
└── tests/
    ├── api/                    HTTP-level tests (no browser)
    │   ├── TestApiPing.java
    │   ├── TestApiAuth.java
    │   ├── TestApiAnalysis.java
    │   ├── TestApiHistory.java
    │   ├── TestApiDelete.java
    │   └── TestApiCancel.java
    └── e2e/                    Selenium system tests (Page Object pattern)
        ├── TestLoginSystem.java
        ├── TestRegisterSystem.java
        └── TestProfileSystem.java
```

Page object navigation is typed — every action returns the next screen:

```java
// No auth required
WelcomePage welcome = onWelcomePage();
LoginPage   login   = welcome.clickLoginButton();
RegisterPage reg    = login.clickRegisterLink();

// Authenticated flow
HomePage    home    = loginAsGuest();          // Firebase anonymous auth
CapturePage capture = home.clickAnalyzeImage();
WelcomePage back    = home.clickLogout();
```

---

## Test mode

The backend is started with `TEST_MODE=true` and `DISABLE_WORKER=true` (see `docker-compose.test.yml`):

- **Any Bearer token** is accepted — API tests use a fixed token; Selenium guest-login tests use real Firebase anonymous tokens.
- **Firebase is not initialised** — no service account key is needed.
- **Ollama worker is disabled** — analyses stay in `analyzing` state, making cancel tests deterministic.
