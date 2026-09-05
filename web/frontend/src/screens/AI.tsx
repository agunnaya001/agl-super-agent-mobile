import { useEffect, useRef, useState } from "react";
import { api } from "../api";
import { Markdown } from "../ui";

type Msg = { role: "user" | "agent"; text: string };

export function AIScreen() {
  const [tab, setTab] = useState<"CHAT" | "ANALYZER" | "AUDIT">("CHAT");
  return (
    <div>
      <div className="tabs">
        <button className={`tab ${tab === "CHAT" ? "active" : ""}`} onClick={() => setTab("CHAT")}>💬 Chat</button>
        <button className={`tab ${tab === "ANALYZER" ? "active" : ""}`} onClick={() => setTab("ANALYZER")}>📜 Contract Analyzer</button>
        <button className={`tab ${tab === "AUDIT" ? "active" : ""}`} onClick={() => setTab("AUDIT")}>🛡️ Security Audit</button>
      </div>
      {tab === "CHAT" && <ChatTab />}
      {tab === "ANALYZER" && <AnalyzerTab />}
      {tab === "AUDIT" && <AuditTab />}
    </div>
  );
}

function ChatTab() {
  const [messages, setMessages] = useState<Msg[]>([
    { role: "agent", text: "Welcome to **AGL Super Agent**! I'm your AI Web3 intelligence command center on Base. Ask me to explain transactions, analyze smart contracts, audit security risks, or guide your Web3 learning." },
  ]);
  const [input, setInput] = useState("");
  const [thinking, setThinking] = useState(false);
  const [followUps, setFollowUps] = useState<string[]>([]);
  const endRef = useRef<HTMLDivElement>(null);

  useEffect(() => { api.getFollowUps().then(setFollowUps).catch(() => {}); }, []);
  useEffect(() => { endRef.current?.scrollIntoView({ behavior: "smooth" }); }, [messages, thinking]);

  const send = async (text: string) => {
    if (!text.trim() || thinking) return;
    const history = messages.map((m) => ({ role: m.role === "user" ? "user" : "model", text: m.text }));
    setMessages((m) => [...m, { role: "user", text }, { role: "agent", text: "" }]);
    setInput(""); setThinking(true);
    try {
      const r = await api.aiChat(text, history);
      setMessages((m) => { const c = [...m]; c[c.length - 1] = { role: "agent", text: r.reply || r.error || "No response." }; return c; });
    } catch (e) {
      setMessages((m) => { const c = [...m]; c[c.length - 1] = { role: "agent", text: `⚠️ Request failed: ${(e as Error).message}` }; return c; });
    }
    setThinking(false);
  };

  return (
    <div>
      <div className="chat-list">
        {messages.map((m, i) => (
          <div key={i} className={`chat-bubble ${m.role === "user" ? "user" : "agent"}`}>
            {m.role === "agent" ? <Markdown text={m.text} /> : m.text}
          </div>
        ))}
        {thinking && <div className="chat-bubble agent"><div className="loader" /></div>}
        <div ref={endRef} />
      </div>

      {followUps.length > 0 && (
        <div className="row gap" style={{ flexWrap: "wrap", marginBottom: 10 }}>
          {followUps.slice(0, 4).map((f) => (
            <button key={f} className="tab" onClick={() => send(f)}>{f}</button>
          ))}
        </div>
      )}

      <div className="row gap">
        <input className="input" placeholder="Ask the AI agent…" value={input} onChange={(e) => setInput(e.target.value)} onKeyDown={(e) => e.key === "Enter" && send(input)} />
        <button className="btn" style={{ width: "auto", padding: "12px 18px" }} disabled={thinking} onClick={() => send(input)}>Send</button>
      </div>
    </div>
  );
}

function AnalyzerTab() {
  const [addr, setAddr] = useState("");
  const [result, setResult] = useState("");
  const [loading, setLoading] = useState(false);

  const run = async () => {
    if (!addr.trim()) return;
    setLoading(true); setResult("");
    try {
      const r = await api.analyzeContract(addr.trim());
      setResult(r.reply || r.error || "No result.");
    } catch (e) { setResult(`⚠️ ${(e as Error).message}`); }
    setLoading(false);
  };

  return (
    <div>
      <div className="card">
        <div className="small muted" style={{ marginBottom: 8 }}>Enter a Base Mainnet contract address to analyze</div>
        <input className="input" placeholder="0xEA1221B4d80A89BD8C75248Fae7c176BD1854698" value={addr} onChange={(e) => setAddr(e.target.value)} />
        <div style={{ height: 10 }} />
        <button className="btn" disabled={loading} onClick={run}>{loading ? <><span className="loader" style={{ width: 16, height: 16 }} /> Analyzing…</> : "🔍 Analyze Contract"}</button>
      </div>
      {result && <div className="card"><Markdown text={result} /></div>}
    </div>
  );
}

function AuditTab() {
  const [target, setTarget] = useState("");
  const [result, setResult] = useState("");
  const [loading, setLoading] = useState(false);

  const run = async () => {
    if (!target.trim()) return;
    setLoading(true); setResult("");
    try {
      const r = await api.auditSecurity(target.trim());
      setResult(r.reply || r.error || "No result.");
    } catch (e) { setResult(`⚠️ ${(e as Error).message}`); }
    setLoading(false);
  };

  return (
    <div>
      <div className="card">
        <div className="small muted" style={{ marginBottom: 8 }}>Audit a wallet address, contract, or transaction hash</div>
        <input className="input" placeholder="0x… address or tx hash" value={target} onChange={(e) => setTarget(e.target.value)} />
        <div style={{ height: 10 }} />
        <button className="btn" disabled={loading} onClick={run}>{loading ? <><span className="loader" style={{ width: 16, height: 16 }} /> Auditing…</> : "🛡️ Run Security Audit"}</button>
      </div>
      {result && <div className="card"><Markdown text={result} /></div>}
    </div>
  );
}
