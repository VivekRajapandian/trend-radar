"use client";

import { useEffect, useState } from "react";
import { formatDateTime, formatDuration } from "../format";
import type { SystemStatus } from "../types";

export default function SystemStatusPage() {
  const [status, setStatus] = useState<SystemStatus | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadStatus() {
      try {
        const response = await fetch("/api/system/status");

        if (!response.ok) {
          throw new Error(`Backend returned ${response.status}`);
        }

        setStatus((await response.json()) as SystemStatus);
      } catch (caughtError) {
        setError(caughtError instanceof Error ? caughtError.message : "Unable to load system status");
      } finally {
        setIsLoading(false);
      }
    }

    loadStatus();
  }, []);

  return (
    <main className="dashboard-shell">
      <aside className="sidebar">
        <div className="brand-mark">
          <span className="brand-icon">T</span>
          <div>
            <p className="eyebrow">TrendRadar</p>
            <h1>Operations</h1>
          </div>
        </div>
        <nav className="nav-list" aria-label="Primary navigation">
          <a href="/">Dashboard</a>
          <a href="#">Signals</a>
          <a href="#">Opportunities</a>
          <a href="/provider-runs">Provider Runs</a>
          <a className="active" href="/system-status">System Status</a>
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar compact-page">
          <div>
            <p className="eyebrow">System Status</p>
            <h2>Operational visibility for the TrendRadar stack.</h2>
            <p className="generated-at">Generated at {formatDateTime(status?.generatedAt)}</p>
          </div>
        </header>

        {isLoading ? (
          <section className="state-panel">
            <span className="loading-bar" />
            <h3>Checking system status</h3>
          </section>
        ) : error ? (
          <section className="state-panel error-state">
            <h3>Unable to load system status</h3>
            <p>{error}</p>
          </section>
        ) : (
          <section className="status-layout">
            <article className="ops-card">
              <p className="eyebrow">Backend</p>
              <h3>{status?.backendStatus}</h3>
              <div className="ops-metadata">
                <span>Database</span>
                <strong>{status?.dbConnectivity}</strong>
              </div>
            </article>
            <article className="ops-card">
              <p className="eyebrow">Stored Data</p>
              <h3>{status?.totalOpportunitiesStored ?? 0}</h3>
              <div className="ops-metadata">
                <span>Source records</span>
                <strong>{status?.totalSourceRecordsStored ?? 0}</strong>
              </div>
            </article>
            <article className="ops-card wide">
              <p className="eyebrow">Latest Refresh</p>
              <h3>{status?.latestProviderRun?.status ?? "No provider runs"}</h3>
              <div className="status-detail-grid">
                <div>
                  <span>Provider</span>
                  <strong>{status?.latestProviderRun?.source ?? "-"}</strong>
                </div>
                <div>
                  <span>Records</span>
                  <strong>{status?.latestProviderRun?.recordsFetched ?? "-"}</strong>
                </div>
                <div>
                  <span>Opportunities</span>
                  <strong>{status?.latestProviderRun?.opportunitiesGenerated ?? "-"}</strong>
                </div>
                <div>
                  <span>Duration</span>
                  <strong>{formatDuration(status?.latestProviderRun?.durationMs)}</strong>
                </div>
              </div>
            </article>
            <article className="ops-card wide">
              <p className="eyebrow">Provider Availability</p>
              <div className="provider-chips">
                {status?.activeProviders.map((provider) => (
                  <span className={`status-badge ${provider.available ? "completed" : "watch"}`} key={provider.sourceType}>
                    {provider.sourceType}: {provider.available ? "available" : "inactive"}
                  </span>
                ))}
              </div>
            </article>
          </section>
        )}
      </section>
    </main>
  );
}
