<#
.SYNOPSIS
    Deploys or tears down the ClimaNuvem SUT locally for E2E testing.
.PARAMETER Down
    Tear down the running deployment instead of starting it.
.PARAMETER Port
    Host port for the backend (default: 8000). Frontend is always on 5173.
.PARAMETER WithOllama
    Start the real analysis worker and an Ollama container for image-analysis system tests.
.EXAMPLE
    .\deploy-local.ps1
    .\deploy-local.ps1 -Port 9000
    .\deploy-local.ps1 -WithOllama
    .\deploy-local.ps1 -Down
#>
param(
    [switch]$Down,
    [switch]$WithOllama,
    [int]$Port = 8000
)

$ErrorActionPreference = "Stop"

$SUT_REPO      = "https://gitlab.com/HP-SCDS/Observatorio/2025-2026/climanuvem/epi-climanuvem.git"
$SUT_DIR       = "..\epi-climanuvem"
$COMPOSE_FILE  = "docker-compose.test.yml"
$OLLAMA_COMPOSE_FILE = "docker-compose.ollama-test.yml"
$PROJECT_NAME  = "climanuvem-test"
$MAX_WAIT_SECS = 180
$POLL_INTERVAL = 5
$OLLAMA_MODEL  = "gemma4:e4b"

$composeFiles = @("-f", $COMPOSE_FILE)
if ($WithOllama -or $Down) {
    $composeFiles += @("-f", $OLLAMA_COMPOSE_FILE)
}

function Write-Step([string]$msg) { Write-Host "[>] $msg" -ForegroundColor Cyan }
function Write-OK([string]$msg)   { Write-Host "[+] $msg" -ForegroundColor Green }
function Write-Fail([string]$msg) { Write-Host "[!] $msg" -ForegroundColor Red; exit 1 }

# ── Prerequisites ──────────────────────────────────────────────────────────────
Write-Step "Checking prerequisites..."

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Fail "docker is not installed or not in PATH."
}
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Fail "git is not installed or not in PATH."
}
$ErrorActionPreference = "Continue"
docker info *>$null
$dockerOk = ($LASTEXITCODE -eq 0)
$ErrorActionPreference = "Stop"
if (-not $dockerOk) {
    Write-Fail "Docker daemon is not running. Please start Docker Desktop."
}
Write-OK "Prerequisites satisfied."

# ── Teardown mode ──────────────────────────────────────────────────────────────
if ($Down) {
    Write-Step "Tearing down project '$PROJECT_NAME'..."
    docker compose @composeFiles -p $PROJECT_NAME down --volumes
    if ($LASTEXITCODE -ne 0) { Write-Fail "docker compose down failed." }
    Write-OK "Teardown complete."
    exit 0
}

# ── Clone SUT if not already present ─────────────────────────────────────────
Write-Step "Checking for SUT in '$SUT_DIR'..."
if (-not (Test-Path $SUT_DIR)) {
    Write-Step "Cloning $SUT_REPO..."
    git clone $SUT_REPO $SUT_DIR
    if ($LASTEXITCODE -ne 0) { Write-Fail "Failed to clone '$SUT_REPO'." }
    Write-OK "Cloned '$SUT_DIR'."
} else {
    Write-OK "'$SUT_DIR' already present, skipping clone."
}

# ── Build images ──────────────────────────────────────────────────────────────
Write-Step "Building Docker images..."
docker compose @composeFiles -p $PROJECT_NAME build
if ($LASTEXITCODE -ne 0) { Write-Fail "docker compose build failed." }
Write-OK "Images built."

# ── Start containers ──────────────────────────────────────────────────────────
Write-Step "Starting containers (project: '$PROJECT_NAME')..."
docker compose @composeFiles -p $PROJECT_NAME up -d
if ($LASTEXITCODE -ne 0) { Write-Fail "docker compose up failed." }
Write-OK "Containers started."

if ($WithOllama) {
    Write-Step "Ensuring Ollama model '$OLLAMA_MODEL' is available..."
    docker compose @composeFiles -p $PROJECT_NAME exec -T ollama ollama pull $OLLAMA_MODEL
    if ($LASTEXITCODE -ne 0) { Write-Fail "ollama pull '$OLLAMA_MODEL' failed." }
    Write-OK "Ollama model '$OLLAMA_MODEL' is ready."
}

# ── Wait for backend ──────────────────────────────────────────────────────────
$backendUrl = "http://localhost:$Port"
$elapsed    = 0
$ready      = $false

Write-Step "Waiting for backend at $backendUrl/ping (up to ${MAX_WAIT_SECS}s)..."
while ($elapsed -lt $MAX_WAIT_SECS) {
    try {
        $resp = Invoke-WebRequest -Uri "$backendUrl/ping" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        if ($resp.Content -match '"pong"') { $ready = $true; break }
    } catch {}
    Start-Sleep -Seconds $POLL_INTERVAL
    $elapsed += $POLL_INTERVAL
    Write-Host "  ... $elapsed / ${MAX_WAIT_SECS}s"
}

if (-not $ready) {
    Write-Host "[!] Backend did not become healthy." -ForegroundColor Red
    docker compose @composeFiles -p $PROJECT_NAME logs --tail 50
    docker compose @composeFiles -p $PROJECT_NAME down --volumes
    exit 1
}
Write-OK "Backend is ready at $backendUrl"

# ── Wait for frontend ─────────────────────────────────────────────────────────
$frontendUrl = "http://localhost:5173"
$elapsed     = 0
$ready       = $false

Write-Step "Waiting for frontend at $frontendUrl (up to ${MAX_WAIT_SECS}s)..."
while ($elapsed -lt $MAX_WAIT_SECS) {
    try {
        $resp = Invoke-WebRequest -Uri $frontendUrl -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        if ($resp.Content -match "(?i)climanuvem|html") { $ready = $true; break }
    } catch {}
    Start-Sleep -Seconds $POLL_INTERVAL
    $elapsed += $POLL_INTERVAL
    Write-Host "  ... $elapsed / ${MAX_WAIT_SECS}s"
}

if ($ready) {
    Write-OK "Frontend is ready at $frontendUrl"
    Write-OK "ClimaNuvem test environment is fully up:"
    Write-OK "  Backend  → $backendUrl"
    Write-OK "  Frontend → $frontendUrl"
} else {
    Write-Host "[!] Frontend did not become healthy." -ForegroundColor Red
    docker compose @composeFiles -p $PROJECT_NAME logs --tail 50
    docker compose @composeFiles -p $PROJECT_NAME down --volumes
    exit 1
}
