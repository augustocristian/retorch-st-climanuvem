# EPI-ClimaNuvem — E2E Test Suite

End-to-end test suite for the [EPI-ClimaNuvem](https://gitlab.com/HP-SCDS/Observatorio/2025-2026/climanuvem/epi-climanuvem) application, built with the [RETORCH](https://github.com/giis-uniovi/retorch) framework.

Covers two test layers:
- **API tests** — HTTP-level tests against the FastAPI backend (no browser needed)
- **E2E tests** — Selenium + Page Object tests that drive the Expo web frontend in Chrome

---

## Prerequisites

| Tool | Minimum version |
|---|---|
| Java (JDK) | 8 |
| Maven | 3.8 |
| Docker + Docker Compose | 24 |
| Git | any recent |
| Chrome | any recent (for E2E tests) |

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

# E2E browser tests only (opens Chrome)
mvn test -Dtest="TestWelcomeScreen,TestLoginForm,TestHomeScreen,TestCaptureScreen"

# E2E tests headless (for CI or no monitor)
mvn test -Dtest="TestWelcomeScreen,TestLoginForm,TestHomeScreen,TestCaptureScreen" -DCI=true

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

All defaults work out of the box. Override via `-D` system properties or environment variables:

| Property | Default | Description |
|---|---|---|
| `SUT_URL` | `http://localhost:8000` | Backend base URL |
| `FRONTEND_URL` | `http://localhost:5173` | Frontend base URL (E2E tests) |
| `TEST_TOKEN` | `test-token-climanuvem` | Auth token injected by API tests |
| `TJOB_NAME` | `local` | Separates build outputs in CI |
| `CI` | _(unset)_ | Set to `true` for headless Chrome |

---

## Test architecture

```
src/test/java/epigijon/climanuvem/e2e/functional/
├── common/
│   ├── BaseApiClass.java       HTTP helpers, multipart upload, JSON fixtures
│   └── BaseLoggedClass.java    Browser lifecycle; onWelcomePage() / loginAsGuest()
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
    └── e2e/                    Selenium tests (Page Object pattern)
        ├── TestWelcomeScreen.java
        ├── TestLoginForm.java
        ├── TestHomeScreen.java
        └── TestCaptureScreen.java
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

- **Any Bearer token** is accepted — API tests use a fixed token; E2E tests use real Firebase anonymous tokens.
- **Firebase is not initialised** — no service account key is needed.
- **Ollama worker is disabled** — analyses stay in `analyzing` state, making cancel tests deterministic.
