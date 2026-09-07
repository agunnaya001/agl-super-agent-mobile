import { useState, useEffect } from "react";
import { api } from "../api";

export function ProposalSimScreen({ wallet }: { wallet: string }) {
  const [gov, setGov] = useState<any>(null);
  const [selectedProposal, setSelectedProposal] = useState<number>(0);
  const [userPower, setUserPower] = useState("50000");
  const [support, setSupport] = useState<"FOR" | "AGAINST" | "ABSTAIN">("FOR");
  const [result, setResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => { api.getGovernance().then(setGov).catch(() => {}); }, []);

  const proposals = gov?.proposals || [];
  const proposal = proposals[selectedProposal];
  const quorum = 40000;

  const simulate = async () => {
    if (!proposal) return;
    setLoading(true);
    const forNum = parseFloat(proposal.forVotes.replace(/,/g, "")) || 0;
    const againstNum = parseFloat(proposal.againstVotes.replace(/,/g, "")) || 0;
    const abstainNum = parseFloat(proposal.abstainVotes.replace(/,/g, "")) || 0;
    setResult(await api.simulateProposal({
      forVotes: forNum, againstVotes: againstNum, abstainVotes: abstainNum,
      quorum, userVotingPower: parseFloat(userPower) || 0, userSupport: support,
    }).catch(() => null));
    setLoading(false);
  };

  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>🏛️ Proposal Vote Simulator</div>
      <div className="card card-elev">
        <div className="bold small" style={{ marginBottom: 10 }}>Select a Proposal</div>
        {proposals.length === 0 ? (
          <div className="small muted">Loading proposals…</div>
        ) : (
          <select className="input" value={selectedProposal} onChange={(e) => { setSelectedProposal(parseInt(e.target.value)); setResult(null); }}>
            {proposals.map((p: any, i: number) => (
              <option key={p.id} value={i}>#{p.id} {p.title}</option>
            ))}
          </select>
        )}
      </div>

      {proposal && (
        <div className="card">
          <div className="row between">
            <span className="bold">#{proposal.id} {proposal.title}</span>
            <span className={`pill ${proposal.state === "ACTIVE" ? "green" : "gold"}`}>{proposal.state}</span>
          </div>
          <div className="small muted" style={{ marginTop: 6 }}>{proposal.description}</div>
          <div style={{ height: 8 }} />
          <div className="row gap">
            <span className="pill green">✅ {proposal.forVotes}</span>
            <span className="pill rose">❌ {proposal.againstVotes}</span>
            <span className="pill purple">🗳️ {proposal.abstainVotes}</span>
          </div>
        </div>
      )}

      <div className="card">
        <div className="bold small" style={{ marginBottom: 10 }}>Your Vote Simulation</div>
        <div className="small muted" style={{ marginBottom: 4 }}>Your wAGL Voting Power</div>
        <input className="input" type="number" value={userPower} onChange={(e) => setUserPower(e.target.value)} />
        <div style={{ height: 10 }} />
        <div className="small muted" style={{ marginBottom: 6 }}>Vote Direction</div>
        <div className="row gap">
          {(["FOR", "AGAINST", "ABSTAIN"] as const).map((s) => (
            <button key={s} className={`tab ${support === s ? "active" : ""}`} onClick={() => setSupport(s)}>
              {s === "FOR" ? "✅ For" : s === "AGAINST" ? "❌ Against" : "🗳️ Abstain"}
            </button>
          ))}
        </div>
        <div style={{ height: 12 }} />
        <button className="btn" disabled={loading} onClick={simulate}>
          {loading ? <><span className="loader" style={{ width: 16, height: 16 }} /> Simulating…</> : "🎲 Simulate Vote Impact"}
        </button>
      </div>

      {result && (
        <div className="card card-elev" style={{ borderColor: result.simulated.wouldPass ? "rgba(0,230,153,0.4)" : "rgba(255,51,102,0.4)" }}>
          <div className="row between">
            <span className="bold">Simulation Result</span>
            <span className={`pill ${result.simulated.wouldPass ? "green" : "rose"}`}>{result.simulated.outcome}</span>
          </div>
          <div style={{ height: 10 }} />
          <div className="metrics">
            <div className="card" style={{ textAlign: "center" }}>
              <div style={{ fontSize: 16, fontWeight: 800, color: "var(--neon)" }}>{result.simulated.forPct?.toFixed(1)}%</div>
              <div className="tiny muted">For (with your vote)</div>
            </div>
            <div className="card" style={{ textAlign: "center" }}>
              <div style={{ fontSize: 16, fontWeight: 800 }}>{result.simulated.totalCast?.toLocaleString()}</div>
              <div className="tiny muted">Total Votes Cast</div>
            </div>
          </div>
          <div style={{ height: 8 }} />
          {result.simulated.meetsQuorum ? (
            <div className="small" style={{ color: "var(--neon)" }}>✅ Quorum met ({result.quorum.toLocaleString()} wAGL required)</div>
          ) : (
            <div className="small" style={{ color: "var(--rose)" }}>
              ❌ Quorum not met — {result.simulated.quorumNeeded?.toLocaleString()} more wAGL needed
            </div>
          )}
          <div className="tiny muted" style={{ marginTop: 6 }}>
            Your {parseFloat(userPower).toLocaleString()} wAGL {support === "FOR" ? "supports" : support === "AGAINST" ? "opposes" : "abstains on"} this proposal.
          </div>
        </div>
      )}
    </div>
  );
}
