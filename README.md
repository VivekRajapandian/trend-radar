# TrendRadar

TrendRadar is a product opportunity intelligence platform for online sellers.

This repository is a monorepo with:

- `backend`: Spring Boot API
- `frontend`: Next.js dashboard
- `postgres`: local persistence for provider runs, source records, normalized signals, scoring runs, and opportunity snapshots

Milestone 5.1 adds a Dockerized local development environment. One command starts PostgreSQL, the Spring Boot backend, and the Next.js frontend. The app still runs without eBay credentials by using the mock provider. No OpenAI, Google Trends, Kubernetes, Redis, message queues, auth, scheduled ingestion, or committed secrets are included.

## Docker Setup

Prerequisites:

- Docker Desktop
- Docker Compose v2

Start the full stack from the repository root:

```powershell
docker compose up --build
```

Expected URLs:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

Docker services:

- `trend-radar-db`
- `trend-radar-backend`
- `trend-radar-frontend`

The backend runs with the `docker` Spring profile and connects to PostgreSQL at `trend-radar-db:5432`. Liquibase runs automatically when the backend starts.

## Docker Validation

Backend health:

```powershell
Invoke-RestMethod "http://localhost:8080/api/health"
```

Opportunities endpoint:

```powershell
Invoke-RestMethod "http://localhost:8080/api/opportunities?niche=anime_collectibles&region=CA"
```

Manual refresh:

```powershell
Invoke-RestMethod -Method Post "http://localhost:8080/api/opportunities/refresh?niche=anime_collectibles&region=CA"
```

Frontend dashboard:

```text
http://localhost:3000
```

Confirm the dashboard shows opportunity cards, score breakdowns, evidence summaries, risk badges, and `Generated at` text.

## Verify Docker Database

Open psql inside the database container:

```powershell
docker compose exec trend-radar-db psql -U trend_radar -d trend_radar
```

Check Liquibase and persistence tables:

```sql
SELECT * FROM databasechangelog;
SELECT id, source_type, query, region, status, records_fetched FROM provider_run ORDER BY id DESC;
SELECT id, provider_run_id, source_type, external_id FROM source_record ORDER BY id DESC;
SELECT id, provider_run_id, product_concept_id, product_name, price FROM normalized_signal ORDER BY id DESC;
SELECT id, provider_run_id, scoring_version, status, opportunities_scored FROM scoring_run ORDER BY id DESC;
SELECT id, product_name, final_score, generated_at FROM opportunity_snapshot ORDER BY id DESC;
```

Rows should appear after calling the opportunities or refresh endpoint. When eBay is disabled or unavailable, rows are persisted from the mock provider.

## Docker Stop And Rebuild

Stop containers:

```powershell
docker compose down
```

Stop containers and remove the persisted database volume:

```powershell
docker compose down -v
```

Rebuild from scratch:

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

## Common Troubleshooting

If the backend fails to connect to PostgreSQL, confirm the database container is healthy:

```powershell
docker compose ps
docker compose logs trend-radar-db
```

If Liquibase fails after schema experiments, reset the local database volume:

```powershell
docker compose down -v
docker compose up --build
```

If the frontend cannot load opportunities, confirm the backend is healthy and the frontend container has:

```text
TREND_RADAR_API_BASE_URL=http://trend-radar-backend:8080
```

If port `3000`, `8080`, or `5432` is already in use, stop the conflicting local process or change the host-side port mapping in `docker-compose.yml`.

## Local Non-Docker Setup

Prerequisites:

- Java 17
- Maven 3.9+
- Node.js 20+
- npm 10+
- PostgreSQL 15+

Create a local database and user:

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

Run backend:

```powershell
cd backend
& "C:\Users\vivek\Softwares\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin\mvn.cmd" spring-boot:run
```

Run frontend:

```powershell
cd frontend
npm install
npm run dev
```

For local non-Docker frontend development, the Next proxy defaults to `http://localhost:8080`. Override it with `TREND_RADAR_API_BASE_URL` if needed.

## Expected Opportunity Shape

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
    "score": 82,
    "scoreLabel": "High",
    "marketplaceProofScore": 74,
    "priceViabilityScore": 78,
    "freshnessScore": 100,
    "sellerQualityScore": 90,
    "shippingRiskScore": 80,
    "competitionRiskScore": 85,
    "finalScore": 82,
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
        "confidence": 0.9,
        "observedAt": "2026-05-14T00:00:00Z"
      }
    ],
    "risks": [],
    "explanation": "Human-readable scoring rationale.",
    "generatedAt": "2026-05-14T00:00:00Z"
  }
]
```
