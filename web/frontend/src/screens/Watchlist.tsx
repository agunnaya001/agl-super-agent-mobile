import { useEffect, useState } from "react";
import { api } from "../api";
import { shortAddr, timeAgo } from "../ui";

export function WatchlistScreen({ wallet }: { wallet: string }) {
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const [label, setLabel] = useState("");
  const [addr, setAddr] = useState("");
  const [type, setType] = useState("WHALE");

  const load = async () => { setLoading(true); setItems(await api.getWatchlist().catch(() => [])); setLoading(false); };
  useEffect(() => { load(); }, []);

  const add = async () => {
    if (!/^0x[a-fA-F0-9]{40}$/.test(addr.trim())) return;
    await api.addWatchlist(label || "Custom Watch", addr.trim(), type).catch(() => {});
    setLabel(""); setAddr(""); setShowAdd(false);
    load();
  };

  const remove = async (id: string) => {
    await api.removeWatchlist(id).catch(() => {});
    load();
  };

  if (loading) return <div className="center"><div className="loader" /></div>;

  return (
    <div>
      <div className="section-title" style={{ marginTop: 0 }}>👁️ Whale & Governance Watchlist</div>
      <div className="card card-elev">
        <div className="row between">
          <div className="row gap"><span style={{ fontSize: 18 }}>📡</span><span className="bold">Tracking {items.length} Addresses</span></div>
          <button className="btn cyan" style={{ width: "auto", fontSize: 12, padding: "6px 12px" }} onClick={() => setShowAdd(!showAdd)}>
            {showAdd ? "✕ Cancel" : "+ Add Address"}
          </button>
        </div>
        <div className="small muted" style={{ marginTop: 8 }}>
          Monitor whale movements, governance proposals, and delegation changes for tracked addresses.
        </div>
      </div>

      {showAdd && (
        <div className="card">
          <div className="small muted" style={{ marginBottom: 4 }}>Label</div>
          <input className="input" placeholder="My Whale" value={label} onChange={(e) => setLabel(e.target.value)} />
          <div style={{ height: 8 }} />
          <div className="small muted" style={{ marginBottom: 4 }}>Address</div>
          <input className="input" placeholder="0x…" value={addr} onChange={(e) => setAddr(e.target.value)} />
          <div style={{ height: 8 }} />
          <div className="small muted" style={{ marginBottom: 4 }}>Type</div>
          <select className="input" value={type} onChange={(e) => setType(e.target.value)}>
            <option value="WHALE">🐋 Whale</option>
            <option value="GOVERNANCE">🏛️ Governance</option>
            <option value="TREASURY">💰 Treasury</option>
            <option value="CUSTOM">📌 Custom</option>
          </select>
          <div style={{ height: 12 }} />
          <button className="btn" onClick={add}>➕ Add to Watchlist</button>
        </div>
      )}

      {items.map((w) => (
        <div key={w.id} className="card">
          <div className="row between">
            <div className="row gap">
              <span style={{ fontSize: 20 }}>
                {w.type === "WHALE" ? "🐋" : w.type === "GOVERNANCE" ? "🏛️" : w.type === "TREASURY" ? "💰" : "📌"}
              </span>
              <div>
                <div className="bold">{w.label}</div>
                <div className="tiny muted mono">{shortAddr(w.address)}</div>
              </div>
            </div>
            <div style={{ textAlign: "right" }}>
              <span className="pill cyan">{w.balanceAgl} AGL</span>
              <div style={{ height: 4 }} />
              {w.id.startsWith("custom-") && (
                <button className="tiny" style={{ color: "var(--rose)", background: "none", border: "none", cursor: "pointer" }} onClick={() => remove(w.id)}>
                  Remove
                </button>
              )}
            </div>
          </div>
          <div style={{ height: 8 }} />
          <div className="row between">
            <span className="tiny muted">Last activity</span>
            <span className="small">{w.lastActivity}</span>
          </div>
          <div className="tiny muted" style={{ marginTop: 2 }}>{timeAgo(w.lastActivityTs)}</div>
          <div style={{ height: 6 }} />
          <div className="row gap">
            {w.tags?.map((t: string) => <span key={t} className="pill" style={{ background: "var(--surface)", color: "var(--text-2)", fontSize: 10 }}>{t}</span>)}
          </div>
        </div>
      ))}
    </div>
  );
}
