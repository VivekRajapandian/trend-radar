# TrendRadar Codex Instructions

Always read docs/PROJECT_BRIEF.md before making changes.

Build milestone by milestone.

Do not implement more than the requested milestone.

After every task, include:
1. What changed
2. How to run it
3. How to manually validate it
4. Any risks or assumptions

Tech stack:
- Backend: Spring Boot
- Frontend: Next.js
- Database: PostgreSQL later
- Initial source: eBay API
- Future source: Google Trends or another demand provider

Architecture rules:
- Build backend as a modular monolith.
- Do not expose eBay-specific models directly to frontend.
- Frontend should consume normalized opportunity APIs.
- Keep provider layer extensible.
- Never commit API keys or secrets.