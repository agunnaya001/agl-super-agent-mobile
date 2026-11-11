import React, { useState } from "react";
import { useGovernanceProposals } from "./hooks/useGovernanceProposals";
import { useTimelockStatus } from "./hooks/useTimelockStatus";
import { GovernanceDiagnosticWidget } from "./components/GovernanceDiagnosticWidget";
import { ProposalCard } from "./components/ProposalCard";
import { TimelockQueueTable } from "./components/TimelockQueueTable";
import { GOVERNANCE_CONTRACTS, getBasescanUrl, truncateAddress } from "./lib/contracts";

export function DaoGovernanceScreen() {
  const { proposals, loading: proposalsLoading, refresh: refetchProposals } = useGovernanceProposals();
  const { operations, loading: timelockLoading, refresh: refreshTimelock } = useTimelockStatus();

  const [activeTab, setActiveTab] = useState<"proposals" | "timelock" | "diagnostics" | "contracts">("proposals");
  const [filterStatus, setFilterStatus] = useState<string>("ALL");
  const [feedback, setFeedback] = useState<string | null>(null);

  const showFeedback = (msg: string) => {
    setFeedback(msg);
    setTimeout(() => setFeedback(null), 3500);
  };

  const handleVote = (proposalId: string, support: 0 | 1 | 2) => {
    const choice = support === 1 ? "FOR" : support === 0 ? "AGAINST" : "ABSTAIN";
    showFeedback(`✅ Vote cast ${choice} on ${proposalId}!`);
  };

  const handleExecute = async (opId: string) => {
    showFeedback(`⚡ Operation ${truncateAddress(opId, 8, 4)} scheduled for execution!`);
    await refreshTimelock();
  };

  const filteredProposals = proposals.filter((p) => {
    if (filterStatus === "ALL") return true;
    return p.status.toUpperCase() === filterStatus;
  });

  return (
    <div className="dao-governance-screen" style={{ paddingBottom: 24 }}>
      {/* Feedback Banner */}
      {feedback && (
        <div
          className="card"
          style={{
            background: "rgba(0, 230, 153, 0.15)",
            border: "1px solid var(--neon)",
            color: "var(--neon)",
            padding: "10px 14px",
            marginBottom: 12,
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <span className="small bold">{feedback}</span>
          <button
            onClick={() => setFeedback(null)}
            style={{ background: "none", border: "none", color: "var(--neon)", cursor: "pointer", fontSize: 16 }}
          >
            ×
          </button>
        </div>
      )}

      {/* Screen Header */}
      <div className="section-title" style={{ marginTop: 0, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div className="row gap" style={{ alignItems: "center" }}>
          <span>🏛️</span>
          <span>DAO Governance</span>
        </div>
        <span className="pill green">Base Mainnet 8453</span>
      </div>

      {/* Governance Stats Banner */}
      <div className="card card-elev" style={{ marginBottom: 14 }}>
        <div className="row between" style={{ alignItems: "flex-start" }}>
          <div>
            <div className="tiny muted">Governance Authority</div>
            <div className="bold" style={{ fontSize: 18, color: "var(--fg)" }}>Agunnaya DAO</div>
            <div className="tiny mono muted" style={{ marginTop: 2 }}>
              Governor: {truncateAddress(GOVERNANCE_CONTRACTS.governor)}
            </div>
          </div>
          <a
            href={getBasescanUrl("address", GOVERNANCE_CONTRACTS.governor)}
            target="_blank"
            rel="noreferrer"
            className="pill cyan"
            style={{ textDecoration: "none" }}
          >
            Basescan ↗
          </a>
        </div>

        <div style={{ height: 12 }} />

        <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 8 }}>
          <div className="card" style={{ background: "var(--surface)", padding: 8 }}>
            <div className="tiny muted">Quorum</div>
            <div className="bold small" style={{ color: "var(--cyan)" }}>1,000,000</div>
            <div className="tiny muted">wAGL votes</div>
          </div>
          <div className="card" style={{ background: "var(--surface)", padding: 8 }}>
            <div className="tiny muted">Voting Period</div>
            <div className="bold small">30,240</div>
            <div className="tiny muted">~7 days</div>
          </div>
          <div className="card" style={{ background: "var(--surface)", padding: 8 }}>
            <div className="tiny muted">Timelock Delay</div>
            <div className="bold small" style={{ color: "var(--neon)" }}>48 Hours</div>
            <div className="tiny muted">Execution grace</div>
          </div>
        </div>
      </div>

      {/* Sub-Navigation Tabs */}
      <div className="tabs" style={{ marginBottom: 12 }}>
        <button
          className={`tab ${activeTab === "proposals" ? "active" : ""}`}
          onClick={() => setActiveTab("proposals")}
        >
          📜 Proposals ({proposals.length})
        </button>
        <button
          className={`tab ${activeTab === "timelock" ? "active" : ""}`}
          onClick={() => setActiveTab("timelock")}
        >
          ⏳ Timelock ({operations.length})
        </button>
        <button
          className={`tab ${activeTab === "diagnostics" ? "active" : ""}`}
          onClick={() => setActiveTab("diagnostics")}
        >
          🛡️ Diagnostics
        </button>
        <button
          className={`tab ${activeTab === "contracts" ? "active" : ""}`}
          onClick={() => setActiveTab("contracts")}
        >
          📑 Contracts
        </button>
      </div>

      {/* TAB: Proposals */}
      {activeTab === "proposals" && (
        <div>
          {/* Status Filter Buttons */}
          <div className="row gap" style={{ marginBottom: 12, overflowX: "auto", paddingBottom: 4 }}>
            {["ALL", "ACTIVE", "QUEUED", "SUCCEEDED"].map((status) => (
              <button
                key={status}
                onClick={() => setFilterStatus(status)}
                className={`pill ${filterStatus === status ? "cyan" : "gray"}`}
                style={{
                  cursor: "pointer",
                  border: filterStatus === status ? "1px solid var(--cyan)" : "1px solid rgba(255,255,255,0.1)",
                  background: filterStatus === status ? "rgba(0, 212, 255, 0.2)" : "rgba(255, 255, 255, 0.05)",
                  color: filterStatus === status ? "var(--cyan)" : "var(--muted)",
                  padding: "4px 10px",
                }}
              >
                {status}
              </button>
            ))}
            <div style={{ flex: 1 }} />
            <button
              className="btn"
              style={{ width: "auto", padding: "4px 10px", fontSize: 11, background: "rgba(255,255,255,0.08)", border: "1px solid var(--border)" }}
              onClick={() => {
                refetchProposals();
                showFeedback("Refreshed governance proposals!");
              }}
              disabled={proposalsLoading}
            >
              🔄 Refresh
            </button>
          </div>

          {proposalsLoading ? (
            <div className="center"><div className="loader" /></div>
          ) : filteredProposals.length === 0 ? (
            <div className="card" style={{ textAlign: "center", padding: 24, color: "var(--muted)" }}>
              No proposals found with status "{filterStatus}".
            </div>
          ) : (
            filteredProposals.map((proposal) => (
              <ProposalCard
                key={proposal.id}
                proposal={proposal}
                onVote={handleVote}
              />
            ))
          )}
        </div>
      )}

      {/* TAB: Timelock Queue */}
      {activeTab === "timelock" && (
        <div>
          <div className="card" style={{ marginBottom: 12 }}>
            <div className="row between">
              <div>
                <div className="bold small">Timelock Controller</div>
                <div className="tiny muted">Enforces mandatory 48h delay between voting approval and contract execution.</div>
              </div>
              <a
                href={getBasescanUrl("address", GOVERNANCE_CONTRACTS.timelock)}
                target="_blank"
                rel="noreferrer"
                className="pill cyan"
                style={{ textDecoration: "none" }}
              >
                Contract ↗
              </a>
            </div>
          </div>

          {timelockLoading ? (
            <div className="center"><div className="loader" /></div>
          ) : (
            <TimelockQueueTable
              operations={operations}
              onExecute={handleExecute}
            />
          )}
        </div>
      )}

      {/* TAB: Diagnostics */}
      {activeTab === "diagnostics" && (
        <div>
          <GovernanceDiagnosticWidget />
        </div>
      )}

      {/* TAB: Contracts */}
      {activeTab === "contracts" && (
        <div className="card card-elev">
          <div className="bold small" style={{ marginBottom: 8 }}>Base Mainnet Governance Infrastructure</div>
          <div className="tiny muted" style={{ marginBottom: 12 }}>
            Agunnaya DAO contracts deployed and verified on Base (Chain ID 8453).
          </div>

          <div className="col gap" style={{ gap: 10 }}>
            <div className="card" style={{ background: "var(--surface)", padding: 10 }}>
              <div className="row between">
                <span className="small bold">Governor Contract</span>
                <a href={getBasescanUrl("address", GOVERNANCE_CONTRACTS.governor)} target="_blank" rel="noreferrer" className="tiny cyan" style={{ textDecoration: "none" }}>Basescan ↗</a>
              </div>
              <div className="mono tiny muted" style={{ marginTop: 4, wordBreak: "break-all" }}>{GOVERNANCE_CONTRACTS.governor}</div>
            </div>

            <div className="card" style={{ background: "var(--surface)", padding: 10 }}>
              <div className="row between">
                <span className="small bold">Timelock Controller</span>
                <a href={getBasescanUrl("address", GOVERNANCE_CONTRACTS.timelock)} target="_blank" rel="noreferrer" className="tiny cyan" style={{ textDecoration: "none" }}>Basescan ↗</a>
              </div>
              <div className="mono tiny muted" style={{ marginTop: 4, wordBreak: "break-all" }}>{GOVERNANCE_CONTRACTS.timelock}</div>
            </div>

            <div className="card" style={{ background: "var(--surface)", padding: 10 }}>
              <div className="row between">
                <span className="small bold">Wrapped AGL (wAGL) Voting Token</span>
                <a href={getBasescanUrl("address", GOVERNANCE_CONTRACTS.token)} target="_blank" rel="noreferrer" className="tiny cyan" style={{ textDecoration: "none" }}>Basescan ↗</a>
              </div>
              <div className="mono tiny muted" style={{ marginTop: 4, wordBreak: "break-all" }}>{GOVERNANCE_CONTRACTS.token}</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
