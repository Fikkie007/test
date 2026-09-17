# Riverside Permit Register Backend

Spring Boot REST API for the Riverside Council community hall permit register. This slice implements permit search, permit detail and renewal quote/confirmation workflows for RC-1 to RC-3.

## Requirements

- Java 26
- Maven Wrapper included in the repository
- Node.js and npm only if running the separate frontend

The current implementation uses in-memory repositories and seeded data. No database is required for local development.

## Install

From this directory, verify Java and compile the project:

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-26.0.2'
.\mvnw.cmd test
```

## Environment

The application imports an optional `.env` file from the backend directory. To create one from the template:

```bash
cp .env.sample .env
```

On PowerShell:

```powershell
Copy-Item .env.sample .env
```

| Variable | Default | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `default` | Active Spring profile |
| `SPRING_BOOT_PORT` | `8080` | HTTP server port |

`.env` is ignored by Git. Do not add secrets or production data to the repository.

## Run

Start the API with:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The API is available at `http://localhost:8080` by default.

## API Workflows

- `GET /api/reference-data` returns hall, purpose and status options.
- `GET /api/permits` searches and pages the register.
- `GET /api/permits/{permitNumber}` returns permit details and history.
- `POST /api/permits/{permitNumber}/renewal-quote` calculates a renewal quote.
- `POST /api/permits/{permitNumber}/renewals` confirms a quoted renewal.

Renewal fees, eligibility, status transitions and stale-quote checks are enforced by the server. Payment processing is outside this service.

## Verify

Run unit and API tests:

```bash
./mvnw test
```

Run the full Maven verification lifecycle:

```bash
./mvnw verify
```

The test suite covers successful renewal, fee caps, Council Use zero-fee renewals, expired-permit rejection, quote conflicts and API reference data.
