# AGL Super Agent — Base44 Dev Environment

This repo is a **native Android app** (Kotlin + Jetpack Compose) at the root (`app/`), plus a **web port** under `web/` that runs in the Base44 preview.

## Running the web port (preview)

```
docker compose -f docker-compose.base44.yml up -d
```

- **web** (Vite + React + TS) → host port **3000** (the preview entry point). Proxies `/api` to the backend.
- **backend** (Node + Express + TS + ethers v6) → host port 8000. Reads Base Mainnet contracts and proxies Gemini AI.

Both run from bind-mounted source with live reload (Vite HMR + `tsx watch`). Edits appear without rebuilds.

## Secrets

- `GEMINI_API_KEY` — required for AI features. Delivered via `/run/base44/app.env`; placeholder in `.env.base44-defaults` lets the app boot without it (AI calls return a friendly error). The backend uses model `gemini-3.6-flash` (override with `GEMINI_MODEL`).

## Architecture notes

- The backend reads Base Mainnet contracts via ethers with multi-RPC failover (`web/backend/src/ethers.ts`). All reads have graceful fallbacks to default/simulated values when RPC fails or contracts don't expose expected functions — mirroring the Android app's resilience.
- Single-origin wiring: only port 3000 is public; the frontend proxies `/api` to `http://backend:8000` (set via `VITE_API_URL`). No CORS needed in the browser.
- Static data (lessons, quests, leaderboard, profile, mock transactions) lives in `web/backend/src/data/static.ts`, ported from the Android `LearningLessonsData` / `BlockchainService` defaults.

## Android app (root)

The original Android app remains in `app/`. It cannot render in the web preview — open it in Android Studio to run on a device/emulator. A `DashboardScreen` (Monitor tab) was added to the Android app as well.

## Verifying it works

- `curl -sf http://localhost:3000/api/health` → `{"ok":true,...}`
- `curl -sf http://localhost:3000/api/ecosystem` → ecosystem stats + contracts
- Preview at `/` shows the AGL Super Agent home screen with portfolio, ecosystem stats, AI suggestions, and bottom nav (Home, Monitor, Wallet, AI Agent, Quests, Profile).
