#!/usr/bin/env bash
# deploy-local.sh — Deploy or tear down the ClimaNuvem SUT locally for E2E testing
#
# Usage:
#   ./deploy-local.sh              — clone SUT (if needed) and start on default ports
#   ./deploy-local.sh --port 9000  — use a custom backend port (frontend stays at 5173)
#   ./deploy-local.sh --with-ollama — start worker + Ollama for real image-analysis tests
#   ./deploy-local.sh --down       — tear down the running deployment

set -euo pipefail

SUT_REPO="https://gitlab.com/HP-SCDS/Observatorio/2025-2026/climanuvem/epi-climanuvem.git"
SUT_DIR="../epi-climanuvem"
COMPOSE_FILE="docker-compose.test.yml"
OLLAMA_COMPOSE_FILE="docker-compose.ollama-test.yml"
PROJECT_NAME="climanuvem-test"
MAX_WAIT_SECS=180
POLL_INTERVAL=5
PORT=8000
DOWN=false
WITH_OLLAMA=false
OLLAMA_MODEL="${OLLAMA_MODEL:-gemma4:e4b}"
POSTGRES_VOLUME="${PROJECT_NAME}_postgres_test_data"
export OLLAMA_MODEL

step() { echo "[>] $*"; }
ok()   { echo "[+] $*"; }
fail() { echo "[!] $*" >&2; exit 1; }

# ── Parse arguments ────────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
    case "$1" in
        --down)   DOWN=true; shift ;;
        --with-ollama) WITH_OLLAMA=true; shift ;;
        --port)   PORT="$2"; shift 2 ;;
        *) fail "Unknown argument: $1. Valid options: --down, --with-ollama, --port <number>" ;;
    esac
done

COMPOSE_ARGS=(-f "$COMPOSE_FILE")
if $WITH_OLLAMA; then
    COMPOSE_ARGS+=(-f "$OLLAMA_COMPOSE_FILE")
fi
export BACKEND_PORT="$PORT"

# ── Prerequisites ──────────────────────────────────────────────────────────────
step "Checking prerequisites..."
command -v docker >/dev/null 2>&1 || fail "docker is not installed."
command -v git    >/dev/null 2>&1 || fail "git is not installed."
docker info >/dev/null 2>&1       || fail "Docker daemon is not running."
ok "Prerequisites satisfied."

# ── Teardown mode ──────────────────────────────────────────────────────────────
if $DOWN; then
    step "Tearing down project '$PROJECT_NAME'..."
    docker compose "${COMPOSE_ARGS[@]}" -p "$PROJECT_NAME" down
    step "Removing PostgreSQL test volume '$POSTGRES_VOLUME'..."
    docker volume rm "$POSTGRES_VOLUME" >/dev/null 2>&1 || true
    ok "Teardown complete."
    exit 0
fi

# ── Clone SUT if not already present ─────────────────────────────────────────
step "Checking for SUT in '$SUT_DIR'..."
if [[ ! -d "$SUT_DIR" ]]; then
    step "Cloning $SUT_REPO..."
    git clone "$SUT_REPO" "$SUT_DIR"
    ok "Cloned '$SUT_DIR'."
else
    ok "'$SUT_DIR' already present, skipping clone."
fi

# ── Build images ──────────────────────────────────────────────────────────────
step "Building Docker images..."
docker compose "${COMPOSE_ARGS[@]}" -p "$PROJECT_NAME" build
ok "Images built."

# ── Start containers ──────────────────────────────────────────────────────────
step "Starting containers (project: '$PROJECT_NAME')..."
docker compose "${COMPOSE_ARGS[@]}" -p "$PROJECT_NAME" up -d
ok "Containers started."

if $WITH_OLLAMA; then
    step "Ensuring Ollama model '$OLLAMA_MODEL' is available..."
    docker compose "${COMPOSE_ARGS[@]}" -p "$PROJECT_NAME" exec -T ollama ollama pull "$OLLAMA_MODEL"
    ok "Ollama model '$OLLAMA_MODEL' is ready."
fi

# ── Wait for backend ──────────────────────────────────────────────────────────
BACKEND_URL="http://localhost:$PORT"
elapsed=0
ready=false

step "Waiting for backend at $BACKEND_URL/ping (up to ${MAX_WAIT_SECS}s)..."
while [[ $elapsed -lt $MAX_WAIT_SECS ]]; do
    if curl --silent --max-time 5 "$BACKEND_URL/ping" 2>/dev/null | grep -q '"pong"'; then
        ready=true
        break
    fi
    sleep $POLL_INTERVAL
    elapsed=$((elapsed + POLL_INTERVAL))
    echo "  ... $elapsed / ${MAX_WAIT_SECS}s"
done

if ! $ready; then
    echo "[!] Backend did not become healthy within ${MAX_WAIT_SECS}s." >&2
    docker compose "${COMPOSE_ARGS[@]}" -p "$PROJECT_NAME" logs --tail 50
    docker compose "${COMPOSE_ARGS[@]}" -p "$PROJECT_NAME" down
    docker volume rm "$POSTGRES_VOLUME" >/dev/null 2>&1 || true
    exit 1
fi
ok "Backend is ready at $BACKEND_URL"

# ── Wait for frontend ─────────────────────────────────────────────────────────
FRONTEND_URL="http://localhost:5173"
elapsed=0
ready=false

step "Waiting for frontend at $FRONTEND_URL (up to ${MAX_WAIT_SECS}s)..."
while [[ $elapsed -lt $MAX_WAIT_SECS ]]; do
    if curl --silent --max-time 5 "$FRONTEND_URL" 2>/dev/null | grep -qi "climanuvem\|html"; then
        ready=true
        break
    fi
    sleep $POLL_INTERVAL
    elapsed=$((elapsed + POLL_INTERVAL))
    echo "  ... $elapsed / ${MAX_WAIT_SECS}s"
done

if $ready; then
    ok "Frontend is ready at $FRONTEND_URL"
    ok "ClimaNuvem test environment is fully up:"
    ok "  Backend  → $BACKEND_URL"
    ok "  Frontend → $FRONTEND_URL"
else
    echo "[!] Frontend did not become healthy within ${MAX_WAIT_SECS}s." >&2
    docker compose "${COMPOSE_ARGS[@]}" -p "$PROJECT_NAME" logs --tail 50
    docker compose "${COMPOSE_ARGS[@]}" -p "$PROJECT_NAME" down
    docker volume rm "$POSTGRES_VOLUME" >/dev/null 2>&1 || true
    exit 1
fi
