import React from "react";
import { TimelockOperation } from "../hooks/useTimelockStatus";
import { truncateAddress, getBasescanUrl } from "../lib/contracts";

interface TimelockQueueTableProps {
  operations: TimelockOperation[];
  onExecute?: (opId: string) => void;
}

export function TimelockQueueTable({ operations, onExecute }: TimelockQueueTableProps) {
  if (operations.length === 0) {
    return (
      <div className="card" style={{ textAlign: "center", padding: 18, color: "var(--muted)" }}>
        No pending operations in the Timelock queue.
      </div>
    );
  }

  return (
    <div className="card card-elev" style={{ overflowX: "auto" }}>
      <div className="bold small" style={{ marginBottom: 10 }}>🔒 Scheduled Timelock Execution Queue</div>
      <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 11, textAlign: "left" }}>
        <thead>
          <tr style={{ borderBottom: "1px solid var(--border)", color: "var(--muted)" }}>
            <th style={{ padding: "8px 6px" }}>Operation ID</th>
            <th style={{ padding: "8px 6px" }}>Target</th>
            <th style={{ padding: "8px 6px" }}>Description</th>
            <th style={{ padding: "8px 6px" }}>Status</th>
            <th style={{ padding: "8px 6px" }}>Action</th>
          </tr>
        </thead>
        <tbody>
          {operations.map((op) => (
            <tr key={op.id} style={{ borderBottom: "1px solid rgba(255, 255, 255, 0.05)" }}>
              <td className="mono tiny" style={{ padding: "8px 6px", color: "var(--cyan)" }}>
                {truncateAddress(op.id, 8, 4)}
              </td>
              <td style={{ padding: "8px 6px" }}>
                <a
                  href={getBasescanUrl("address", op.target)}
                  target="_blank"
                  rel="noreferrer"
                  style={{ color: "var(--cyan)", textDecoration: "none" }}
                >
                  {truncateAddress(op.target, 6, 4)} ↗
                </a>
              </td>
              <td style={{ padding: "8px 6px", color: "var(--fg)" }}>{op.description}</td>
              <td style={{ padding: "8px 6px" }}>
                <span className={`pill ${op.ready ? "green" : op.done ? "gray" : "cyan"}`}>
                  {op.ready ? "READY" : op.done ? "EXECUTED" : "DELAY"}
                </span>
              </td>
              <td style={{ padding: "8px 6px" }}>
                {op.ready && onExecute && (
                  <button
                    className="btn"
                    style={{ padding: "4px 8px", fontSize: 10, background: "var(--emerald)", color: "#000", fontWeight: 700 }}
                    onClick={() => onExecute(op.id)}
                  >
                    Execute
                  </button>
                )}
                {!op.ready && <span className="tiny muted">Locked</span>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
