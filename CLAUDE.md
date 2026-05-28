# CLAUDE.md — retorch-st-climanuvem

This file gives Claude Code the full context needed to work in this repository without re-deriving it each session.

> **MANDATORY RULE — ALWAYS UPDATE THIS FILE**
> Every time you add, remove, or rename a test class, change a package name, modify the API surface, alter backend configuration, add a dependency, or change deployment behaviour, you **must** update the relevant sections of this file in the same operation. Never leave CLAUDE.md describing state that no longer matches the code. If a section becomes outdated it is worse than no documentation.

---

## Project purpose

End-to-end test suite for **EPI-ClimaNuvem**, a FastAPI + PostgreSQL backend and React Native/Expo web frontend that classifies cloud types in photographs using an Ollama LLM. The suite is orchestrated with the [RETORCH](https://github.com/giis-uniovi/retorch) framework and covers two complementary layers:

- **API tests** — HTTP-level tests that call the REST endpoints directly (no browser, no Ollama required).
- **Browser (E2E) tests** — Selenium WebDriver tests that drive the Expo web frontend through Chrome.

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
3. Start the services and poll `GET /ping` until ready (up to 120 s)

To stop:

```bash
./deploy-local.sh --down        # Linux
./deploy-local.ps1 -Down        # Windows
```

### Architecture

| Service | Container name | Port | Purpose |
|---|---|---|---|
| **db** | climanuvem_test_db | internal | PostgreSQL 15 |
| **backend** | climanuvem_test_backend | **8000** | FastAPI REST API |
| **frontend** | climanuvem_test_frontend | **5173** | Expo web build (React Native for Web) |

Ollama is omitted from the test compose (`DISABLE_WORKER=true`) — no GPU required.

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

12 cloud types are seeded on first startup (Cirros, Cúmulos, Estratos, …).

---

## Test-mode authentication

The SUT uses Firebase ID tokens in production. For E2E tests a **test-mode bypass** is enabled via environment variables in `docker-compose.test.yml`:

| Variable | Value | Effect |
|---|---|---|
| `TEST_MODE` | `true` | Firebase SDK is not initialized; auth checks accept the test token |
| `TEST_TOKEN` | `test-token-climanuvem` | The static Bearer token accepted by the backend |
| `TEST_USER_UID` | `test-user-e2e` | The `uid` injected into every authenticated request |
| `DISABLE_WORKER` | `true` | The background Ollama worker does not start; analyses stay in `analyzing` |
| `FIREBASE_KEY_PATH` | _(empty)_ | Skipped in test mode |

The Java test suite reads `TEST_TOKEN` from `src/test/resources/test.properties` (or the `TEST_TOKEN` system/env property in CI) and injects it as `Authorization: Bearer {token}` on every authenticated request.

**Modified backend files (inside `epi-climanuvem/backend/`):**
- `app/infrastructure/config.py` — declares the four test-mode vars
- `app/infrastructure/firebase_service.py` — skips `initialize_app` when `TEST_MODE=true`
- `app/presentation/dependencies/auth_dependency.py` — short-circuits token validation for `TEST_TOKEN`
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
│   │   ├── Dockerfile
│   │   └── requirements.txt
│   └── frontend/                       ← React Native/Expo web app (port 5173)
│       └── Dockerfile
│
├── src/test/java/epigijon/climanuvem/e2e/functional/
│   ├── common/
│   │   ├── BaseApiClass.java           ← HTTP plumbing, auth token, multipart upload, fixtures
│   │   └── BaseLoggedClass.java        ← Selenium browser lifecycle, loginAsGuest()
│   ├── utils/
│   │   ├── Waiter.java                 ← Explicit wait helpers (page- and element-level)
│   │   └── Navigation.java             ← Browser action helpers (click, fill, navigate)
│   └── tests/
│       ├── TestApiPing.java            ← GET /ping, GET /
│       ├── TestApiAuth.java            ← Auth enforcement (403/401 scenarios)
│       ├── TestApiAnalysis.java        ← POST /analysis/upload
│       ├── TestApiHistory.java         ← GET /analysis/history
│       ├── TestApiDelete.java          ← DELETE /analysis/{id}, /user-data
│       ├── TestApiCancel.java          ← PATCH /analysis/{id}/cancel
│       ├── TestWelcomeScreen.java      ← Welcome page elements and navigation
│       ├── TestLoginForm.java          ← Login form structure and field interactions
│       ├── TestHomeScreen.java         ← Home page after anonymous login
│       └── TestCaptureScreen.java      ← Capture page after anonymous login
│
├── src/test/resources/
│   ├── test.properties                 ← LOCALHOST_URL, FRONTEND_URL, TEST_TOKEN
│   └── log4j2.xml
│
├── .retorch/
│   └── configurations/
│       ├── ClimaNuvemSystemResources.json  ← RETORCH resource model
│       └── retorchCI.properties
│
├── docker-compose.test.yml             ← Test environment (db + backend + frontend, no Ollama)
├── deploy-local.sh                     ← Linux/macOS: clone SUT, start all services
├── deploy-local.ps1                    ← Windows PowerShell: clone SUT, start all services
└── pom.xml                             ← Maven build (httpmime for multipart)
```

---

## Key dependencies

| Dependency | Purpose |
|---|---|
| JUnit 5 (Jupiter) | Test runner |
| Apache HttpClient 4.5.14 | HTTP client for API requests |
| Apache HttpMime 4.5.14 | Multipart entity builder (image upload) |
| Gson 2.14.0 | JSON parsing and payload building |
| RETORCH annotations | `@AccessMode` resource declarations |
| Log4j2 + SLF4J | Structured logging |
| `javax.imageio.ImageIO` (JDK) | Creates minimal 10×10 JPEG test images in memory |

---

## RETORCH resource model

| Resource ID | Represents | Typical access |
|---|---|---|
| `backend` | FastAPI service | `READONLY, concurrency=10, sharing=true` |
| `analysis` | Analysis records in the database | `READWRITE, concurrency=1, sharing=false` (write tests) / `READONLY` (read-only tests) |

---

## Configuration

### `src/test/resources/test.properties`
```properties
LOCALHOST_URL=http://localhost:8000
TEST_TOKEN=test-token-climanuvem
```
Both can be overridden at runtime via system properties (`-DSUT_URL=…`, `-DTEST_TOKEN=…`) or environment variables (`SUT_URL`, `TEST_TOKEN`).

### `src/test/resources/log4j2.xml`
Logs go to `target/testlogs/log${sys:TJOB_NAME:-testinglocal}-test.log`. Parallel TJobs write to separate files.

### `pom.xml` — per-TJob build directory
```xml
<directory>${project.basedir}/target/${TJOB_NAME}</directory>
```
Falls back to `target/local` when `TJOB_NAME` is not set (local-execution profile).

---

## Running tests

### Local — full suite

```bash
# 1. Start the SUT (clones from GitLab on first run)
./deploy-local.sh        # Linux
./deploy-local.ps1       # Windows

# 2. Run all tests
mvn test

# 3. Tear down when done
./deploy-local.sh --down
./deploy-local.ps1 -Down
```

### Local — single class

```bash
mvn test -Dtest=TestApiAnalysis
mvn test -Dtest=TestApiHistory
```

### CI (Jenkins / RETORCH)

```bash
mvn test -Dtest="<TestClass#method>" -DTJOB_NAME="<TJOB_NAME>" -DSUT_URL="<SUT_URL>" -DTEST_TOKEN="<TOKEN>"
```

---

## `BaseApiClass` helpers (`epigijon.climanuvem.e2e.functional.common`)

### URL builders
- `analysisUrl(path)` — prepends `{sutUrl}/analysis`
- `rootUrl(path)` — prepends `{sutUrl}`

### HTTP methods (unauthenticated)
- `get(url)` — GET, returns response body
- `getStatus(url)` — GET, returns HTTP status code

### HTTP methods (authenticated — `Authorization: Bearer {testToken}`)
- `getAuth(url)` — GET, returns body
- `getStatusAuth(url)` — GET, returns status
- `deleteStatusAuth(url)` — DELETE, returns status
- `patchStatusAuth(url)` — PATCH, returns status

### Image upload
- `uploadImage(url, imageBytes, location)` — multipart POST, returns body
- `uploadImageStatus(url, imageBytes, location)` — multipart POST, returns status

### JSON helpers
- `getJsonObject(url)` — GET + parse as JsonObject (no auth)
- `getJsonObjectAuth(url)` — GET + parse as JsonObject (with auth)
- `getJsonArrayAuth(url)` — GET + parse as JsonArray (with auth)
- `containsByField(array, fieldName, value)` — true if any element matches

### Test-data helpers
- `unique()` — current time in ms, used as a uniqueness suffix
- `createTestImage()` — 10×10 JPEG in memory (no disk I/O)
- `createAnalysis(location)` — uploads an image and returns the assigned `analysis_id`
- `deleteAllUserData()` — calls `DELETE /analysis/user-data` for the test user

### Typical test shape (read-only)
```java
@AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
@Test
@DisplayName("GET /ping returns HTTP 200 with ping:pong payload")
void testPingEndpoint() throws IOException {
    Assertions.assertEquals(200, getStatus(rootUrl("/ping")));
    JsonObject body = getJsonObject(rootUrl("/ping"));
    Assertions.assertEquals("pong", body.get("ping").getAsString());
}
```

### Typical test shape (write + verify)
```java
@AccessMode(resID = "analysis", concurrency = 1, sharing = false, accessMode = "READWRITE")
@Test
@DisplayName("DELETE /analysis/{id} returns HTTP 200 and the analysis no longer appears in history")
void testDeleteSingleAnalysisReturns200() throws IOException {
    int analysisId = createAnalysis("Delete Me City");
    int deleteStatus = deleteStatusAuth(analysisUrl("/" + analysisId));
    Assertions.assertEquals(200, deleteStatus);
    JsonArray history = getJsonArrayAuth(analysisUrl("/history"));
    Assertions.assertFalse(containsByField(history, "id", String.valueOf(analysisId)));
}
```

---

## Design decisions and known issues

### DISABLE_WORKER=true for deterministic cancel tests
The background worker calls Ollama and, on failure, sets `status = 'cancelled'`. Without disabling it, the `PATCH /cancel` test would be racy: if the worker finishes before the test sends the PATCH, the analysis is already cancelled and the test sees a 400 instead of 200. `DISABLE_WORKER=true` in the test compose keeps every uploaded analysis in `'analyzing'` state.

### Multipart upload using httpmime
Apache HttpClient 4.5 separates the multipart entity builder into a separate artifact (`httpmime`). It is added to `pom.xml` alongside `httpclient`. The `createTestImage()` helper uses `javax.imageio.ImageIO` from the standard JDK to build a minimal 10×10 JPEG in memory — no fixture files needed.

### Test isolation for history tests
`TestApiHistory` is annotated `@TestInstance(PER_CLASS)` so its non-static `@BeforeAll cleanUpUserData()` can call `deleteAllUserData()`. This clears the test user's history before the history tests run, regardless of what previous test classes created.

### Firebase not initialized in TEST_MODE
`firebase_service.py` guards `initialize_app()` behind `if not TEST_MODE`. The `firebase_admin.messaging` module can be imported without an active app — errors only arise at `messaging.send()`, which never happens in tests because no FCM token is passed.

### Code style conventions (for this project)
- **No comments** unless the WHY is non-obvious (constraint, workaround, invariant).
- **`@DisplayName`** must be a human-readable sentence, not the method name.
- **Test data isolation**: each test creates its own data; `@BeforeAll` cleanup when empty state is required.
- **`BaseApiClass` helpers**: use `createAnalysis()` for setup — do not duplicate multipart payload construction inline.
- **Assertions**: use `Assertions.assertAll` when checking multiple fields of the same object.
- **Logger**: always use the inherited `log` field; never instantiate a new logger in a test class.
