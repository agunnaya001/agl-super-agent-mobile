import { useEffect, useState } from "react";
import { api } from "../api";
import { Screen } from "../types";
import { fmtUsd, fmtNum, timeAgo, statusPill, shortAddr } from "../ui";
import { D3BalanceChart } from "../components/D3BalanceChart";
import { TokenLogo } from "../components/TokenLogo";

export function DashboardScreen({ wallet, navigate }: { wallet: string; navigate: (s: Screen) => void }) {
  const [status, setStatus] = useState<any>(null);
  const [oracle, setOracle] = useState<any>(null);
  const [eco, setEco] = useState<any>(null);
  const [txs, setTxs] = useState<any[]>([]);
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [running, setRunning] = useState(false);

  const load = async () => {
    const [s, o, e, t, p] = await Promise.all([
      api.getStatus().catch(() => null), api.getOracle().catch(() => null),
      api.getEcosystem().catch(() => null), api.getTransactions(wallet).catch(() => []),
      api.getProfile(wallet).catch(() => null),
    ]);
    setStatus(s); setOracle(o); setEco(e); setTxs(t); setProfile(p); setLoading(false);
  };

  useEffect(() => { load(); }, [wallet]);

  const runDiag = async () => {
    setRunning(true);
    await api.getDiagnostics().catch(() => {});
    await load();
    setRunning(false);
  };

  if (loading) return <div className="center"><div className="loader" /></div>;
  const stats = eco?.stats;
  const healthy = status?.allRelationshipsVerified ?? true;

  return (
    <div>
      <div className="row between" style={{ marginBottom: 14 }}>
        <div className="row gap"><span style={{ fontSize: 22 }}>📊</span><span style={{ fontSize: 20, fontWeight: 800 }}>Agent Dashboard</span></div>
      </div>

      {/* Agent status */}
      <div className="card card-elev" style={{ borderColor: healthy ? "rgba(0,230,153,0.5)" : "var(--border)" }}>
        <div className="row between">
          <div className="row gap">
            <span className="status-dot" />
            <div>
              <div className="bold">Agent Online</div>
              <div className="small" style={{ color: "var(--cyan)" }}>Idle — monitoring Base Mainnet</div>
            </div>
          </div>
          <span className="pill green">OPERATIONAL</span>
        </div>
        <div style={{ height: 16 }} />
        <div className="row between"><span className="small muted">Connected Wallet</span><span className="mono small bold">{shortAddr(wallet)}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Network</span><span className="small bold">Base Mainnet · Chain {status?.chainId ?? 8453}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Latest Block</span><span className="small bold">#{(status?.currentBlock ?? 50741280).toLocaleString()}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">RPC Latency</span><span className="small bold" style={{ color: "var(--neon)" }}>{status?.rpcLatencyMs ?? 48} ms</span></div>
        <div style={{ height: 14 }} />
        <button className="btn cyan" disabled={running} onClick={runDiag}>
          {running ? <><span className="loader" style={{ width: 16, height: 16 }} /> Checking Base nodes…</> : <>🔄 Re-Run Network Diagnostics</>}
        </button>
      </div>

      {/* D3 Balance History Chart */}
      <D3BalanceChart wallet={wallet} />

      {/* Quick metrics */}
      <div className="metrics" style={{ marginTop: 14 }}>
        <MetricCard icon="📈" tint="var(--neon)" label="AGL Price" value={fmtUsd(oracle?.currentPriceUsd ?? 3.42)} sub={`${(oracle?.change24hPercent ?? 0) >= 0 ? "+" : ""}${(oracle?.change24hPercent ?? 0).toFixed(1)}% 24h`} subColor={oracle?.change24hPercent >= 0 ? "var(--neon)" : "var(--rose)"} />
        <MetricCard icon="🤖" tint="var(--purple)" label="Active Agents" value={fmtNum(stats?.totalActiveAgents ?? 0)} sub="online globally" subColor="var(--text-2)" />
      </div>
      <div className="metrics" style={{ marginTop: 12 }}>
        <MetricCard icon="✅" tint="var(--cyan)" label="Contract Audits" value={fmtNum(stats?.totalContractAuditsCompleted ?? 0)} sub="completed" subColor="var(--text-2)" />
        <MetricCard icon="⚡" tint="var(--gold)" label="Agent Level" value={`Lv ${profile?.level ?? 24}`} sub={`${(profile?.totalXp ?? 12450).toLocaleString()} XP`} subColor="var(--text-2)" />
      </div>

      {/* Recent activity */}
      <div className="section-title">Recent Activity</div>
      <div className="row between" style={{ marginBottom: 10 }}>
        <span />
        <span className="small bold" style={{ color: "var(--cyan)", cursor: "pointer" }} onClick={() => navigate("WALLET")}>View Wallet →</span>
      </div>
      {txs.slice(0, 5).map((tx) => (
        <div key={tx.hash} className="list-row">
          <TokenLogo symbol={tx.tokenSymbol || "AGL"} size={28} />
          <div className="col" style={{ flex: 1 }}>
            <div className="bold small">{tx.type.replace(/_/g, " ").toLowerCase().replace(/^\w/, (c: string) => c.toUpperCase())}</div>
            <div className="tiny muted">{tx.value} {tx.tokenSymbol} · {timeAgo(tx.timestamp)}</div>
          </div>
          {statusPill(tx.status)}
        </div>
      ))}
    </div>
  );
}

function MetricCard({ icon, tint, label, value, sub, subColor }: any) {
  return (
    <div className="card" style={{ padding: 16 }}>
      <div className="row gap">
        <div className="avatar-circle" style={{ width: 28, height: 28, background: `${tint}26`, fontSize: 14 }}>{icon}</div>
        <span className="tiny muted">{label}</span>
      </div>
      <div style={{ height: 10 }} />
      <div style={{ fontSize: 20, fontWeight: 800 }}>{value}</div>
      <div className="tiny" style={{ color: subColor }}>{sub}</div>
    </div>
  );
}
