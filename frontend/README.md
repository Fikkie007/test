# Riverside Permit Register Frontend

React 19 and Vite staff portal for searching permits, viewing permit history, and confirming renewals.

## Requirements

- Node.js 24 or later
- npm
- The backend running on `http://localhost:8080`

## Install

From this directory:

```bash
npm install
```

The repository includes a local `.env`. To create one from the template instead:

```bash
cp .env.example .env
```

On PowerShell:

```powershell
Copy-Item .env.example .env
```

## Environment

| Variable | Default | Purpose |
|---|---|---|
| `VITE_API_BASE_URL` | `/api` | API base path used by the browser |
| `VITE_API_PROXY_TARGET` | `http://localhost:8080` | Backend target for the Vite development proxy |

Do not put secrets in `VITE_*` variables. Vite exposes them to browser code.

## Run

Start the backend first, then run from this directory:

```bash
npm run dev
```

Open the URL printed by Vite, normally `http://localhost:5173`.

The portal supports:

- searching by permit number, holder, hall, purpose, status, and date range;
- opening a permit and viewing renewal history;
- requesting a server-calculated renewal quote;
- confirming a renewal after reviewing the quote.

The browser does not calculate authoritative fees or renewal eligibility.

## Verify

```bash
npm run lint
npm run build
```

To preview the production build:

```bash
npm run preview
```
