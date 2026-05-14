"use client";

import { useEffect, useState } from "react";
import { formatDateTime, formatDuration } from "../format";
import type { ProviderRun, ProviderRunPage } from "../types";

export default function ProviderRunsPage() {
  const [runs, setRuns] = useState<ProviderRun[]>([]);
  const [total, setTotal] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadRuns() {
      try {
        const response = await fetch("/api/provider-runs?size=25");

        if (!response.ok) {
          throw new Error(`Backend returned ${response.status}`);
        }

        const page = (await response.json()) as ProviderRunPage;
        setRuns(page.runs);
        setTotal(page.totalElements);
      } catch (caughtError) {
        setError(caughtError instanceof Error ? caughtError.message : "Unable to load provider runs");
      } finally {
        setIsLoading(false);
      }
    }

    loadRuns();
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
          <a className="active" href="/provider-runs">Provider Runs</a>
          <a href="/system-status">System Status</a>
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar compact-page">
          <div>
            <p className="eyebrow">Provider Runs</p>
            <h2>Refresh history and provider execution detail.</h2>
            <p className="generated-at">{total} runs stored</p>
          </div>
        </header>

        {isLoading ? (
          <section className="state-panel">
            <span className="loading-bar" />
            <h3>Loading provider runs</h3>
          </section>
        ) : error ? (
          <section className="state-panel error-state">
            <h3>Unable to load provider runs</h3>
            <p>{error}</p>
          </section>
        ) : (
          <section className="table-panel">
            <div className="provider-table header">
              <span>Status</span>
              <span>Provider</span>
              <span>Niche</span>
              <span>Region</span>
              <span>Records</span>
              <span>Opportunities</span>
              <span>Duration</span>
              <span>Started</span>
            </div>
            {runs.map((run) => (
              <div className="provider-table" key={run.id}>
                <span className={`status-badge ${run.status.toLowerCase()}`}>{run.status}</span>
                <strong>{run.source}</strong>
                <span>{run.niche}</span>
                <span>{run.region}</span>
                <span>{run.recordsFetched}</span>
                <span>{run.opportunitiesGenerated}</span>
                <span>{formatDuration(run.durationMs)}</span>
                <span>{formatDateTime(run.startedAt)}</span>
              </div>
            ))}
          </section>
        )}
      </section>
    </main>
  );
}
