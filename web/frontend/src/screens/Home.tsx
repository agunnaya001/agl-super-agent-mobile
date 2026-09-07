import { useEffect, useState } from "react";
import { api } from "../api";
import { Screen } from "../types";
import { fmtUsd, fmtNum, timeAgo, statusPill, shortAddr, Markdown } from "../ui";
import { WalkthroughModal } from "../components/WalkthroughModal";

export function HomeScreen({ wallet, navigate }: { wallet: string; navigate: (s: Screen) => void }) {
  const [portfolio, setPortfolio] = useState<any>(null);
  const [eco, setEco] = useState<any>(null);
  const [txs, setTxs] = useState<any[]>([]);
  const [suggestions, setSuggestions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showWalkthrough, setShowWalkthrough] = useState(false);

  useEffect(() => {
    let alive = true;
    (async () => {
      const [p, e, t, s] = await Promise.all([
        api.getPortfolio(wallet).catch(() => null),
        api.getEcosystem().catch(() => null),
        api.getTransactions(wallet).catch(() => []),
        api.getAiSuggestions().catch(() => []),
      ]);
      if (!alive) return;
      setPortfolio(p); setEco(e); setTxs(t); setSuggestions(s); setLoading(false);
    })();
    return () => { alive = false; };
  }, [wallet]);

  if (loading) return <div className="center"><div className="loader" /></div>;

  const stats = eco?.stats;

  return (
    <div>
      {showWalkthrough && (
        <WalkthroughModal
          onClose={() => setShowWalkthrough(false)}
          onNavigate={navigate}
        />
      )}

      {/* New User Walkthrough / Help Banner */}
      <div
        className="card"
        style={{
          background: "linear-gradient(135deg, rgba(0, 210, 255, 0.12), rgba(168, 85, 247, 0.12))",
          borderColor: "rgba(0, 210, 255, 0.35)",
          padding: "12px 16px",
          marginBottom: 12,
        }}
      >
        <div className="row between" style={{ alignItems: "center" }}>
          <div className="row gap">
            <span style={{ fontSize: 20 }}>💡</span>
            <div>
              <div className="bold small">New to AGL Super Agent?</div>
              <div className="tiny muted">
                Explore key features: AI Agent, Wallet Analytics & 30-Day Trends
              </div>
            </div>
          </div>
          <button
            className="btn cyan"
            style={{ fontSize: 11, padding: "6px 12px", fontWeight: 700 }}
            onClick={() => setShowWalkthrough(true)}
          >
            Start Tour 🚀
          </button>
        </div>
      </div>

      {/* Portfolio hero */}
      <div className="card card-elev">
        <div className="small muted">Total Portfolio Value</div>
        <div style={{ fontSize: 32, fontWeight: 800, margin: "4px 0" }}>{fmtUsd(portfolio?.totalBalanceUsd ?? 0)}</div>
        <div className="row between">
          <span className="pill green">{portfolio?.change24hPercent >= 0 ? "+" : ""}{(portfolio?.change24hPercent ?? 0).toFixed(1)}% 24h</span>
          <span className="small muted">Base Mainnet · Chain 8453</span>
        </div>
        <div style={{ height: 12 }} />
        <div className="metrics">
          <div><div className="tiny muted">AGL Balance</div><div className="bold">{(portfolio?.aglBalance ?? 0).toFixed(2)}</div></div>
          <div><div className="tiny muted">AGL Staked (wAGL)</div><div className="bold">{(portfolio?.aglStaked ?? 0).toFixed(2)}</div></div>
          <div><div className="tiny muted">Rewards Earned</div><div className="bold">{(portfolio?.aglRewardsEarned ?? 0).toFixed(0)} AGL</div></div>
          <div><div className="tiny muted">Credits</div><div className="bold">{portfolio?.aglCredits ?? 0}</div></div>
        </div>
      </div>

      {/* Quick actions */}
      <div className="metrics">
        <button className="btn" onClick={() => navigate("WALLET")}>👛 Wallet</button>
        <button className="btn cyan" onClick={() => navigate("AI")}>🤖 Ask Agent</button>
      </div>
      <div style={{ height: 8 }} />
      <div className="metrics">
        <button className="btn ghost" onClick={() => navigate("STAKING")}>💎 Staking</button>
        <button className="btn ghost" onClick={() => setShowWalkthrough(true)}>❓ Help & Tour</button>
      </div>

      {/* Ecosystem stats */}
      {stats && (
        <>
          <div className="section-title">Ecosystem Stats</div>
          <div className="card">
            <div className="row between">
              <div><div className="tiny muted">AGL Price</div><div className="bold">{fmtUsd(stats.currentPriceUsd)}</div></div>
              <div><div className="tiny muted">Staking APR</div><div className="bold" style={{ color: "var(--neon)" }}>{stats.stakingAprPercent}%</div></div>
            </div>
            <div style={{ height: 10 }} />
            <div className="row between">
              <div><div className="tiny muted">Active Agents</div><div className="bold">{fmtNum(stats.totalActiveAgents)}</div></div>
              <div><div className="tiny muted">Contract Audits</div><div className="bold">{fmtNum(stats.totalContractAuditsCompleted)}</div></div>
            </div>
          </div>
        </>
      )}

      {/* AI suggestions */}
      {suggestions.length > 0 && (
        <>
          <div className="section-title">AI Suggestions</div>
          {suggestions.slice(0, 3).map((s) => (
            <div key={s.id} className="card" style={{ cursor: "pointer" }} onClick={() => navigate("AI")}>
              <div className="row between">
                <div className="row gap"><span style={{ fontSize: 20 }}>{s.icon}</span><span className="bold">{s.title}</span></div>
                <span className="pill cyan">{s.badge}</span>
              </div>
              <div className="small muted" style={{ marginTop: 6 }}>{s.description}</div>
            </div>
          ))}
        </>
      )}

      {/* Recent transactions */}
      <div className="section-title">Recent Activity</div>
      {txs.slice(0, 5).map((tx) => (
        <div key={tx.hash} className="list-row">
          <div className="avatar-circle" style={{ background: "var(--surface)" }}>{tx.type.includes("IN") ? "⬇️" : tx.type.includes("STAKE") ? "💎" : tx.type.includes("SWAP") ? "🔄" : "⚡"}</div>
          <div className="col" style={{ flex: 1 }}>
            <div className="bold small">{tx.type.replace(/_/g, " ")}</div>
            <div className="tiny muted">{tx.value} {tx.tokenSymbol} · {timeAgo(tx.timestamp)}</div>
          </div>
          {statusPill(tx.status)}
        </div>
      ))}
      <div style={{ height: 8 }} />
      <button className="btn ghost" onClick={() => navigate("DASHBOARD")}>📊 Open Monitor Dashboard</button>
    </div>
  );
}
