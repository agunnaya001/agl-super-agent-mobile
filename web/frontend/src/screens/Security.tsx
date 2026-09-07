import { useEffect, useState } from "react";
import { api } from "../api";
import { Markdown, shortAddr, statusPill } from "../ui";

type Tab = "APPROVALS" | "RISK" | "PHISHING";

export function SecurityScreen({ wallet }: { wallet: string }) {
  const [tab, setTab] = useState<Tab>("APPROVALS");
  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>🛡️ Security Center</div>
      <div className="tabs">
        <button className={`tab ${tab === "APPROVALS" ? "active" : ""}`} onClick={() => setTab("APPROVALS")}>🔓 Approvals</button>
        <button className={`tab ${tab === "RISK" ? "active" : ""}`} onClick={() => setTab("RISK")}>⚠️ Risk Score</button>
        <button className={`tab ${tab === "PHISHING" ? "active" : ""}`} onClick={() => setTab("PHISHING")}>🎣 Phishing Check</button>
      </div>
      {tab === "APPROVALS" && <ApprovalsTab wallet={wallet} />}
      {tab === "RISK" && <RiskTab />}
      {tab === "PHISHING" && <PhishingTab />}
    </div>
  );
}

function ApprovalsTab({ wallet }: { wallet: string }) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const load = async () => { setLoading(true); setData(await api.getApprovals(wallet).catch(() => null)); setLoading(false); };
  useEffect(() => { load(); }, [wallet]);
  if (loading) return <div className="center"><div className="loader" /></div>;
  const approvals = data?.approvals || [];
  return (
    <div>
      <div className="card card-elev">
        <div className="row between">
          <div className="row gap"><span style={{ fontSize: 18 }}>🔓</span><span className="bold">Token Approval Scanner</span></div>
          <span className="pill cyan">{shortAddr(wallet)}</span>
        </div>
        <div className="small muted" style={{ marginTop: 8 }}>
          Scans ERC-20 allowances for your wallet. Flag risky or unlimited approvals and revoke guidance.
        </div>
        <div style={{ height: 10 }} />
        <button className="btn ghost" onClick={load}>🔄 Re-scan</button>
      </div>
      {approvals.length === 0 ? (
        <div className="card" style={{ textAlign: "center" }}>
          <div style={{ fontSize: 32 }}>✅</div>
          <div className="bold" style={{ marginTop: 6 }}>No Active Approvals Found</div>
          <div className="small muted" style={{ marginTop: 4 }}>Your wallet has no open token allowances to ecosystem contracts.</div>
        </div>
      ) : (
        approvals.map((a: any, i: number) => (
          <div key={i} className="card">
            <div className="row between">
              <div className="row gap">
                <span style={{ fontSize: 20 }}>{a.isEcosystem ? "🪙" : "⚠️"}</span>
                <div>
                  <div className="bold">{a.tokenSymbol} → {a.spenderName}</div>
                  <div className="tiny muted mono">{shortAddr(a.spenderAddress)}</div>
                </div>
              </div>
              {a.isUnlimited
                ? <span className="pill rose">UNLIMITED</span>
                : <span className="pill gold">{a.allowanceFormatted}</span>}
            </div>
            <div style={{ height: 8 }} />
            <div className="row between">
              <span className="tiny muted">Risk: {a.riskLevel}</span>
              {a.isUnlimited && <span className="tiny" style={{ color: "var(--rose)" }}>⚠️ Consider revoking unlimited approval</span>}
            </div>
            {a.isUnlimited && (
              <button className="btn" style={{ marginTop: 10, background: "var(--rose)" }}>
                🚫 Revoke Approval (requires signer)
              </button>
            )}
          </div>
        ))
      )}
      <div className="card">
        <div className="small muted">
          💡 <strong>Tip:</strong> Only approve the exact amount you need. Unlimited approvals (MAX_UINT256) are a common attack vector.
          Live approval history requires an event indexer; this scanner reads current allowance state directly from Base Mainnet.
        </div>
      </div>
    </div>
  );
}

function RiskTab() {
  const [addr, setAddr] = useState("");
  const [result, setResult] = useState("");
  const [loading, setLoading] = useState(false);
  const run = async () => {
    if (!addr.trim()) return;
    setLoading(true); setResult("");
    try {
      const r = await api.addressRisk(addr.trim());
      setResult(r.reply || r.error || "No result.");
    } catch (e) { setResult(`⚠️ ${(e as Error).message}`); }
    setLoading(false);
  };
  return (
    <div>
      <div className="card">
        <div className="bold small" style={{ marginBottom: 8 }}>⚠️ Address Risk Scoring</div>
        <div className="small muted" style={{ marginBottom: 10 }}>
          Check any Base Mainnet address for honeypot, mintable, proxy, and ownership risks before interacting.
        </div>
        <input className="input" placeholder="0x… address to check" value={addr} onChange={(e) => setAddr(e.target.value)} />
        <div style={{ height: 10 }} />
        <button className="btn" disabled={loading || !addr.trim()} onClick={run}>
          {loading ? <><span className="loader" style={{ width: 16, height: 16 }} /> Scoring…</> : "🔍 Score Risk"}
        </button>
      </div>
      {result && <div className="card"><Markdown text={result} /></div>}
    </div>
  );
}

function PhishingTab() {
  const [target, setTarget] = useState("");
  const [result, setResult] = useState("");
  const [loading, setLoading] = useState(false);
  const run = async () => {
    if (!target.trim()) return;
    setLoading(true); setResult("");
    try {
      const r = await api.phishingCheck(target.trim());
      setResult(r.reply || r.error || "No result.");
    } catch (e) { setResult(`⚠️ ${(e as Error).message}`); }
    setLoading(false);
  };
  return (
    <div>
      <div className="card">
        <div className="bold small" style={{ marginBottom: 8 }}>🎣 Phishing & Impersonation Detector</div>
        <div className="small muted" style={{ marginBottom: 10 }}>
          Paste an address or contract name to check if it mimics a known legitimate protocol on Base.
        </div>
        <input className="input" placeholder="0x… or contract name" value={target} onChange={(e) => setTarget(e.target.value)} />
        <div style={{ height: 10 }} />
        <button className="btn" disabled={loading || !target.trim()} onClick={run}>
          {loading ? <><span className="loader" style={{ width: 16, height: 16 }} /> Checking…</> : "🎣 Check for Phishing"}
        </button>
      </div>
      {result && <div className="card"><Markdown text={result} /></div>}
    </div>
  );
}
