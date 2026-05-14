# TrendRadar

TrendRadar is a product opportunity intelligence platform for online sellers. It turns marketplace signals into normalized, explainable product opportunity snapshots with scoring, evidence, risk context, provider run history, and system visibility.

The current implementation is a modular monolith backend, a Next.js operational dashboard, PostgreSQL persistence, Liquibase migrations, an optional eBay Browse provider, a deterministic scoring engine, and a Docker Compose local stack.

## Features

Currently implemented:

- Premium Next.js dashboard for opportunity research.
- Normalized opportunity API consumed by the frontend.
- Health endpoint for backend validation.
- Manual opportunity refresh endpoint.
- Provider run history API with pagination.
- System status API with backend, database, storage, provider, and latest-run status.
- Dashboard sections for system status, last refresh, and recent provider runs.
- Dedicated Provider Runs and System Status pages.
- Provider layer with optional eBay Browse API integration.
- Mock marketplace provider fallback when eBay is disabled or missing credentials.
- Normalization from provider signals into TrendRadar domain concepts.
- Opportunity scoring engine with component scores:
  - marketplace proof
  - price viability
  - freshness
  - seller quality
  - shipping risk
  - competition risk
- Human-readable deterministic explanation text based on normalized evidence and risks.
- PostgreSQL persistence for provider runs, raw source records, normalized signals, scoring runs, and opportunity snapshots.
- Liquibase schema migrations with audit timestamps and query indexes.
- Lightweight backend logging around refresh, provider execution, scoring, and persistence.
- Docker Compose stack for PostgreSQL, Spring Boot backend, and Next.js frontend.

Not implemented yet:

- OpenAI-powered explanation layer.
- Google Trends or other demand provider.
- Scheduled ingestion.
- Alerts, watchlists, or notifications.
- Authentication or user accounts.
- Kubernetes, Redis, message queues, or microservices.

## Architecture Overview

TrendRadar has three local runtime components:

- **Frontend:** Next.js app on port `3000`. It renders opportunity cards, score breakdowns, system status, recent provider runs, Provider Runs, and System Status views.
- **Backend:** Spring Boot API on port `8080`. It owns provider orchestration, normalization, scoring, explanations, persistence, provider run history, and system status responses.
- **Database:** PostgreSQL on port `5432`. Liquibase creates and updates the schema automatically when the backend starts.

The frontend uses Next.js API proxy routes for browser-safe and container-safe calls. The proxy uses `TREND_RADAR_API_BASE_URL`, which is `http://trend-radar-backend:8080` inside Docker and defaults to `http://localhost:8080` for non-Docker local development.

## Current System Design

The backend is organized as a modular monolith:

- **Provider layer:** Fetches raw marketplace signals. `EbayMarketSignalProvider` is first priority when enabled and configured. `MockMarketSignalProvider` keeps the app runnable without external credentials.
- **Normalization layer:** Converts provider-shaped marketplace signals into normalized `OpportunitySnapshot` domain responses.
- **Scoring layer:** Calculates explainable score components and final score labels: `High`, `Promising`, `Watch`, or `Weak`.
- **Explanation layer:** Generates deterministic text explanations from marketplace evidence and risks.
- **Persistence layer:** Stores provider execution metadata, raw source JSON, normalized signals, scoring metadata, final opportunity snapshots, and audit timestamps.
- **Observability layer:** Exposes provider run history and aggregate system status from persisted runs, scoring records, source records, snapshots, provider availability, and database connectivity.
- **API layer:** Exposes normalized TrendRadar APIs only. eBay-shaped models are not exposed to the frontend.

`GET /api/opportunities` returns the latest persisted snapshots for the requested niche and region. If none exist, it performs a refresh, persists the results, and returns normalized opportunities.

`POST /api/opportunities/refresh` always runs a provider fetch, normalization, scoring, persistence, and response cycle.

## Tech Stack

- **Frontend:** Next.js 15, React 19, TypeScript.
- **Backend:** Java 17, Spring Boot 3.5, Spring Web, Spring Data JPA.
- **Database:** PostgreSQL 16 in Docker, PostgreSQL 15+ recommended for local installs.
- **Migrations:** Liquibase.
- **External provider:** eBay Browse API, optional and disabled by default.
- **Local orchestration:** Docker Compose.
- **Tests:** JUnit/Spring Boot tests, H2 test database for backend tests.

## Project Structure

```text
trend-radar/
  backend/
    Dockerfile
    pom.xml
    src/main/java/com/trendradar/
      api/                 REST controllers
      application/         use-case orchestration and query services
      domain/              normalized TrendRadar domain records
      explanation/         deterministic explanation generation
      health/              health endpoint
      infrastructure/
        ebay/              eBay Browse client/provider/config
        persistence/       JPA entities, repositories, persistence service
      normalization/       provider signal to opportunity snapshot mapping
      provider/            provider interfaces and mock provider
      scoring/             scoring engine and score model
    src/main/resources/
      application.properties
      application-docker.yml
      db/changelog/        Liquibase migrations
  frontend/
    Dockerfile
    app/
      api/opportunities/   Next.js proxy to backend opportunities API
      api/provider-runs/   Next.js proxy to backend provider run APIs
      api/system/status/   Next.js proxy to backend system status API
      provider-runs/       provider run history page
      system-status/       system status page
      page.tsx             main dashboard UI
      globals.css          dashboard and operations styling
  docker-compose.yml
  docs/
    PROJECT_BRIEF.md
```

## Running Locally Non-Docker

Prerequisites:

- Java 17
- Maven 3.9+
- Node.js 20+
- npm 10+
- PostgreSQL 15+

Create the database:

```sql
CREATE DATABASE trend_radar;
CREATE USER trend_radar WITH PASSWORD 'trend_radar';
GRANT ALL PRIVILEGES ON DATABASE trend_radar TO trend_radar;

\c trend_radar
GRANT ALL ON SCHEMA public TO trend_radar;
```

Create `backend/.env` from `backend/.env.example`:

```properties
TREND_RADAR_DB_URL=jdbc:postgresql://localhost:5432/trend_radar
TREND_RADAR_DB_USERNAME=trend_radar
TREND_RADAR_DB_PASSWORD=trend_radar

TREND_RADAR_EBAY_ENABLED=false
TREND_RADAR_EBAY_TOKEN_URL=https://api.sandbox.ebay.com/identity/v1/oauth2/token
TREND_RADAR_EBAY_SEARCH_URL=https://api.ebay.com/buy/browse/v1/item_summary/search
TREND_RADAR_EBAY_USERNAME=your-ebay-client-id
TREND_RADAR_EBAY_PASSWORD=your-ebay-client-secret
TREND_RADAR_EBAY_SCOPE=https://api.ebay.com/oauth/api_scope
TREND_RADAR_EBAY_LIMIT=10
TREND_RADAR_EBAY_MARKETPLACE_ID=EBAY_CA
```

Start the backend:

```powershell
cd backend
& "C:\Users\vivek\Softwares\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin\mvn.cmd" spring-boot:run
```

Or, if Maven is on `PATH`:

```powershell
cd backend
mvn spring-boot:run
```

Start the frontend:

```powershell
cd frontend
npm install
npm run dev
```

Local URLs:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

## Running With Docker

Prerequisites:

- Docker Desktop
- Docker Compose v2

Start the full stack from the repository root:

```powershell
docker compose up --build
```

Docker services:

- `trend-radar-db`
- `trend-radar-backend`
- `trend-radar-frontend`

Docker URLs:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

The backend runs with `SPRING_PROFILES_ACTIVE=docker`, connects to PostgreSQL at `trend-radar-db:5432`, and runs Liquibase automatically.

Stop containers:

```powershell
docker compose down
```

Stop containers and remove the database volume:

```powershell
docker compose down -v
```

Rebuild containers:

```powershell
docker compose build --no-cache
docker compose up
```

Inspect logs:

```powershell
docker compose logs -f trend-radar-backend
docker compose logs -f trend-radar-frontend
docker compose logs -f trend-radar-db
```

## Environment Variables

Backend variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `TREND_RADAR_DB_URL` | `jdbc:postgresql://localhost:5432/trend_radar` | JDBC URL for local backend. Docker overrides this to `trend-radar-db`. |
| `TREND_RADAR_DB_USERNAME` | `trend_radar` | PostgreSQL username. |
| `TREND_RADAR_DB_PASSWORD` | `trend_radar` | PostgreSQL password. |
| `TREND_RADAR_EBAY_ENABLED` | `false` | Enables eBay provider only when credentials are also present. |
| `TREND_RADAR_EBAY_TOKEN_URL` | `https://api.sandbox.ebay.com/identity/v1/oauth2/token` | eBay OAuth token URL. |
| `TREND_RADAR_EBAY_SEARCH_URL` | `https://api.ebay.com/buy/browse/v1/item_summary/search` | eBay Browse search URL. |
| `TREND_RADAR_EBAY_USERNAME` | empty | eBay client ID for Basic Auth. |
| `TREND_RADAR_EBAY_PASSWORD` | empty | eBay client secret for Basic Auth. |
| `TREND_RADAR_EBAY_SCOPE` | `https://api.ebay.com/oauth/api_scope` | eBay OAuth scope. |
| `TREND_RADAR_EBAY_LIMIT` | `10` | eBay search result limit. |
| `TREND_RADAR_EBAY_MARKETPLACE_ID` | `EBAY_CA` | eBay marketplace header value. |

Docker Compose variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `TREND_RADAR_DB_NAME` | `trend_radar` | PostgreSQL database created by the container. |
| `TREND_RADAR_DB_USERNAME` | `trend_radar` | PostgreSQL user created by the container and used by backend. |
| `TREND_RADAR_DB_PASSWORD` | `trend_radar` | PostgreSQL password created by the container and used by backend. |
| `TREND_RADAR_EBAY_ENABLED` | `false` | Passed to backend container. |
| `TREND_RADAR_EBAY_USERNAME` | empty | Passed to backend container. |
| `TREND_RADAR_EBAY_PASSWORD` | empty | Passed to backend container. |
| `TREND_RADAR_EBAY_MARKETPLACE_ID` | `EBAY_CA` | Passed to backend container. |

Frontend variable:

| Variable | Default | Purpose |
| --- | --- | --- |
| `TREND_RADAR_API_BASE_URL` | `http://localhost:8080` | Backend base URL used by the Next.js API proxy. Docker sets it to `http://trend-radar-backend:8080`. |

Secrets should stay in local environment variables or ignored `.env` files. Do not commit real API credentials.

## Available APIs

### Health

```http
GET /api/health
```

```powershell
Invoke-RestMethod "http://localhost:8080/api/health"
```

Expected:

```json
{
  "status": "OK"
}
```

### Get Opportunities

```http
GET /api/opportunities?niche=anime_collectibles&region=CA
```

```powershell
Invoke-RestMethod "http://localhost:8080/api/opportunities?niche=anime_collectibles&region=CA"
```

Behavior:

- Returns latest persisted opportunity snapshots for the requested niche and region.
- If no snapshots exist, refreshes via available provider, persists results, and returns normalized snapshots.
- Defaults are `niche=anime_collectibles` and `region=CA`.

### Refresh Opportunities

```http
POST /api/opportunities/refresh?niche=anime_collectibles&region=CA
```

```powershell
Invoke-RestMethod -Method Post "http://localhost:8080/api/opportunities/refresh?niche=anime_collectibles&region=CA"
```

Behavior:

- Starts a provider run.
- Fetches eBay data when eBay is enabled and configured.
- Falls back to mock provider when eBay is disabled, missing credentials, unavailable, or returns no usable products.
- Persists raw source records, normalized signals, scoring run metadata, and opportunity snapshots.
- Returns normalized opportunities.

### Provider Runs

```http
GET /api/provider-runs?page=0&size=20
GET /api/provider-runs/{id}
```

```powershell
Invoke-RestMethod "http://localhost:8080/api/provider-runs?page=0&size=20"
Invoke-RestMethod "http://localhost:8080/api/provider-runs/1"
```

The list endpoint returns newest runs first and includes pagination metadata.

### System Status

```http
GET /api/system/status
```

```powershell
Invoke-RestMethod "http://localhost:8080/api/system/status"
```

The response includes backend status, database connectivity, latest provider run, latest scoring run, stored opportunity/source record counts, active providers, and generation timestamp.

### Frontend Proxy Routes

These routes are served by Next.js and forwarded to the backend:

```http
GET /api/opportunities?niche=anime_collectibles&region=CA
GET /api/provider-runs?page=0&size=20
GET /api/provider-runs/{id}
GET /api/system/status
```

## Response Shapes

### Opportunity

The frontend consumes normalized TrendRadar data, not eBay-shaped data.

```json
[
  {
    "productConcept": {
      "id": "mock-rimuru-figure",
      "name": "Rimuru figure collectible anime authentic",
      "category": "Anime figure"
    },
    "niche": {
      "code": "anime_collectibles",
      "displayName": "Anime collectibles"
    },
    "region": {
      "code": "CA",
      "displayName": "Canada"
    },
    "score": 79,
    "scoreLabel": "Promising",
    "marketplaceProofScore": 53,
    "priceViabilityScore": 80,
    "freshnessScore": 100,
    "sellerQualityScore": 95,
    "shippingRiskScore": 80,
    "competitionRiskScore": 85,
    "finalScore": 79,
    "marketplaceEvidence": {
      "estimatedSoldCount": 1320,
      "activeListings": 3,
      "medianPrice": 42.5,
      "minPrice": 18.75,
      "maxPrice": 42.5,
      "demandSignal": "Mock marketplace signal..."
    },
    "sourceEvidence": [
      {
        "sourceType": "marketplace_mock",
        "title": "Rimuru figure collectible anime authentic",
        "confidence": 0.95,
        "observedAt": "2026-05-14T00:00:00Z"
      }
    ],
    "risks": [
      {
        "type": "licensed_ip",
        "severity": "MEDIUM",
        "description": "Character merchandise may require careful sourcing"
      }
    ],
    "explanation": "Human-readable scoring rationale.",
    "generatedAt": "2026-05-14T00:00:00Z"
  }
]
```

### Provider Run Page

```json
{
  "runs": [
    {
      "id": 1,
      "provider": "marketplace_mock",
      "source": "marketplace_mock",
      "niche": "anime_collectibles",
      "region": "CA",
      "query": "anime collectibles Canada",
      "status": "COMPLETED",
      "startedAt": "2026-05-14T12:00:00Z",
      "completedAt": "2026-05-14T12:00:01Z",
      "durationMs": 1000,
      "recordsFetched": 3,
      "opportunitiesGenerated": 3,
      "scoringVersion": "v1",
      "errorMessage": null,
      "createdAt": "2026-05-14T12:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### System Status

```json
{
  "backendStatus": "OK",
  "dbConnectivity": "OK",
  "latestProviderRun": {},
  "latestScoringRun": {},
  "totalOpportunitiesStored": 3,
  "totalSourceRecordsStored": 3,
  "activeProviders": [
    {
      "sourceType": "marketplace_mock",
      "available": true
    }
  ],
  "generatedAt": "2026-05-14T12:00:00Z"
}
```

## Manual Validation

### Confirm Backend Works

```powershell
Invoke-RestMethod "http://localhost:8080/api/health"
Invoke-RestMethod "http://localhost:8080/api/opportunities?niche=anime_collectibles&region=CA"
```

The health endpoint should return `OK`. The opportunities endpoint should return an array of normalized opportunity snapshots.

### Confirm Frontend Works

Open:

```text
http://localhost:3000
```

Confirm the dashboard renders:

- top opportunity cards
- score badges
- score breakdown rows
- evidence summary
- risk badges
- generated-at text
- System Status card
- Last Refresh summary
- Recent Provider Runs table

Then open:

```text
http://localhost:3000/provider-runs
http://localhost:3000/system-status
```

Confirm each page loads with operational data instead of an error state.

### Confirm DB Connectivity

Docker:

```powershell
docker compose exec trend-radar-db psql -U trend_radar -d trend_radar
```

Non-Docker:

```powershell
psql -U trend_radar -d trend_radar
```

Then run:

```sql
SELECT COUNT(*) FROM databasechangelog;
SELECT COUNT(*) FROM provider_run;
SELECT COUNT(*) FROM source_record;
SELECT COUNT(*) FROM normalized_signal;
SELECT COUNT(*) FROM scoring_run;
SELECT COUNT(*) FROM opportunity_snapshot;
```

`databasechangelog` should contain 7 applied changesets. The other tables should receive rows after calling the opportunities or refresh endpoint.

### Confirm Provider Run History

Create a run:

```powershell
Invoke-RestMethod -Method Post "http://localhost:8080/api/opportunities/refresh?niche=anime_collectibles&region=CA"
```

Read run history:

```powershell
Invoke-RestMethod "http://localhost:8080/api/provider-runs?page=0&size=10" | ConvertTo-Json -Depth 6
```

Confirm the latest run includes:

- `id`
- `provider`
- `source`
- `niche`
- `region`
- `query`
- `status`
- `startedAt`
- `completedAt`
- `durationMs`
- `recordsFetched`
- `opportunitiesGenerated`
- `scoringVersion`
- `createdAt`

Confirm provider and scoring linkage in the database:

```sql
SELECT pr.id,
       pr.source_type,
       pr.status,
       sr.id AS scoring_run_id,
       sr.scoring_version,
       sr.opportunities_scored
FROM provider_run pr
LEFT JOIN scoring_run sr ON sr.provider_run_id = pr.id
ORDER BY pr.id DESC;
```

### Confirm System Status

```powershell
Invoke-RestMethod "http://localhost:8080/api/system/status" | ConvertTo-Json -Depth 6
```

Confirm:

- `backendStatus` is `OK`
- `dbConnectivity` is `OK`
- `latestProviderRun` is present after a refresh
- `latestScoringRun` is present after a refresh
- `totalOpportunitiesStored` increases or remains nonzero after opportunities are generated
- `totalSourceRecordsStored` increases or remains nonzero after provider records are stored
- `activeProviders` includes `marketplace_mock` and, when configured, eBay provider information

### Confirm eBay Integration

By default, eBay is disabled and the mock provider is used. To test eBay locally:

1. Set `TREND_RADAR_EBAY_ENABLED=true`.
2. Set `TREND_RADAR_EBAY_USERNAME` and `TREND_RADAR_EBAY_PASSWORD`.
3. Start or restart the backend.
4. Call:

```powershell
Invoke-RestMethod -Method Post "http://localhost:8080/api/opportunities/refresh?niche=anime_collectibles&region=CA"
```

5. Check the response or database:

```sql
SELECT source_type, query, status, records_fetched
FROM provider_run
ORDER BY id DESC;

SELECT source_type, title
FROM source_record
ORDER BY id DESC;
```

Successful eBay runs should show `source_type = 'ebay_browse'`. If eBay is disabled or credentials are missing, the latest successful provider should be `marketplace_mock`.

### Confirm Scoring Works

```powershell
Invoke-RestMethod "http://localhost:8080/api/opportunities?niche=anime_collectibles&region=CA" | ConvertTo-Json -Depth 6
```

Confirm each opportunity includes:

- `marketplaceProofScore`
- `priceViabilityScore`
- `freshnessScore`
- `sellerQualityScore`
- `shippingRiskScore`
- `competitionRiskScore`
- `finalScore`
- `scoreLabel`

The frontend displays these fields in each opportunity card's `Score Breakdown` section.

### Confirm Docker Setup Works

Start the full stack:

```powershell
docker compose up --build
```

In another terminal:

```powershell
docker compose ps
Invoke-RestMethod "http://localhost:8080/api/health"
Invoke-RestMethod "http://localhost:8080/api/opportunities?niche=anime_collectibles&region=CA"
Invoke-RestMethod "http://localhost:8080/api/provider-runs?page=0&size=5"
Invoke-RestMethod "http://localhost:8080/api/system/status"
```

Open:

```text
http://localhost:3000
http://localhost:3000/provider-runs
http://localhost:3000/system-status
```

Confirm backend logs show Liquibase:

```powershell
docker compose logs trend-radar-backend
```

Look for messages like `Liquibase: Update has been successful` and `Tomcat started on port 8080`.

## Screenshots

Placeholders for project documentation:

- Dashboard: `docs/screenshots/dashboard.png`
- Provider Runs: `docs/screenshots/provider-runs.png`
- System Status: `docs/screenshots/system-status.png`

## Tests And Build Checks

Backend tests:

```powershell
cd backend
& "C:\Users\vivek\Softwares\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin\mvn.cmd" test
```

Frontend production build:

```powershell
cd frontend
npm run build
```

Docker config validation:

```powershell
docker compose config
```

## Current Milestone Status

Implemented through Milestone 6:

- Milestone 1: initial Spring Boot backend, Next.js frontend, health endpoint, dashboard shell.
- Milestone 2: normalized opportunity API with mocked data.
- Milestone 3: optional eBay Browse provider and mock fallback.
- Milestone 4: Opportunity Scoring Engine v1 with score breakdowns and frontend display.
- Milestone 5: PostgreSQL persistence, Liquibase migrations, provider/scoring run tracking, raw source records, normalized signals, opportunity snapshots.
- Milestone 5.1: Dockerized local development with one-command startup.
- Milestone 6: provider run history APIs, system status API, operational dashboard sections, Provider Runs page, System Status page, audit timestamps, indexes, and observability tests.

## Next Planned Milestones

Planned, not yet implemented:

- OpenAI-powered explanation layer for richer opportunity reasoning.
- Google Trends or another demand provider.
- Scheduled ingestion and refresh cadence.
- Alerts, watchlists, or saved opportunities.
- Authentication and user-specific workspaces.
- More robust production configuration and deployment packaging.

## Troubleshooting

If ports are already in use, stop the conflicting process or update host-side mappings in `docker-compose.yml`.

If the backend cannot connect to PostgreSQL in Docker:

```powershell
docker compose ps
docker compose logs trend-radar-db
docker compose logs trend-radar-backend
```

If Liquibase fails after local schema experiments:

```powershell
docker compose down -v
docker compose up --build
```

If the frontend cannot load opportunities or operations data:

- Confirm the backend health endpoint works.
- Confirm `TREND_RADAR_API_BASE_URL` is set to `http://trend-radar-backend:8080` in Docker.
- Confirm these Next.js proxy routes respond:

```powershell
Invoke-RestMethod "http://localhost:3000/api/opportunities?niche=anime_collectibles&region=CA"
Invoke-RestMethod "http://localhost:3000/api/provider-runs?page=0&size=5"
Invoke-RestMethod "http://localhost:3000/api/system/status"
```
