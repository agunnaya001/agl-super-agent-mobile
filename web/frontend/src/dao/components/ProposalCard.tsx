import React, { useState } from "react";
import { GovernanceProposal } from "../hooks/useGovernanceProposals";
import { truncateAddress } from "../lib/contracts";

interface ProposalCardProps {
  proposal: GovernanceProposal;
  onVote?: (proposalId: string, support: 0 | 1 | 2) => void;
}

export function ProposalCard({ proposal, onVote }: ProposalCardProps) {
  const [voted, setVoted] = useState<number | null>(null);

  const totalVotes = proposal.forVotes + proposal.againstVotes + proposal.abstainVotes || 1;
  const forPct = Math.round((proposal.forVotes / totalVotes) * 100);
  const againstPct = Math.round((proposal.againstVotes / totalVotes) * 100);

  const handleVote = (support: 0 | 1 | 2) => {
    setVoted(support);
    if (onVote) onVote(proposal.id, support);
  };

  return (
    <div className="card card-elev" style={{ marginBottom: 12, border: "1px solid var(--border)" }}>
      <div className="row between" style={{ marginBottom: 6 }}>
        <div className="row gap" style={{ alignItems: "center" }}>
          <span className="bold small" style={{ color: "var(--cyan)" }}>{proposal.id}</span>
          <span className="tiny muted">by {truncateAddress(proposal.proposer)}</span>
        </div>
        <span className={`pill ${proposal.status === "Active" ? "green" : proposal.status === "Queued" ? "cyan" : "gray"}`}>
          {proposal.status.toUpperCase()}
        </span>
      </div>

      <div className="bold" style={{ fontSize: 15, marginBottom: 6 }}>{proposal.title}</div>
      <div className="small muted" style={{ marginBottom: 12, lineHeight: 1.4 }}>{proposal.description}</div>

      {/* Progress Bar */}
      <div style={{ marginBottom: 10 }}>
        <div className="row between tiny muted" style={{ marginBottom: 4 }}>
          <span>For: {proposal.forVotes.toLocaleString()} ({forPct}%)</span>
          <span>Against: {proposal.againstVotes.toLocaleString()} ({againstPct}%)</span>
        </div>
        <div style={{ height: 6, borderRadius: 3, background: "rgba(255, 255, 255, 0.1)", display: "flex", overflow: "hidden" }}>
          <div style={{ width: `${forPct}%`, background: "var(--emerald)" }} />
          <div style={{ width: `${againstPct}%`, background: "#ff453a" }} />
        </div>
      </div>

      {/* Voting Controls */}
      {proposal.status === "Active" && (
        <div className="row gap" style={{ marginTop: 10 }}>
          <button
            className="btn"
            style={{
              flex: 1,
              padding: "6px 8px",
              fontSize: 11,
              background: voted === 1 ? "var(--emerald)" : "rgba(0, 230, 153, 0.15)",
              color: voted === 1 ? "#000" : "var(--emerald)",
              border: "1px solid var(--emerald)"
            }}
            onClick={() => handleVote(1)}
          >
            {voted === 1 ? "✓ Voted For" : "Vote For (Yes)"}
          </button>
          <button
            className="btn"
            style={{
              flex: 1,
              padding: "6px 8px",
              fontSize: 11,
              background: voted === 0 ? "#ff453a" : "rgba(255, 69, 58, 0.15)",
              color: voted === 0 ? "#FFF" : "#ff453a",
              border: "1px solid #ff453a"
            }}
            onClick={() => handleVote(0)}
          >
            {voted === 0 ? "✓ Voted Against" : "Vote Against (No)"}
          </button>
          <button
            className="btn"
            style={{
              width: "auto",
              padding: "6px 10px",
              fontSize: 11,
              background: "var(--surface)",
              color: "var(--muted)"
            }}
            onClick={() => handleVote(2)}
          >
            Abstain
          </button>
        </div>
      )}

      {proposal.eta && proposal.status === "Queued" && (
        <div className="tiny muted" style={{ marginTop: 6, color: "var(--cyan)" }}>
          ⏱ Timelock ETA: Execution ready in ~{Math.max(0, Math.round((proposal.eta - Date.now() / 1000) / 3600))} hours
        </div>
      )}
    </div>
  );
}
