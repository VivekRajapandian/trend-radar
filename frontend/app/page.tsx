const signalCards = [
  { label: "Opportunity Index", value: "82", trend: "+12 pts", tone: "strong" },
  { label: "Rising Products", value: "24", trend: "+8 today", tone: "cool" },
  { label: "Seasonal Signals", value: "11", trend: "4 urgent", tone: "warm" }
];

const opportunities = [
  { product: "Compact travel organizers", score: 91, signal: "Marketplace momentum", stage: "Watch" },
  { product: "Desk cable kits", score: 86, signal: "Demand consistency", stage: "Research" },
  { product: "Pet cooling mats", score: 79, signal: "Seasonal lift", stage: "Validate" }
];

export default function Home() {
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
          <span>Focus queue</span>
          <p>New signals are grouped by momentum, seasonality, and confidence.</p>
        </div>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Product intelligence</p>
            <h2>Discover what is getting hot before everyone else.</h2>
          </div>
          <button className="ghost-button" type="button">Refresh</button>
        </header>

        <section className="metrics-grid" aria-label="Signal summary">
          {signalCards.map((card) => (
            <article className={`metric-card ${card.tone}`} key={card.label}>
              <p>{card.label}</p>
              <div>
                <strong>{card.value}</strong>
                <span>{card.trend}</span>
              </div>
            </article>
          ))}
        </section>

        <section className="content-grid">
          <article className="radar-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Radar view</p>
                <h3>Normalized opportunity signals</h3>
              </div>
              <span className="status-pill">Current</span>
            </div>
            <div className="radar-visual" aria-label="Decorative radar chart">
              <span className="ring ring-one" />
              <span className="ring ring-two" />
              <span className="ring ring-three" />
              <span className="radar-sweep" />
              <span className="radar-dot dot-one" />
              <span className="radar-dot dot-two" />
              <span className="radar-dot dot-three" />
            </div>
          </article>

          <article className="opportunity-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Top candidates</p>
                <h3>Opportunity queue</h3>
              </div>
              <span className="status-pill">Ranked</span>
            </div>
            <div className="opportunity-list">
              {opportunities.map((item) => (
                <div className="opportunity-row" key={item.product}>
                  <div>
                    <strong>{item.product}</strong>
                    <span>{item.signal}</span>
                  </div>
                  <p>{item.score}</p>
                  <em>{item.stage}</em>
                </div>
              ))}
            </div>
          </article>
        </section>
      </section>
    </main>
  );
}
