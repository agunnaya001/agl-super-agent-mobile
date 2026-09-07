import { useEffect, useState } from "react";
import { api } from "../api";
import { shortAddr } from "../ui";

export function DelegationScreen({ wallet }: { wallet: string }) {
  const [data, setData] = useState<any>(null);
  const [myDelegation, setMyDelegation] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [checkAddr, setCheckAddr] = useState("");
  const [checkResult, setCheckResult] = useState<any>(null);

  useEffect(() => {
    (async () => {
      const [d, my] = await Promise.all([
        api.getDelegations().catch(() => null),
        api.getDelegation(wallet).catch(() => null),
      ]);
      setData(d); setMyDelegation(my); setLoading(false);
    })();
  }, [wallet]);

  const checkAddress = async () => {
    if (!/^0x[a-fA-F0-9]{40}$/.test(checkAddr.trim())) return;
    setCheckResult(await api.getDelegation(checkAddr.trim()).catch(() => null));
  };

  if (loading) return <div className="center"><div className="loader" /></div>;

  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>🗳️ Delegation Explorer</div>

      {/* My delegation */}
      <div className="card card-elev">
        <div className="bold small" style={{ marginBottom: 8 }}>Your Delegation Status</div>
        <div className="row between">
          <span className="small muted">Voting Power</span>
          <span className="bold" style={{ color: "var(--cyan)" }}>{myDelegation?.votingPower ?? "0"} wAGL</span>
        </div>
        <div style={{ height: 6 }} />
        <div className="row between">
          <span className="small muted">Delegated To</span>
          {myDelegation?.delegatee
            ? <span className="mono tiny">{shortAddr(myDelegation.delegatee)}</span>
            : <span className="small" style={{ color: "var(--gold)" }}>{myDelegation?.isSelfDelegated ? "Self-delegated" : "Not delegated"}</span>}
        </div>
        <div style={{ height: 6 }} />
        <div className="row between">
          <span className="small muted">Checkpoints</span>
          <span className="bold">{myDelegation?.numCheckpoints ?? 0}</span>
        </div>
      </div>

      {/* Check any address */}
      <div className="card">
        <div className="bold small" style={{ marginBottom: 8 }}>Check Any Address</div>
        <div className="row gap">
          <input className="input" placeholder="0x… address" value={checkAddr} onChange={(e) => setCheckAddr(e.target.value)} />
          <button className="btn" style={{ width: "auto", padding: "12px 16px" }} onClick={checkAddress}>Check</button>
        </div>
        {checkResult && (
          <div style={{ marginTop: 10 }}>
            <div className="row between"><span className="small muted">Voting Power</span><span className="bold">{checkResult.votingPower} wAGL</span></div>
            <div className="row between" style={{ marginTop: 4 }}><span className="small muted">Delegated To</span><span className="mono tiny">{checkResult.delegatee ? shortAddr(checkResult.delegatee) : "Self/None"}</span></div>
          </div>
        )}
      </div>

      {/* Delegation graph */}
      <div className="section-title">Delegation Graph</div>
      {(data?.delegations || []).map((d: any, i: number) => (
        <div key={i} className="card">
          <div className="row gap" style={{ alignItems: "center" }}>
            <div className="col" style={{ flex: 1 }}>
              <div className="bold small">{d.label}</div>
              <div className="tiny muted mono">{shortAddr(d.delegator)} → {shortAddr(d.delegatee)}</div>
            </div>
            <span className="pill cyan">{d.votingPower} wAGL</span>
          </div>
        </div>
      ))}

      {/* Top delegates */}
      <div className="section-title">Top Delegates by Voting Power</div>
      {(data?.topDelegates || []).map((d: any, i: number) => (
        <div key={i} className="list-row">
          <div className="avatar-circle" style={{ background: "var(--surface)" }}>#{i + 1}</div>
          <div className="col" style={{ flex: 1 }}>
            <div className="bold small">{d.label}</div>
            <div className="tiny muted mono">{shortAddr(d.address)}</div>
          </div>
          <div style={{ textAlign: "right" }}>
            <div className="bold">{d.totalDelegated}</div>
            <div className="tiny muted">{d.pctOfSupply}% supply</div>
          </div>
        </div>
      ))}
    </div>
  );
}
