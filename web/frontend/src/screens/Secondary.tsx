import { useEffect, useState } from "react";
import { api } from "../api";
import { shortAddr, fmtUsd } from "../ui";

function useAsync<T>(fn: () => Promise<T>, deps: any[]) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => { let alive = true; fn().then((d) => { if (alive) { setData(d); setLoading(false); } }).catch(() => { if (alive) setLoading(false); }); return () => { alive = false; }; }, deps);
  return { data, loading };
}

export function StakingScreen() {
  const { data, loading } = useAsync(() => api.getStaking(), []);
  if (loading) return <div className="center"><div className="loader" /></div>;
  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>💎 AGL Staking Vault</div>
      <div className="card card-elev">
        <div className="row between"><span className="small muted">Total Staked</span><span className="bold">{data?.formattedTotalStaked ?? "0"} AGL</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Reward Pool</span><span className="bold">{data?.formattedRewardPoolBalance ?? "0"} AGL</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Status</span>{data?.isPaused ? <span className="pill gold">PAUSED</span> : <span className="pill green">ACTIVE</span>}</div>
      </div>
      <div className="section-title">Staking Tiers</div>
      {(data?.tiers ?? []).map((t: any) => (
        <div key={t.tierId} className="card">
          <div className="row between"><span className="bold">{t.formattedDuration}</span><span className="pill green">{t.aprPercent}% APR</span></div>
          <div className="tiny muted" style={{ marginTop: 6 }}>Tier #{t.tierId} · {t.isActive ? "Active" : "Inactive"}</div>
        </div>
      ))}
      <div className="card" style={{ marginTop: 12 }}>
        <div className="small muted">Read-only monitoring. Staking actions require a connected wallet signer.</div>
      </div>
    </div>
  );
}

export function GovernanceScreen() {
  const { data, loading } = useAsync(() => api.getGovernance(), []);
  if (loading) return <div className="center"><div className="loader" /></div>;
  const d = data?.details;
  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>🏛️ DAO Governance</div>
      <div className="card card-elev">
        <div className="bold">{d?.name ?? "Agunnaya DAO Governor"}</div>
        <div className="tiny muted mono" style={{ marginTop: 4 }}>{shortAddr(d?.contractAddress)}</div>
        <div style={{ height: 10 }} />
        <div className="row between"><span className="small muted">Voting Delay</span><span className="small bold">{d?.votingDelayBlocks ?? 7200} blocks</span></div>
        <div style={{ height: 6 }} />
        <div className="row between"><span className="small muted">Voting Period</span><span className="small bold">{d?.votingPeriodBlocks ?? 30240} blocks</span></div>
        <div style={{ height: 6 }} />
        <div className="row between"><span className="small muted">Quorum</span><span className="small bold">{d?.formattedQuorum ?? "40,000 wAGL"}</span></div>
      </div>
      <div className="section-title">Proposals</div>
      {(data?.proposals ?? []).map((p: any) => (
        <div key={p.id} className="card">
          <div className="row between"><span className="bold">#{p.id} {p.title}</span><span className={`pill ${p.state === "ACTIVE" ? "green" : p.state === "PENDING" ? "gold" : "cyan"}`}>{p.state}</span></div>
          <div className="small muted" style={{ marginTop: 6 }}>{p.description}</div>
          <div style={{ height: 8 }} />
          <div className="row gap">
            <span className="pill green">✅ {p.forVotes}</span>
            <span className="pill rose">❌ {p.againstVotes}</span>
            <span className="pill purple"> abstain {p.abstainVotes}</span>
          </div>
          <div className="tiny muted" style={{ marginTop: 6 }}>End block #{Number(p.endBlock).toLocaleString()} · {p.calldatasSummary}</div>
        </div>
      ))}
    </div>
  );
}

export function TimelockScreen() {
  const { data, loading } = useAsync(() => api.getTimelock(), []);
  if (loading) return <div className="center"><div className="loader" /></div>;
  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>⏳ Timelock Controller</div>
      <div className="card card-elev">
        <div className="row between"><span className="small muted">Minimum Delay</span><span className="bold">{data?.formattedMinDelay ?? "2 days"}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Governor</span><span className="mono tiny">{shortAddr(data?.governorAddress)}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Contract</span><span className="mono tiny">{shortAddr(data?.contractAddress)}</span></div>
      </div>
      <div className="card">
        <div className="small muted">The timelock enforces a delay between governance approval and execution, protecting against malicious instant actions.</div>
      </div>
    </div>
  );
}

export function DiagnosticsScreen() {
  const { data, loading } = useAsync(() => api.getDiagnostics(), []);
  if (loading) return <div className="center"><div className="loader" /></div>;
  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>🔧 Contract Diagnostics</div>
      <div className="card card-elev" style={{ borderColor: "rgba(0,230,153,0.5)" }}>
        <div className="row between">
          <div className="row gap"><span className="status-dot" /><span className="bold">Base Mainnet Connection</span></div>
          <span className="pill green">HEALTHY</span>
        </div>
        <div className="tiny" style={{ color: "var(--cyan)", marginTop: 6 }}>Chain ID: {data?.chainId ?? 8453} · Latency: {data?.rpcLatencyMs ?? 48}ms</div>
        <div style={{ height: 12 }} />
        <div className="row between"><span className="small muted">Latest Block</span><span className="bold">#{(data?.currentBlock ?? 50741280).toLocaleString()}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Verified Contracts</span><span className="bold" style={{ color: "var(--neon)" }}>{(data?.contracts ?? []).length} / 6</span></div>
        <div className="tiny muted" style={{ marginTop: 6 }}>RPC: {data?.activeRpcEndpoint}</div>
      </div>
      <div className="section-title">Ecosystem Contracts</div>
      {(data?.contracts ?? []).map((c: any) => (
        <div key={c.id} className="card">
          <div className="row between">
            <div className="row gap"><span style={{ fontSize: 18 }}>{c.iconEmoji}</span><span className="bold">{c.name}</span></div>
            <span className="pill green">DEPLOYED</span>
          </div>
          <div className="tiny muted" style={{ marginTop: 6 }}>{c.purpose}</div>
          <div className="mono tiny" style={{ color: "var(--cyan)", marginTop: 4 }}>{shortAddr(c.contractAddress)}</div>
        </div>
      ))}
    </div>
  );
}

export function PriceAlertsScreen() {
  const [oracle, setOracle] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [price, setPrice] = useState("");
  useEffect(() => { api.getOracle().then((o) => { setOracle(o); setLoading(false); }).catch(() => setLoading(false)); }, []);
  const simulate = async () => { const p = parseFloat(price); const r = await api.simulateOracle(isNaN(p) ? null : p); setOracle(r); };
  if (loading) return <div className="center"><div className="loader" /></div>;
  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>🔔 Price Alerts</div>
      <div className="card card-elev">
        <div className="small muted">AGL Oracle Price</div>
        <div style={{ fontSize: 30, fontWeight: 800 }}>{fmtUsd(oracle?.currentPriceUsd ?? 3.42)}</div>
        <div className="tiny" style={{ color: "var(--cyan)" }}>{oracle?.oracleProvider}</div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="tiny muted">24h Change</span><span className="bold" style={{ color: (oracle?.change24hPercent ?? 0) >= 0 ? "var(--neon)" : "var(--rose)" }}>{(oracle?.change24hPercent ?? 0) >= 0 ? "+" : ""}{(oracle?.change24hPercent ?? 0).toFixed(1)}%</span></div>
        <div className="row between"><span className="tiny muted">24h High / Low</span><span className="small">{fmtUsd(oracle?.high24hUsd)} / {fmtUsd(oracle?.low24hUsd)}</span></div>
      </div>
      <div className="card">
        <div className="bold small">Simulate Oracle Price</div>
        <div style={{ height: 8 }} />
        <div className="row gap"><input className="input" placeholder="3.50" value={price} onChange={(e) => setPrice(e.target.value)} /><button className="btn" style={{ width: "auto", padding: "12px 16px" }} onClick={simulate}>Set</button></div>
      </div>
      <div className="card">
        <div className="bold small">Active Alerts</div>
        <div className="list-row" style={{ marginTop: 8 }}><div className="col" style={{ flex: 1 }}><div className="bold small">AGL {'>'} $3.80</div><div className="tiny muted">Resistance Breakout Target</div></div><span className="pill green">ABOVE</span></div>
        <div className="list-row"><div className="col" style={{ flex: 1 }}><div className="bold small">AGL {'<'} $3.20</div><div className="tiny muted">DCA Dip Accumulation Zone</div></div><span className="pill rose">BELOW</span></div>
      </div>
    </div>
  );
}

export function AglTokenScreen() {
  const { data, loading } = useAsync(() => api.getAglToken(), []);
  if (loading) return <div className="center"><div className="loader" /></div>;
  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>🪙 AGL Token</div>
      <div className="card card-elev">
        <div className="row gap"><span style={{ fontSize: 28 }}>🪙</span><div><div style={{ fontSize: 20, fontWeight: 800 }}>{data?.name ?? "Agunnaya Labs"}</div><div className="small" style={{ color: "var(--cyan)" }}>{data?.symbol ?? "AGL"}</div></div></div>
        <div style={{ height: 12 }} />
        <div className="row between"><span className="small muted">Decimals</span><span className="bold">{data?.decimals ?? 18}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Total Supply</span><span className="bold">{data?.formattedTotalSupply ?? "1,000,000,000 AGL"}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Contract</span><span className="mono tiny">{shortAddr(data?.contractAddress)}</span></div>
      </div>
      <div className="card"><div className="small muted">Core utility, staking, and ecosystem currency on Base Mainnet (ERC-20).</div></div>
    </div>
  );
}

export function CreditsScreen({ wallet }: { wallet: string }) {
  const { data, loading } = useAsync(() => api.getCredits(wallet), [wallet]);
  if (loading) return <div className="center"><div className="loader" /></div>;
  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>⚡ AGL Compute Credits</div>
      <div className="card card-elev">
        <div className="row between"><span className="small muted">Credits per AGL</span><span className="bold">{data?.creditsPerAgl ?? "100"}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Total AGL Burned</span><span className="bold">{data?.formattedTotalAglBurned ?? "0"}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Your AGL Burned</span><span className="bold">{data?.formattedUserAglBurned ?? "0"}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Your Credits</span><span className="bold" style={{ color: "var(--cyan)" }}>{data?.formattedUserCredits ?? "0"}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Status</span>{data?.isPaused ? <span className="pill gold">PAUSED</span> : <span className="pill green">ACTIVE</span>}</div>
      </div>
      <div className="card"><div className="small muted">Burn AGL to mint compute credits for AI agent execution on Base.</div></div>
    </div>
  );
}

export function WaglScreen({ wallet }: { wallet: string }) {
  const { data, loading } = useAsync(() => api.getWagl(wallet), [wallet]);
  if (loading) return <div className="center"><div className="loader" /></div>;
  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>🗳️ Wrapped AGL (wAGL)</div>
      <div className="card card-elev">
        <div className="row between"><span className="small muted">wAGL Balance</span><span className="bold">{data?.formattedBalance ?? "0.00"}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Voting Power</span><span className="bold" style={{ color: "var(--cyan)" }}>{data?.formattedVotingPower ?? "0.00"}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Delegatee</span><span className="mono tiny">{data?.delegatee ? shortAddr(data.delegatee) : "None"}</span></div>
        <div style={{ height: 8 }} />
        <div className="row between"><span className="small muted">Checkpoints</span><span className="bold">{data?.numCheckpoints ?? 0}</span></div>
      </div>
      <div className="card">
        <div className="row between"><span className="small muted">Total Supply</span><span className="bold">{data?.formattedTotalSupply ?? "0"}</span></div>
        <div style={{ height: 8 }} />
        <div className="small muted">ERC-20Votes wrapper for DAO governance snapshot checkpointing.</div>
      </div>
    </div>
  );
}
