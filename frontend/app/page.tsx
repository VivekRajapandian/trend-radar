"use client";

import { useEffect, useMemo, useState } from "react";

type ProductConcept = {
  id: string;
  name: string;
  category: string;
};

type Niche = {
  code: string;
  displayName: string;
};

type Region = {
  code: string;
  displayName: string;
};

type MarketplaceEvidence = {
  estimatedSoldCount: number;
  activeListings: number;
  medianPrice: number;
  minPrice: number;
  maxPrice: number;
  demandSignal: string;
};

type SourceEvidence = {
  sourceType: string;
  title: string;
  confidence: number;
  observedAt: string;
};

type RiskSignal = {
  type: string;
  severity: string;
  description: string;
};

type OpportunitySnapshot = {
  productConcept: ProductConcept;
  niche: Niche;
  region: Region;
  score: number;
  scoreLabel: string;
  marketplaceProofScore: number;
  priceViabilityScore: number;
  freshnessScore: number;
  sellerQualityScore: number;
  shippingRiskScore: number;
  competitionRiskScore: number;
  finalScore: number;
  marketplaceEvidence: MarketplaceEvidence;
  sourceEvidence: SourceEvidence[];
  risks: RiskSignal[];
  explanation: string;
  generatedAt: string;
};

const OPPORTUNITIES_URL =
  "http://localhost:8080/api/opportunities?niche=anime_collectibles&region=CA";

export default function Home() {
  const [opportunities, setOpportunities] = useState<OpportunitySnapshot[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadOpportunities() {
      try {
        setIsLoading(true);
        setError(null);

        const response = await fetch(OPPORTUNITIES_URL, {
          signal: controller.signal
        });

        if (!response.ok) {
          throw new Error(`Backend returned ${response.status}`);
        }

        const data = (await response.json()) as OpportunitySnapshot[];
        setOpportunities(data);
      } catch (caughtError) {
        if (caughtError instanceof DOMException && caughtError.name === "AbortError") {
          return;
        }

        setError(caughtError instanceof Error ? caughtError.message : "Unable to load opportunities");
      } finally {
        setIsLoading(false);
      }
    }

    loadOpportunities();

    return () => controller.abort();
  }, []);

  const summary = useMemo(() => {
    const topScore = opportunities.length
      ? Math.max(...opportunities.map((opportunity) => opportunity.score))
      : 0;

    const riskCount = opportunities.reduce(
      (count, opportunity) => count + opportunity.risks.length,
      0
    );

    return [
      { label: "Top Score", value: topScore ? String(topScore) : "-", detail: "normalized", tone: "strong" },
      { label: "Opportunities", value: String(opportunities.length), detail: "ranked", tone: "cool" },
      { label: "Risk Signals", value: String(riskCount), detail: "review", tone: "warm" }
    ];
  }, [opportunities]);

  const activeNiche = opportunities[0]?.niche.displayName ?? "Anime collectibles";
  const activeRegion = opportunities[0]?.region.displayName ?? "Canada";

  return (
    <main className="dashboard-shell">
      <aside className="sidebar">
        <div className="brand-mark">
          <span className="brand-icon">T</span>
          <div>
            <p className="eyebrow">TrendRadar</p>
            <h1>Opportunity OS</h1>
          </div>
        </div>
        <nav className="nav-list" aria-label="Primary navigation">
          <a className="active" href="#">Radar</a>
          <a href="#">Signals</a>
          <a href="#">Opportunities</a>
          <a href="#">Providers</a>
        </nav>
        <div className="sidebar-note">
          <span>Normalized API</span>
          <p>Cards are rendered from backend opportunity snapshots, not marketplace-shaped data.</p>
        </div>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Product intelligence</p>
            <h2>Opportunity signals for focused seller research.</h2>
          </div>
          <div className="selector-row" aria-label="Opportunity filters">
            <button className="selector-button" type="button">
              <span>Niche</span>
              {activeNiche}
            </button>
            <button className="selector-button" type="button">
              <span>Region</span>
              {activeRegion}
            </button>
          </div>
        </header>

        <section className="metrics-grid" aria-label="Signal summary">
          {summary.map((card) => (
            <article className={`metric-card ${card.tone}`} key={card.label}>
              <p>{card.label}</p>
              <div>
                <strong>{card.value}</strong>
                <span>{card.detail}</span>
              </div>
            </article>
          ))}
        </section>

        {isLoading ? (
          <section className="state-panel" aria-live="polite">
            <span className="loading-bar" />
            <h3>Loading opportunities</h3>
            <p>TrendRadar is asking the backend for normalized opportunity snapshots.</p>
          </section>
        ) : error ? (
          <section className="state-panel error-state" aria-live="polite">
            <h3>Unable to reach the opportunity API</h3>
            <p>{error}</p>
            <p>Start the Spring Boot backend on port 8080, then refresh this page.</p>
          </section>
        ) : (
          <section className="content-grid">
            <article className="opportunity-panel">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">Top opportunities</p>
                  <h3>Ranked product concepts</h3>
                </div>
                <span className="status-pill">Backend data</span>
              </div>

              <div className="opportunity-list">
                {opportunities.map((opportunity) => (
                  <article className="opportunity-card" key={opportunity.productConcept.id}>
                    <div className="opportunity-card-header">
                      <div>
                        <p className="category-label">{opportunity.productConcept.category}</p>
                        <h4>{opportunity.productConcept.name}</h4>
                      </div>
                      <div className="score-badge">
                        <strong>{opportunity.finalScore}</strong>
                        <span>{opportunity.scoreLabel}</span>
                      </div>
                    </div>

                    <p className="explanation">{opportunity.explanation}</p>

                    <div className="evidence-summary">
                      <div>
                        <span>Sold</span>
                        <strong>{opportunity.marketplaceEvidence.estimatedSoldCount}</strong>
                      </div>
                      <div>
                        <span>Listings</span>
                        <strong>{opportunity.marketplaceEvidence.activeListings}</strong>
                      </div>
                      <div>
                        <span>Median</span>
                        <strong>${Number(opportunity.marketplaceEvidence.medianPrice).toFixed(2)}</strong>
                      </div>
                    </div>

                    <section className="score-breakdown" aria-label={`${opportunity.productConcept.name} score breakdown`}>
                      <div className="breakdown-heading">
                        <strong>Score Breakdown</strong>
                        <span>Final {opportunity.finalScore}</span>
                      </div>
                      <div className="breakdown-grid">
                        {([
                          ["Marketplace proof", opportunity.marketplaceProofScore],
                          ["Price viability", opportunity.priceViabilityScore],
                          ["Freshness", opportunity.freshnessScore],
                          ["Seller quality", opportunity.sellerQualityScore],
                          ["Shipping risk", opportunity.shippingRiskScore],
                          ["Competition risk", opportunity.competitionRiskScore]
                        ] as Array<[string, number]>).map(([label, value]) => (
                          <div className="breakdown-row" key={label}>
                            <span>{label}</span>
                            <div>
                              <i style={{ width: `${value}%` }} />
                            </div>
                            <strong>{value}</strong>
                          </div>
                        ))}
                      </div>
                    </section>

                    <div className="risk-row" aria-label={`${opportunity.productConcept.name} risk signals`}>
                      {opportunity.risks.map((risk) => (
                        <span className={`risk-badge ${risk.severity.toLowerCase()}`} key={risk.type}>
                          {risk.severity}: {risk.type.replaceAll("_", " ")}
                        </span>
                      ))}
                    </div>
                  </article>
                ))}
              </div>
            </article>

            <article className="evidence-panel">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">Evidence summary</p>
                  <h3>Why these concepts surfaced</h3>
                </div>
                <span className="status-pill">Mock sources</span>
              </div>

              <div className="source-list">
                {opportunities.flatMap((opportunity) =>
                  opportunity.sourceEvidence.map((source) => (
                    <div className="source-row" key={`${opportunity.productConcept.id}-${source.title}`}>
                      <div>
                        <strong>{source.title}</strong>
                        <span>{opportunity.productConcept.name}</span>
                      </div>
                      <p>{Math.round(source.confidence * 100)}%</p>
                    </div>
                  ))
                )}
              </div>
            </article>
          </section>
        )}
      </section>
    </main>
  );
}
