import { useState } from "react";
import { api } from "../api";
import { fmtUsd } from "../ui";

export function StakingCalcScreen() {
  const [amount, setAmount] = useState("1000");
  const [apr, setApr] = useState("18.5");
  const [duration, setDuration] = useState("365");
  const [compound, setCompound] = useState(true);
  const [result, setResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const calc = async () => {
    setLoading(true);
    setResult(await api.stakingCalculate(
      parseFloat(amount) || 0, parseFloat(apr) || 0, parseInt(duration) || 0, compound
    ).catch(() => null));
    setLoading(false);
  };

  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>💎 Staking Calculator & Optimizer</div>
      <div className="card card-elev">
        <div className="bold small" style={{ marginBottom: 10 }}>Configure Your Stake</div>
        <div className="small muted" style={{ marginBottom: 4 }}>Stake Amount (AGL)</div>
        <input className="input" type="number" value={amount} onChange={(e) => setAmount(e.target.value)} />
        <div style={{ height: 10 }} />
        <div className="small muted" style={{ marginBottom: 4 }}>APR (%)</div>
        <input className="input" type="number" value={apr} onChange={(e) => setApr(e.target.value)} />
        <div style={{ height: 10 }} />
        <div className="small muted" style={{ marginBottom: 4 }}>Duration (days)</div>
        <input className="input" type="number" value={duration} onChange={(e) => setDuration(e.target.value)} />
        <div style={{ height: 10 }} />
        <label className="row gap" style={{ cursor: "pointer" }}>
          <input type="checkbox" checked={compound} onChange={(e) => setCompound(e.target.checked)} />
          <span className="small">🔄 Auto-compound rewards daily</span>
        </label>
        <div style={{ height: 12 }} />
        <button className="btn" disabled={loading} onClick={calc}>
          {loading ? <><span className="loader" style={{ width: 16, height: 16 }} /> Calculating…</> : "📊 Calculate Projection"}
        </button>
      </div>

      {result && (
        <>
          <div className="card card-elev" style={{ borderColor: "rgba(0,230,153,0.4)" }}>
            <div className="small muted">Projected Returns</div>
            <div style={{ fontSize: 28, fontWeight: 800, color: "var(--neon)" }}>
              +{result.totalRewards?.toFixed(2)} AGL
            </div>
            <div className="small muted">over {result.durationDays} days at {result.aprPercent}% APR</div>
            <div style={{ height: 12 }} />
            <div className="metrics">
              <div className="card" style={{ textAlign: "center" }}>
                <div style={{ fontSize: 18, fontWeight: 800 }}>{result.finalAmount?.toFixed(2)}</div>
                <div className="tiny muted">Final Balance</div>
              </div>
              <div className="card" style={{ textAlign: "center" }}>
                <div style={{ fontSize: 18, fontWeight: 800 }}>{result.dailyRewards?.toFixed(4)}</div>
                <div className="tiny muted">Daily Rewards</div>
              </div>
            </div>
          </div>

          {result.optimalTier && (
            <div className="card" style={{ borderColor: "rgba(0,212,255,0.4)" }}>
              <div className="row gap"><span style={{ fontSize: 18 }}>🎯</span><span className="bold">AI-Recommended Tier</span></div>
              <div className="small" style={{ marginTop: 6 }}>
                {result.optimalTier.label} at {result.optimalTier.aprPercent}% APR yields the highest total rewards for your stake.
              </div>
            </div>
          )}

          <div className="section-title">All Tier Projections</div>
          {(result.projections || []).map((t: any) => (
            <div key={t.tierId} className="card">
              <div className="row between">
                <span className="bold">{t.label}</span>
                <span className="pill green">{t.aprPercent}% APR</span>
              </div>
              <div style={{ height: 8 }} />
              <div className="row between">
                <span className="small muted">Rewards</span>
                <span className="bold" style={{ color: "var(--neon)" }}>+{t.rewards?.toFixed(2)} AGL</span>
              </div>
              <div className="row between" style={{ marginTop: 4 }}>
                <span className="small muted">Final</span>
                <span className="bold">{t.final?.toFixed(2)} AGL</span>
              </div>
            </div>
          ))}
        </>
      )}
    </div>
  );
}
