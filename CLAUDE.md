# CLAUDE.md — retorch-st-climanuvem

This file gives Claude Code the full context needed to work in this repository without re-deriving it each session.

> **MANDATORY RULE — ALWAYS UPDATE THIS FILE**
> Every time you add, remove, or rename a test class or page object, change a package name, modify the API surface, alter backend configuration, add a dependency, or change deployment behaviour, you **must** update the relevant sections of this file in the same operation. Never leave CLAUDE.md describing state that no longer matches the code. If a section becomes outdated it is worse than no documentation.

---

## Project purpose

Test suite for **EPI-ClimaNuvem**, a FastAPI + PostgreSQL backend and React Native/Expo web frontend that classifies cloud types in photographs using an Ollama LLM. The suite covers two complementary layers:

- **API tests** — HTTP-level tests that call the REST endpoints directly (no browser, no Ollama required).
- **Login system tests** — Selenium/JUnit tests that drive the Expo web frontend through Chrome, using the **Page Object pattern**. These tests do not use RETORCH annotations.

---

## System Under Test (SUT)

The SUT is **not committed** to this repository. The deploy scripts clone it automatically on first run.

- **GitLab repo:** `https://gitlab.com/HP-SCDS/Observatorio/2025-2026/climanuvem/epi-climanuvem`
- **Cloned into:** `epi-climanuvem/` (local only, git-ignored)

### Starting the SUT locally

```bash
# Linux/macOS
./deploy-local.sh

# Windows PowerShell
./deploy-local.ps1
```

Both scripts:
1. Clone `epi-climanuvem` from GitLab if not already present
2. Build `docker-compose.test.yml`
3. Start all services and wait for backend (`/ping`) and frontend (HTTP 200)

To stop:

```bash
./deploy-local.sh --down
./deploy-local.ps1 -Down
```

### Architecture

| Service | Container name | Port | Purpose |
|---|---|---|---|
| **db** | climanuvem_test_db | internal | PostgreSQL 15 |
| **backend** | climanuvem_test_backend | **8000** | FastAPI REST API |
| **frontend** | climanuvem_test_frontend | **5173** | Expo web build (React Native for Web) |

Ollama is omitted (`DISABLE_WORKER=true`) — no GPU required.

### REST API surface

**Public (no auth):**

| Method | Path | Description |
|---|---|---|
| GET | `/ping` | Liveness probe — `{"ping":"pong"}` |
| GET | `/` | Root — `{"Hello":"World"}` |

**Authenticated (Bearer token required):**

| Method | Path | Description |
|---|---|---|
| GET | `/test` | Echo user info |
| POST | `/analysis/upload` | Upload image (multipart), creates analysis record |
| GET | `/analysis/history` | List all analyses for the authenticated user |
| DELETE | `/analysis/{id}` | Delete a single analysis |
| DELETE | `/analysis/user-data` | Delete all analyses for the authenticated user |
| PATCH | `/analysis/{id}/cancel` | Cancel an in-progress analysis |

**HTTP status conventions:**
- No `Authorization` header → `403` (FastAPI HTTPBearer default)
- Invalid/unknown token → `401`
- Resource not found → `404`
- Logic conflict (e.g., cancel already-cancelled) → `400`

### Database schema

```
analysis (id, uid, image_path, datetime, location, latitude, longitude, is_anon, status)
clouds   (id, name, forecast, warning, warning_level)
analysis_cloud (id, analysis_id→analysis, cloud_id→clouds, confidence, box_ymin, box_xmin, box_ymax, box_xmax)
```

`status` values: `'analyzing'` | `'completed'` | `'cancelled'`

---

## Test-mode configuration

The SUT uses Firebase ID tokens in production. For testing, environment variables in `docker-compose.test.yml` activate a bypass:

| Variable | Value | Effect |
|---|---|---|
| `TEST_MODE` | `true` | Firebase SDK is not initialized; **any** Bearer token is accepted |
| `TEST_TOKEN` | `test-token-climanuvem` | Token used by API tests |
| `TEST_USER_UID` | `test-user-e2e` | UID injected for all authenticated requests |
| `DISABLE_WORKER` | `true` | Ollama worker does not start; analyses stay in `analyzing` |
| `FIREBASE_KEY_PATH` | _(empty)_ | Skipped in test mode |

`TEST_MODE=true` accepts **any** non-empty Bearer token so that:
- API tests send the fixed `TEST_TOKEN`.
- Selenium guest-login tests send real Firebase anonymous tokens obtained via "Continuar como invitado".

**Modified backend files (inside `epi-climanuvem/backend/`):**
- `app/infrastructure/config.py` — declares the four test-mode vars
- `app/infrastructure/firebase_service.py` — skips `initialize_app` when `TEST_MODE=true`
- `app/presentation/dependencies/auth_dependency.py` — accepts any token in test mode
- `app/main.py` — conditionally starts the background worker

---

## Repository layout

```
retorch-st-climanuvem/
├── epi-climanuvem/                     ← SUT (cloned from GitLab, not committed here)
│   ├── backend/
│   │   ├── app/
│   │   │   ├── main.py
│   │   │   ├── infrastructure/
│   │   │   │   ├── config.py           ← TEST_MODE, TEST_TOKEN, DISABLE_WORKER
│   │   │   │   ├── firebase_service.py
│   │   │   │   └── database/
│   │   │   ├── presentation/
│   │   │   │   ├── dependencies/auth_dependency.py
│   │   │   │   └── routes/
│   │   │   │       ├── analysis_routes.py
│   │   │   │       └── test_routes.py
│   │   │   └── business/
│   │   │       ├── analysis_service.py
│   │   │       └── worker.py
│   │   └── Dockerfile
│   └── frontend/                       ← React Native/Expo web app
│       └── Dockerfile
│
├── src/test/java/epigijon/climanuvem/e2e/functional/
│   ├── common/
│   │   ├── BaseApiClass.java           ← HTTP plumbing, auth token, multipart upload, fixtures
│   │   └── BaseLoggedClass.java        ← Selenium browser lifecycle and login configuration
│   ├── pages/                          ← Page Object Model (one class per screen)
│   │   ├── BasePage.java               ← Shared WebDriver, wait, isPresent/click/fill helpers
│   │   ├── WelcomePage.java            ← Root screen — clickLoginButton() / clickAnonymousLogin()
│   │   ├── LoginPage.java              ← Login form — email/password submit, Google provider start, failures
│   │   ├── RegisterPage.java           ← Register form — enterUsername/Email/Password()
│   │   ├── HomePage.java               ← Home screen — clickAnalyzeImage() / clickLogout()
│   │   └── CapturePage.java            ← Capture screen — query methods only
│   └── tests/
│       ├── api/                        ← API (HTTP-level) tests
│       │   ├── TestApiPing.java        ← GET /ping, GET /
│       │   ├── TestApiAuth.java        ← Auth enforcement (403/401)
│       │   ├── TestApiAnalysis.java    ← POST /analysis/upload
│       │   ├── TestApiHistory.java     ← GET /analysis/history
│       │   ├── TestApiDelete.java      ← DELETE /analysis/{id}, /user-data
│       │   └── TestApiCancel.java      ← PATCH /analysis/{id}/cancel
│       └── e2e/                        ← Selenium/JUnit system tests
│           ├── TestLoginSystem.java    ← Base Choice login coverage
│           ├── TestRegisterSystem.java ← Base Choice account-creation coverage
│           └── TestProfileSystem.java  ← Hierarchical profile-configuration coverage
│
├── src/test/resources/
│   ├── test.properties                 ← URLs, TEST_TOKEN, login test defaults
│   └── log4j2.xml
│
├── .retorch/
│   └── configurations/
│       ├── ClimaNuvemSystemResources.json  ← RETORCH resource model
│       └── retorchCI.properties
│
├── docker-compose.test.yml             ← Test environment (db + backend + frontend)
├── deploy-local.sh                     ← Linux/macOS: clone SUT and start all services
├── deploy-local.ps1                    ← Windows PowerShell: clone SUT and start all services
└── pom.xml                             ← Maven build (httpmime for multipart upload)
```

---

## Key dependencies

| Dependency | Purpose |
|---|---|
| JUnit 5 (Jupiter) | Test runner |
| Apache HttpClient 4.5.14 | HTTP client for API requests |
| Apache HttpMime 4.5.14 | Multipart entity builder (image upload) |
| Gson 2.14.0 | JSON parsing |
| Selenium 4.44.0 | Browser automation for login system tests |
| RETORCH annotations | `@AccessMode` resource declarations used by API tests |
| Log4j2 + SLF4J | Structured logging |
| `javax.imageio.ImageIO` (JDK) | Creates minimal 10×10 JPEG test images in memory |

---

## RETORCH resource model

| Resource ID | Represents | Typical access |
|---|---|---|
| `backend` | FastAPI service | `READONLY, concurrency=10, sharing=true` |
| `analysis` | Analysis records in the database | `READWRITE, concurrency=1, sharing=false` |
| `frontend` | Expo web frontend | `READONLY, concurrency=5, sharing=true` |
| `web-browser` | Chrome WebDriver instance | `READWRITE, concurrency=1, sharing=false` |

---

## Configuration

### `src/test/resources/test.properties`
```properties
LOCALHOST_URL=http://localhost:8000
FRONTEND_URL=http://localhost:5173
TEST_TOKEN=test-token-climanuvem
LOGIN_UNKNOWN_EMAIL=missing-user@example.com
LOGIN_WRONG_PASSWORD=wrong-password
PROFILE_LOGIN_EMAIL=
PROFILE_LOGIN_PASSWORD=
FIREBASE_WEB_API_KEY=
```
All values can be overridden via system properties (`-DSUT_URL=…`, `-DTEST_TOKEN=…`, `-DFRONTEND_URL=…`) or matching environment variables. The email success cases in `TestLoginSystem` require `LOGIN_EXISTING_EMAIL` and `LOGIN_EXISTING_PASSWORD`; `TestRegisterSystem` uses `LOGIN_EXISTING_EMAIL` for the "email already in use" case. `TestProfileSystem` uses `PROFILE_LOGIN_EMAIL` and `PROFILE_LOGIN_PASSWORD` for authenticated profile tests, falling back to the login credentials when profile-specific values are not set. The successful registration case expects the email-verification dialog, and if `FIREBASE_WEB_API_KEY` is configured, the created account is deleted through Firebase Auth REST after the test.

### `src/test/resources/log4j2.xml`
Logs to `target/testlogs/log${sys:TJOB_NAME:-testinglocal}-test.log`. The `epigijon` logger runs at DEBUG level.

### `pom.xml` — per-TJob build directory
Falls back to `target/local` when `TJOB_NAME` is not set (local-execution profile).

---

## Running tests

### Local — full suite

```bash
# 1. Start the SUT (clones from GitLab on first run, waits for both backend and frontend)
./deploy-local.sh        # Linux
./deploy-local.ps1       # Windows

# 2. Run all tests
mvn test

# 3. Run only API tests
mvn test -Dtest="TestApi*"

# 4. Run only login system tests (add -DCI=true for headless)
mvn test -Dtest="TestLoginSystem" -DLOGIN_EXISTING_EMAIL="<email>" \
         -DLOGIN_EXISTING_PASSWORD="<password>" -DCI=true

# 5. Run only account-creation system tests
mvn test -Dtest="TestRegisterSystem" -DLOGIN_EXISTING_EMAIL="<email>" -DCI=true

# 6. Run only profile-configuration system tests
mvn test -Dtest="TestProfileSystem" -DPROFILE_LOGIN_EMAIL="<verified-email>" \
         -DPROFILE_LOGIN_PASSWORD="<password>" -DCI=true

# 7. Tear down when done
./deploy-local.sh --down
./deploy-local.ps1 -Down
```

### CI (Jenkins / RETORCH)

```bash
mvn test -Dtest="<TestClass#method>" -DTJOB_NAME="<name>" -DSUT_URL="<url>" \
         -DTEST_TOKEN="<token>" -DFRONTEND_URL="<url>" -DCI=true
```

`-DCI=true` activates headless Chrome for the browser tests.

---

## Page Object Model

All browser interactions go through the `pages/` package. Tests never touch `WebDriver` directly.

### Navigation is typed

Every navigation action returns the next page object, so the compiler catches bad flows:

```
WelcomePage → clickLoginButton()     → LoginPage
WelcomePage → clickAnonymousLogin()  → HomePage
LoginPage   → submitLogin()          → LoginPage
LoginPage   → waitForHome()          → HomePage
LoginPage   → clickRegisterLink()    → RegisterPage
RegisterPage → submitRegister()      → RegisterPage
RegisterPage → waitForHome()         → HomePage
HomePage    → clickAnalyzeImage()    → CapturePage
HomePage    → clickLogout()          → WelcomePage
```

### `BasePage` shared primitives

| Method | Purpose |
|---|---|
| `isPresent(By)` | True when ≥1 element matches the locator |
| `click(By)` | Wait for clickable → click |
| `fill(By, String)` | Wait for visible → clear → sendKeys |
| `inputValue(By)` | Read `value` attribute of an input |
| `byText(text)` | XPath: element whose full text equals `text` |
| `byPartialText(text)` | XPath: element whose text contains `text` |
| `inputByPlaceholder(ph)` | CSS: `input[placeholder='ph']` |

### Test entry points on `BaseLoggedClass`

| Method | Returns | Use |
|---|---|---|
| `onWelcomePage()` | `WelcomePage` | Tests that start at the root screen |
| `loginAsGuest()` | `HomePage` | Tests that need an authenticated session |

### Typical test shape

```java
// Login failure
@Test
void emptyPasswordIsRejected() {
    LoginPage page = onWelcomePage().clickLoginButton()
        .login(existingLoginEmail, "");
    Assertions.assertTrue(page.waitForLoginFailure().hasLoginErrorOrValidation());
}

// Authenticated guest login
@Test
void guestLoginReachesHome() {
    Assertions.assertTrue(loginAsGuest().isWelcomeMessageVisible());
}
```

---

## `BaseApiClass` helpers

### URL builders
- `analysisUrl(path)` — prepends `{sutUrl}/analysis`
- `rootUrl(path)` — prepends `{sutUrl}`

### HTTP (unauthenticated)
- `get(url)` / `getStatus(url)`

### HTTP (authenticated — `Authorization: Bearer {testToken}`)
- `getAuth(url)` / `getStatusAuth(url)` / `deleteStatusAuth(url)` / `patchStatusAuth(url)`

### Image upload
- `uploadImage(url, bytes, location)` — multipart POST, returns body
- `uploadImageStatus(url, bytes, location)` — multipart POST, returns status

### JSON
- `getJsonObject(url)` / `getJsonObjectAuth(url)` / `getJsonArrayAuth(url)`
- `containsByField(array, field, value)`

### Fixtures
- `unique()` — current time in ms as a uniqueness suffix
- `createTestImage()` — 10×10 JPEG in memory
- `createAnalysis(location)` — upload + return `analysis_id`
- `deleteAllUserData()` — `DELETE /analysis/user-data`

---

## Design decisions

### DISABLE_WORKER=true for deterministic cancel tests
The background worker calls Ollama and on error sets `status='cancelled'`. Without disabling it the cancel test is racy. `DISABLE_WORKER=true` keeps every uploaded analysis in `'analyzing'` so `PATCH /cancel` always returns 200.

### TEST_MODE accepts any Bearer token
API tests send the fixed `TEST_TOKEN`; Selenium guest-login tests send real Firebase anonymous tokens. Accepting any token in `TEST_MODE` means both suites work against the same backend without needing a Firebase emulator.

### Page Object navigation returns typed page objects
Every navigation method returns the next screen's page object. This enforces correct flows at compile time and makes test intent readable without comments.

### Test isolation
Each browser test gets a fresh Chrome session (`@BeforeEach` / `@AfterEach`). `TestApiHistory` uses `@TestInstance(PER_CLASS)` and a `@BeforeAll deleteAllUserData()` to guarantee an empty history state.

### Code style
- No comments unless the WHY is non-obvious.
- `@DisplayName` must be a human-readable sentence.
- Each test creates its own data; `@BeforeAll` cleanup only when empty state is required.
- Never instantiate a new logger in a test class — use the inherited `log` field.
