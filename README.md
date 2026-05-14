# TrendRadar

TrendRadar is a product opportunity intelligence platform for online sellers.

This repository is an initial monorepo with:

- `backend`: Spring Boot API skeleton
- `frontend`: Next.js dashboard shell

Milestone 1 intentionally does not include eBay integration, persistence, scoring, or provider-specific models.

## Prerequisites

- Java 17
- Maven 3.9+
- Node.js 20+
- npm 10+

## Run the Backend

```powershell
cd backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`.

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "OK"
}
```

## Run the Frontend

```powershell
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:3000`.

## Manual Validation

1. Start the backend and open `http://localhost:8080/api/health`.
2. Confirm the response is JSON with `status` set to `OK`.
3. Start the frontend and open `http://localhost:3000`.
4. Confirm the TrendRadar dashboard shell renders with the overview, signal cards, radar panel, and opportunity table.
