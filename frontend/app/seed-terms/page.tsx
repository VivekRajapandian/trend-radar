"use client";

import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { formatDateTime } from "../format";
import type { IngestionRunSummary, SeedTerm, SystemStatus } from "../types";

export default function SeedTermsPage() {
  const [seedTerms, setSeedTerms] = useState<SeedTerm[]>([]);
  const [status, setStatus] = useState<SystemStatus | null>(null);
  const [summary, setSummary] = useState<IngestionRunSummary | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRunning, setIsRunning] = useState(false);
  const [form, setForm] = useState({
    niche: "anime_collectibles",
    region: "CA",
    searchTerm: "",
    priority: "50",
    sourceType: "ebay_browse"
  });

  async function loadData() {
    const query = typeof window === "undefined" ? "" : window.location.search;
    const [seedResponse, statusResponse] = await Promise.all([
      fetch(`/api/seed-terms${query}`, { cache: "no-store" }),
      fetch("/api/system/status", { cache: "no-store" })
    ]);

    if (!seedResponse.ok) {
      throw new Error(`Seed terms returned ${seedResponse.status}`);
    }

    setSeedTerms((await seedResponse.json()) as SeedTerm[]);

    if (statusResponse.ok) {
      setStatus((await statusResponse.json()) as SystemStatus);
    }
  }

  useEffect(() => {
    async function loadSeedTerms() {
      try {
        await loadData();
      } catch (caughtError) {
        setError(caughtError instanceof Error ? caughtError.message : "Unable to load seed terms");
      } finally {
        setIsLoading(false);
      }
    }

    loadSeedTerms();
  }, []);

  async function runIngestion() {
    try {
      setIsRunning(true);
      setError(null);
      const query = typeof window === "undefined" ? "" : window.location.search;
      const response = await fetch(`/api/ingestion/run${query}`, { method: "POST" });

      if (!response.ok) {
        throw new Error(`Ingestion returned ${response.status}`);
      }

      setSummary((await response.json()) as IngestionRunSummary);
      await loadData();
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Unable to run ingestion");
    } finally {
      setIsRunning(false);
    }
  }

  async function createSeedTerm(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      setError(null);
      const response = await fetch("/api/seed-terms", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          niche: form.niche,
          region: form.region,
          searchTerm: form.searchTerm,
          enabled: true,
          priority: Number(form.priority),
          sourceType: form.sourceType
        })
      });

      if (!response.ok) {
        throw new Error(`Create returned ${response.status}`);
      }

      setForm((current) => ({ ...current, searchTerm: "" }));
      await loadData();
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Unable to create seed term");
    }
  }

  async function toggleSeedTerm(seedTerm: SeedTerm) {
    try {
      setError(null);
      const response = await fetch(`/api/seed-terms/${seedTerm.id}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ enabled: !seedTerm.enabled })
      });

      if (!response.ok) {
        throw new Error(`Update returned ${response.status}`);
      }

      await loadData();
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Unable to update seed term");
    }
  }

  async function deleteSeedTerm(seedTerm: SeedTerm) {
    try {
      setError(null);
      const response = await fetch(`/api/seed-terms/${seedTerm.id}`, { method: "DELETE" });

      if (!response.ok) {
        throw new Error(`Delete returned ${response.status}`);
      }

      await loadData();
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Unable to delete seed term");
    }
  }

  return (
    <main className="dashboard-shell">
      <aside className="sidebar">
        <div className="brand-mark">
          <span className="brand-icon">T</span>
          <div>
            <p className="eyebrow">TrendRadar</p>
            <h1>Ingestion</h1>
          </div>
        </div>
        <nav className="nav-list" aria-label="Primary navigation">
          <a href="/">Dashboard</a>
          <a href="/seed-terms">Signals</a>
          <a href="/#opportunities">Opportunities</a>
          <a className="active" href="/seed-terms">Seed Terms</a>
          <a href="/provider-runs">Provider Runs</a>
          <a href="/system-status">System Status</a>
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar compact-page">
          <div>
            <p className="eyebrow">Seed Terms / Ingestion</p>
            <h2>Manage the search terms that feed product intelligence.</h2>
            <p className="generated-at">Latest ingestion {formatDateTime(status?.latestIngestionRunAt)}</p>
          </div>
          <button className="primary-action" type="button" onClick={runIngestion} disabled={isRunning}>
            {isRunning ? "Running..." : "Run ingestion"}
          </button>
        </header>

        <section className="ops-grid" aria-label="Ingestion summary">
          <article className="ops-card">
            <p className="eyebrow">Scheduler</p>
            <h3>{status?.schedulerEnabled ? "Enabled" : "Disabled"}</h3>
            <div className="ops-metadata">
              <span>Fixed rate</span>
              <strong>{status?.schedulerFixedRateMinutes ?? 360} min</strong>
            </div>
          </article>
          <article className="ops-card">
            <p className="eyebrow">Enabled Seeds</p>
            <h3>{status?.enabledSeedTermCount ?? seedTerms.filter((term) => term.enabled).length}</h3>
            <div className="ops-metadata">
              <span>Total configured</span>
              <strong>{seedTerms.length}</strong>
            </div>
          </article>
          <article className="ops-card">
            <p className="eyebrow">Manual Run</p>
            <h3>{summary ? `${summary.successfulRuns}/${summary.totalSeedTerms}` : "Ready"}</h3>
            <div className="ops-metadata">
              <span>Opportunities</span>
              <strong>{summary?.opportunitiesGenerated ?? "-"}</strong>
            </div>
          </article>
        </section>

        <form className="seed-form" onSubmit={createSeedTerm}>
          <label>
            <span>Niche</span>
            <input
              value={form.niche}
              onChange={(event) => setForm((current) => ({ ...current, niche: event.target.value }))}
            />
          </label>
          <label>
            <span>Region</span>
            <input
              value={form.region}
              onChange={(event) => setForm((current) => ({ ...current, region: event.target.value }))}
            />
          </label>
          <label className="wide-field">
            <span>Search term</span>
            <input
              required
              value={form.searchTerm}
              onChange={(event) => setForm((current) => ({ ...current, searchTerm: event.target.value }))}
              placeholder="e.g. anime display case"
            />
          </label>
          <label>
            <span>Priority</span>
            <input
              type="number"
              value={form.priority}
              onChange={(event) => setForm((current) => ({ ...current, priority: event.target.value }))}
            />
          </label>
          <label>
            <span>Source</span>
            <input
              value={form.sourceType}
              onChange={(event) => setForm((current) => ({ ...current, sourceType: event.target.value }))}
            />
          </label>
          <button className="primary-action" type="submit">Add seed</button>
        </form>

        {isLoading ? (
          <section className="state-panel">
            <span className="loading-bar" />
            <h3>Loading seed terms</h3>
          </section>
        ) : error ? (
          <section className="state-panel error-state">
            <h3>Ingestion workspace needs attention</h3>
            <p>{error}</p>
          </section>
        ) : (
          <section className="table-panel">
            <div className="seed-table header">
              <span>Status</span>
              <span>Niche</span>
              <span>Region</span>
              <span>Search term</span>
              <span>Priority</span>
              <span>Provider</span>
              <span>Created</span>
              <span>Updated</span>
              <span>Actions</span>
            </div>
            {seedTerms.map((seedTerm) => (
              <div className="seed-table" key={seedTerm.id}>
                <span className={`status-badge ${seedTerm.enabled ? "completed" : "watch"}`}>
                  {seedTerm.enabled ? "Enabled" : "Disabled"}
                </span>
                <strong>{seedTerm.niche}</strong>
                <span>{seedTerm.region}</span>
                <span>{seedTerm.searchTerm}</span>
                <span>{seedTerm.priority}</span>
                <span>{seedTerm.sourceType}</span>
                <span>{formatDateTime(seedTerm.createdAt)}</span>
                <span>{formatDateTime(seedTerm.updatedAt)}</span>
                <span className="row-actions">
                  <button type="button" onClick={() => toggleSeedTerm(seedTerm)}>
                    {seedTerm.enabled ? "Disable" : "Enable"}
                  </button>
                  <button type="button" onClick={() => deleteSeedTerm(seedTerm)}>Delete</button>
                </span>
              </div>
            ))}
          </section>
        )}

        {summary?.errors.length ? (
          <section className="state-panel error-state compact-state">
            <h3>Run completed with errors</h3>
            {summary.errors.map((runError) => (
              <p key={runError}>{runError}</p>
            ))}
          </section>
        ) : null}
      </section>
    </main>
  );
}
