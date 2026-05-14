# TrendRadar

TrendRadar is a product opportunity intelligence platform for online sellers.

This repository is a monorepo with:

- `backend`: Spring Boot API
- `frontend`: Next.js dashboard

Milestone 4 adds Opportunity Scoring Engine v1. The backend still exposes normalized TrendRadar opportunity snapshots, and the frontend still consumes that normalized contract. No database, OpenAI, Google Trends, scheduled ingestion, auth, or committed secrets are included.

## Prerequisites

- Java 17
- Maven 3.9+
- Node.js 20+
- npm 10+

## eBay Local Configuration

The backend is safe by default: eBay is disabled unless explicitly enabled and configured. Without eBay credentials, `/api/opportunities` falls back to mocked normalized marketplace data.

Create `backend/.env` from `backend/.env.example`:

```properties
TREND_RADAR_EBAY_ENABLED=true
TREND_RADAR_EBAY_TOKEN_URL=https://api.sandbox.ebay.com/identity/v1/oauth2/token
TREND_RADAR_EBAY_SEARCH_URL=https://api.ebay.com/buy/browse/v1/item_summary/search
TREND_RADAR_EBAY_USERNAME=your-ebay-client-id
TREND_RADAR_EBAY_PASSWORD=your-ebay-client-secret
TREND_RADAR_EBAY_SCOPE=https://api.ebay.com/oauth/api_scope
TREND_RADAR_EBAY_LIMIT=10
TREND_RADAR_EBAY_MARKETPLACE_ID=EBAY_CA
```

`backend/.env` is ignored by git. In higher environments, pass the same values as environment variables instead of using a local file.

## Run the Backend

```powershell
cd backend
& "C:\Users\vivek\Softwares\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin\mvn.cmd" spring-boot:run
```

The backend starts on `http://localhost:8080`.

## Run the Frontend

```powershell
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:3000` and calls the backend at `http://localhost:8080`.

## Manual Validation

Backend health endpoint:

```powershell
Invoke-RestMethod "http://localhost:8080/api/health"
```

Expected:

```json
{
  "status": "OK"
}
```

Opportunities endpoint:

```powershell
Invoke-RestMethod "http://localhost:8080/api/opportunities?niche=anime_collectibles&region=CA"
```

Exact browser URL:

```text
http://localhost:8080/api/opportunities?niche=anime_collectibles&region=CA
```

Expected response shape:

```json
[
  {
    "productConcept": {
      "id": "v1|227244758280|0",
      "name": "Reincarnated as a Slime Figure",
      "category": "Other Animation Merchandise"
    },
    "niche": {
      "code": "anime_collectibles",
      "displayName": "Anime collectibles"
    },
    "region": {
      "code": "CA",
      "displayName": "Canada"
    },
    "score": 78,
    "scoreLabel": "Promising",
    "marketplaceProofScore": 98,
    "priceViabilityScore": 70,
    "freshnessScore": 80,
    "sellerQualityScore": 100,
    "shippingRiskScore": 80,
    "competitionRiskScore": 65,
    "finalScore": 78,
    "marketplaceEvidence": {
      "estimatedSoldCount": 6820,
      "activeListings": 10,
      "medianPrice": 153.87,
      "minPrice": 14.38,
      "maxPrice": 431.4,
      "demandSignal": "Live eBay Browse result with 6820 total matches for query \"slime anime figure\"; seller feedback 99.9%"
    },
    "sourceEvidence": [
      {
        "sourceType": "ebay_browse",
        "title": "Reincarnated as a Slime Figure Set Collectible Anime Preowned Authentic",
        "confidence": 0.95,
        "observedAt": "2026-03-06T13:09:47Z"
      }
    ],
    "risks": [
      {
        "type": "licensed_ip",
        "severity": "MEDIUM",
        "description": "Character merchandise may require careful sourcing"
      }
    ],
    "explanation": "Short human-readable rationale for the opportunity.",
    "generatedAt": "2026-05-14T00:00:00Z"
  }
]
```

If eBay is disabled or unavailable, the same endpoint returns the same normalized shape with `sourceEvidence[0].sourceType` set to `marketplace_mock`.

Frontend validation:

1. Start the backend on port `8080`.
2. Start the frontend on port `3000`.
3. Open `http://localhost:3000`.
4. Confirm the dashboard shows top opportunity cards from the backend.
5. Confirm each opportunity card shows the final score badge.
6. Confirm each opportunity card includes a `Score Breakdown` section with marketplace proof, price viability, freshness, seller quality, shipping risk, and competition risk.
7. Stop the backend and refresh the frontend to confirm the error state appears.
