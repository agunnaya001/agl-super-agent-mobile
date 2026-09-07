import { useEffect, useState } from "react";
import { api } from "../api";
import { Screen } from "../types";
import { shortAddr, tierPill } from "../ui";

export function ProfileScreen({ wallet, navigate }: { wallet: string; navigate: (s: Screen) => void }) {
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => { api.getProfile(wallet).then((p) => { setProfile(p); setLoading(false); }).catch(() => setLoading(false)); }, [wallet]);

  if (loading || !profile) return <div className="center"><div className="loader" /></div>;

  return (
    <div>
      <div className="card card-elev" style={{ textAlign: "center" }}>
        <div style={{ fontSize: 48 }}>🤖</div>
        <div style={{ fontSize: 20, fontWeight: 800, marginTop: 6 }}>Super Agent</div>
        <div className="mono small" style={{ color: "var(--cyan)", marginTop: 4 }}>{shortAddr(wallet)}</div>
        <div style={{ height: 10 }} />
        <div className="row gap" style={{ justifyContent: "center" }}>{tierPill(profile.tier)}<span className="pill cyan">Lv {profile.level}</span></div>
      </div>

      <div className="metrics">
        <div className="card" style={{ textAlign: "center" }}><div style={{ fontSize: 20, fontWeight: 800 }}>{profile.totalXp.toLocaleString()}</div><div className="tiny muted">Total XP</div></div>
        <div className="card" style={{ textAlign: "center" }}><div style={{ fontSize: 20, fontWeight: 800 }}>{profile.consecutiveStreakDays} 🔥</div><div className="tiny muted">Day Streak</div></div>
      </div>
      <div className="metrics" style={{ marginTop: 12 }}>
        <div className="card" style={{ textAlign: "center" }}><div style={{ fontSize: 20, fontWeight: 800 }}>{profile.questsCompletedCount}</div><div className="tiny muted">Quests Done</div></div>
        <div className="card" style={{ textAlign: "center" }}><div style={{ fontSize: 20, fontWeight: 800 }}>{profile.contractsAnalyzedCount}</div><div className="tiny muted">Contracts Scanned</div></div>
      </div>

      <div className="section-title">Badges</div>
      {profile.badges.map((b: any) => (
        <div key={b.id} className="list-row" style={b.isUnlocked ? {} : { opacity: 0.5 }}>
          <div className="avatar-circle" style={{ background: b.isUnlocked ? "rgba(0,230,153,0.12)" : "var(--surface)" }}>{b.iconEmoji}</div>
          <div className="col" style={{ flex: 1 }}>
            <div className="bold small">{b.title}</div>
            <div className="tiny muted">{b.description}</div>
          </div>
          {b.isUnlocked ? <span className="pill green">UNLOCKED</span> : <span className="pill" style={{ background: "var(--surface)", color: "var(--muted)" }}>LOCKED</span>}
        </div>
      ))}

      <div className="section-title">Explore</div>
      <div className="metrics">
        <button className="btn ghost" onClick={() => navigate("AGL_TOKEN")}>🪙 AGL Token</button>
        <button className="btn ghost" onClick={() => navigate("CREDITS")}>⚡ Credits</button>
      </div>
      <div className="metrics" style={{ marginTop: 8 }}>
        <button className="btn ghost" onClick={() => navigate("WAGL")}>🗳️ wAGL</button>
        <button className="btn ghost" onClick={() => navigate("STAKING")}>💎 Staking</button>
      </div>
      <div className="metrics" style={{ marginTop: 8 }}>
        <button className="btn ghost" onClick={() => navigate("GOVERNANCE")}>🏛️ Governance</button>
        <button className="btn ghost" onClick={() => navigate("TIMELOCK")}>⏳ Timelock</button>
      </div>
      <div className="metrics" style={{ marginTop: 8 }}>
        <button className="btn ghost" onClick={() => navigate("DIAGNOSTICS")}>🔧 Diagnostics</button>
        <button className="btn ghost" onClick={() => navigate("PRICE_ALERTS")}>🔔 Price Alerts</button>
      </div>

      <div className="section-title">Advanced Tools</div>
      <div className="metrics">
        <button className="btn ghost" onClick={() => navigate("SECURITY")}>🛡️ Security Center</button>
        <button className="btn ghost" onClick={() => navigate("WATCHLIST")}>👁️ Watchlist</button>
      </div>
      <div className="metrics" style={{ marginTop: 8 }}>
        <button className="btn ghost" onClick={() => navigate("STAKING_CALC")}>💎 Staking Calculator</button>
        <button className="btn ghost" onClick={() => navigate("PROPOSAL_SIM")}>🎲 Proposal Simulator</button>
      </div>
      <div className="metrics" style={{ marginTop: 8 }}>
        <button className="btn ghost" onClick={() => navigate("DELEGATION")}>🗳️ Delegation Explorer</button>
        <button className="btn ghost" onClick={() => navigate("GOVERNANCE")}>🏛️ Governance</button>
      </div>
    </div>
  );
}
