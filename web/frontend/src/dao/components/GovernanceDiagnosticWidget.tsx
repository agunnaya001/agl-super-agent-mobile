import React, { useState } from "react";
import { useGovernanceDiagnostic } from "../lib/useGovernanceDiagnostic";

export function GovernanceDiagnosticWidget() {
  const diagnostic = useGovernanceDiagnostic({ pollIntervalMs: 30000 });
  const [showLogs, setShowLogs] = useState(false);

  const rpcEndpoint = diagnostic.rpc?.endpoint ? new URL(diagnostic.rpc.endpoint).hostname : "Connecting...";
  const isGovVerified = diagnostic.governor?.status === "verified" || diagnostic.governor?.canInteract;
  const isTimelockBonded = diagnostic.timelock?.governorBonded || diagnostic.timelock?.canInteract;

  return (
    <div className="card card-elev" style={{ border: "1px solid rgba(0, 240, 255, 0.3)", marginBottom: 16 }}>
      <div className="row between" style={{ marginBottom: 10 }}>
        <div className="row gap" style={{ alignItems: "center" }}>
          <span style={{ fontSize: 20 }}>🛡️</span>
          <div>
            <div className="bold" style={{ fontSize: 14 }}>Base Mainnet Governance Diagnostics</div>
            <div className="tiny muted">Governor & Timelock RPC Health Monitor</div>
          </div>
        </div>
        <div className="row gap" style={{ alignItems: "center" }}>
          <span className={`pill ${diagnostic.isHealthy ? "green" : diagnostic.isLoading ? "cyan" : "amber"}`}>
            {diagnostic.isLoading ? "PROBING..." : diagnostic.status.toUpperCase()}
          </span>
          <button
            className="btn"
            style={{ width: "auto", padding: "4px 10px", fontSize: 11 }}
            onClick={() => diagnostic.checkConnection()}
            disabled={diagnostic.isLoading}
          >
            {diagnostic.isLoading ? "Checking..." : "🔄 Re-check"}
          </button>
        </div>
      </div>

      {/* Metric Tiles */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(130px, 1fr))", gap: 8, marginBottom: 12 }}>
        <div className="card" style={{ background: "var(--surface)", padding: 8 }}>
          <div className="tiny muted">Active RPC</div>
          <div className="bold small" style={{ color: "var(--cyan)", wordBreak: "break-all" }}>
            {rpcEndpoint}
          </div>
          <div className="tiny muted">{diagnostic.rpc?.latencyMs ? `${diagnostic.rpc.latencyMs}ms` : "—"}</div>
        </div>

        <div className="card" style={{ background: "var(--surface)", padding: 8 }}>
          <div className="tiny muted">Base Block Height</div>
          <div className="bold small" style={{ color: "var(--fg)" }}>
            {diagnostic.rpc?.blockNumber ? `#${diagnostic.rpc.blockNumber.toLocaleString()}` : "—"}
          </div>
          <div className="tiny muted">Chain ID: {diagnostic.rpc?.chainId || 8453}</div>
        </div>

        <div className="card" style={{ background: "var(--surface)", padding: 8 }}>
          <div className="tiny muted">Governor Bond</div>
          <div className="bold small" style={{ color: isGovVerified ? "var(--emerald)" : "var(--amber)" }}>
            {isGovVerified ? "✅ Verified" : "Verifying"}
          </div>
          <div className="tiny muted">{diagnostic.governor?.name || "Agunnaya DAO"}</div>
        </div>

        <div className="card" style={{ background: "var(--surface)", padding: 8 }}>
          <div className="tiny muted">Timelock Bond</div>
          <div className="bold small" style={{ color: isTimelockBonded ? "var(--emerald)" : "var(--amber)" }}>
            {isTimelockBonded ? "🔒 Bonded" : "Verifying"}
          </div>
          <div className="tiny muted">{diagnostic.timelock?.formattedMinDelay ? `Delay: ${diagnostic.timelock.formattedMinDelay}` : "Delay: 24h"}</div>
        </div>
      </div>

      {/* Log Accordion */}
      <div>
        <button
          className="btn"
          style={{ width: "100%", background: "transparent", border: "1px dashed var(--border)", fontSize: 11, padding: "6px 10px" }}
          onClick={() => setShowLogs(!showLogs)}
        >
          {showLogs ? "Hide Audit Diagnostic Logs ▲" : `Show Diagnostic Audit Logs (${diagnostic.logs.length}) ▼`}
        </button>

        {showLogs && (
          <div
            style={{
              marginTop: 8,
              background: "#05070e",
              borderRadius: 8,
              padding: 10,
              maxHeight: 180,
              overflowY: "auto",
              fontFamily: "monospace",
              fontSize: 10,
              lineHeight: 1.5,
              border: "1px solid var(--border)"
            }}
          >
            {diagnostic.logs.map((log, idx) => (
              <div key={`${log.timestamp}-${idx}`} style={{ color: log.level === "error" ? "#ff453a" : log.level === "warn" ? "#ffb020" : log.level === "success" ? "#00e699" : "#a0aec0" }}>
                <span style={{ color: "#4a5568" }}>[{new Date(log.timestamp).toLocaleTimeString()}]</span>{" "}
                <span style={{ color: "var(--cyan)" }}>[{log.level.toUpperCase()}]</span> {log.message}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
