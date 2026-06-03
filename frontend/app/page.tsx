"use client";

import { useEffect, useMemo, useState } from "react";
import { formatDateTime, formatDuration } from "./format";
import type { ProviderRun, ProviderRunPage, SystemStatus } from "./types";

type ProductConcept = {
  id: string;
  name: string;
  category: string;
  imageUrl: string | null;
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

const OPPORTUNITIES_URL = "/api/opportunities?niche=anime_collectibles&region=CA";
const PROVIDER_RUNS_URL = "/api/provider-runs?size=5";
const SYSTEM_STATUS_URL = "/api/system/status";

export default function Home() {
  const [opportunities, setOpportunities] = useState<OpportunitySnapshot[]>([]);
  const [providerRuns, setProviderRuns] = useState<ProviderRun[]>([]);
  const [systemStatus, setSystemStatus] = useState<SystemStatus | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRunningIngestion, setIsRunningIngestion] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadDashboard(signal?: AbortSignal) {
      try {
        setIsLoading(true);
        setError(null);

        const [response, providerRunsResponse, systemStatusResponse] = await Promise.all([
          fetch(OPPORTUNITIES_URL, { signal }),
          fetch(PROVIDER_RUNS_URL, { signal }),
          fetch(SYSTEM_STATUS_URL, { signal })
        ]);

        if (!response.ok) {
          throw new Error(`Backend returned ${response.status}`);
        }

        const data = (await response.json()) as OpportunitySnapshot[];
        setOpportunities(data);

        if (providerRunsResponse.ok) {
          const providerRunPage = (await providerRunsResponse.json()) as ProviderRunPage;
          setProviderRuns(providerRunPage.runs);
        }

        if (systemStatusResponse.ok) {
          setSystemStatus((await systemStatusResponse.json()) as SystemStatus);
        }
      } catch (caughtError) {
        if (caughtError instanceof DOMException && caughtError.name === "AbortError") {
          return;
        }

        setError(caughtError instanceof Error ? caughtError.message : "Unable to load opportunities");
      } finally {
        setIsLoading(false);
      }
  }

  useEffect(() => {
    const controller = new AbortController();

    loadDashboard(controller.signal);

    return () => controller.abort();
  }, []);

  async function runIngestion() {
    try {
      setIsRunningIngestion(true);
      setError(null);
      const response = await fetch("/api/ingestion/run?niche=anime_collectibles&region=CA", { method: "POST" });

      if (!response.ok) {
        throw new Error(`Ingestion returned ${response.status}`);
      }

      await loadDashboard();
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Unable to run ingestion");
    } finally {
      setIsRunningIngestion(false);
    }
  }

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
  const lastGeneratedAt = opportunities[0]?.generatedAt
    ? new Intl.DateTimeFormat("en-CA", {
        dateStyle: "medium",
        timeStyle: "short"
      }).format(new Date(opportunities[0].generatedAt))
    : "Waiting for first run";

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
          <a className="active" href="/">Dashboard</a>
          <a href="/seed-terms">Signals</a>
          <a href="#opportunities">Opportunities</a>
          <a href="/seed-terms">Seed Terms</a>
          <a href="/provider-runs">Provider Runs</a>
          <a href="/system-status">System Status</a>
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
            <p className="generated-at">Generated at {lastGeneratedAt}</p>
          </div>
          <div className="selector-row" aria-label="Opportunity filters">
            <a className="selector-button" href="/seed-terms?niche=anime_collectibles&region=CA">
              <span>Niche</span>
              {activeNiche}
            </a>
            <a className="selector-button" href="/seed-terms?niche=anime_collectibles&region=CA">
              <span>Region</span>
              {activeRegion}
            </a>
            <button className="primary-action" type="button" onClick={runIngestion} disabled={isRunningIngestion}>
              {isRunningIngestion ? "Running..." : "Run ingestion"}
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

        {!isLoading && !error ? (
          <section className="ops-grid" aria-label="Operational summary">
            <article className="ops-card">
              <p className="eyebrow">System Status</p>
              <h3>{systemStatus?.backendStatus ?? "Unknown"}</h3>
              <div className="ops-metadata">
                <span>Database</span>
                <strong>{systemStatus?.dbConnectivity ?? "-"}</strong>
              </div>
              <div className="ops-metadata">
                <span>Opportunities stored</span>
                <strong>{systemStatus?.totalOpportunitiesStored ?? "-"}</strong>
              </div>
            </article>
            <article className="ops-card">
              <p className="eyebrow">Scheduler</p>
              <h3>{systemStatus?.schedulerEnabled ? "Enabled" : "Disabled"}</h3>
              <div className="ops-metadata">
                <span>Enabled seeds</span>
                <strong>{systemStatus?.enabledSeedTermCount ?? "-"}</strong>
              </div>
              <div className="ops-metadata">
                <span>Latest ingestion</span>
                <strong>{formatDateTime(systemStatus?.latestIngestionRunAt)}</strong>
              </div>
            </article>
            <article className="ops-card">
              <p className="eyebrow">Last Refresh</p>
              <h3>{systemStatus?.latestProviderRun?.status ?? "No runs yet"}</h3>
              <div className="ops-metadata">
                <span>Provider</span>
                <strong>{systemStatus?.latestProviderRun?.source ?? "-"}</strong>
              </div>
              <div className="ops-metadata">
                <span>Completed</span>
                <strong>{formatDateTime(systemStatus?.latestProviderRun?.completedAt)}</strong>
              </div>
            </article>
            <article className="ops-card wide">
              <div className="panel-heading compact">
                <div>
                  <p className="eyebrow">Recent Provider Runs</p>
                  <h3>Execution history</h3>
                </div>
                <a className="text-link" href="/provider-runs">View all</a>
              </div>
              <div className="compact-table">
                {providerRuns.slice(0, 4).map((run) => (
                  <div className="run-row" key={run.id}>
                    <span className={`status-badge ${run.status.toLowerCase()}`}>{run.status}</span>
                    <strong>{run.source}</strong>
                    <span>{run.recordsFetched} records</span>
                    <span>{formatDuration(run.durationMs)}</span>
                  </div>
                ))}
              </div>
            </article>
          </section>
        ) : null}

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
          <section className="content-grid" id="opportunities">
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
                  <details className="opportunity-card compact-opportunity" key={opportunity.productConcept.id}>
                    <summary className="opportunity-summary">
                      <div className="product-thumb" aria-hidden="true">
                        {opportunity.productConcept.imageUrl ? (
                          <img src={opportunity.productConcept.imageUrl} alt="" />
                        ) : (
                          <span>{opportunity.productConcept.name.slice(0, 1)}</span>
                        )}
                      </div>
                      <div className="opportunity-summary-main">
                        <p className="category-label">{opportunity.productConcept.category}</p>
                        <h4>{opportunity.productConcept.name}</h4>
                      </div>
                      <div className="compact-metric">
                        <span>Median</span>
                        <strong>${Number(opportunity.marketplaceEvidence.medianPrice).toFixed(2)}</strong>
                      </div>
                      <div className="compact-metric">
                        <span>Listings</span>
                        <strong>{opportunity.marketplaceEvidence.activeListings}</strong>
                      </div>
                      <div className="score-badge compact-score">
                        <strong>{opportunity.finalScore}</strong>
                        <span>{opportunity.scoreLabel}</span>
                      </div>
                      <span className="expand-cue">Details</span>
                    </summary>

                    <div className="opportunity-details">
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
                    </div>
                  </details>
                ))}
              </div>
            </article>

            <article className="evidence-panel">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">Evidence summary</p>
                  <h3>Why these concepts surfaced</h3>
                </div>
                <span className="status-pill">Provider sources</span>
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
