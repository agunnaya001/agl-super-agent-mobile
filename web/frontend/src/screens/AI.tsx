import { useEffect, useRef, useState } from "react";
import { api } from "../api";
import { Markdown } from "../ui";
import { Web3LearningModule } from "../components/Web3LearningModule";

type Msg = { role: "user" | "agent"; text: string };
type TabType = "CHAT" | "LEARN" | "ANALYZER" | "AUDIT";

export function AIScreen() {
  const [tab, setTab] = useState<TabType>("CHAT");
  return (
    <div>
      <div className="tabs">
        <button className={`tab ${tab === "CHAT" ? "active" : ""}`} onClick={() => setTab("CHAT")}>💬 Chat</button>
        <button className={`tab ${tab === "LEARN" ? "active" : ""}`} onClick={() => setTab("LEARN")}>🎓 Web3 Learning</button>
        <button className={`tab ${tab === "ANALYZER" ? "active" : ""}`} onClick={() => setTab("ANALYZER")}>📜 Contract Analyzer</button>
        <button className={`tab ${tab === "AUDIT" ? "active" : ""}`} onClick={() => setTab("AUDIT")}>🛡️ Audit</button>
      </div>
      {tab === "CHAT" && <ChatTab onOpenLearn={() => setTab("LEARN")} />}
      {tab === "LEARN" && <Web3LearningModule onJumpToAudit={() => setTab("AUDIT")} />}
      {tab === "ANALYZER" && <AnalyzerTab />}
      {tab === "AUDIT" && <AuditTab />}
    </div>
  );
}

function ChatTab({ onOpenLearn }: { onOpenLearn?: () => void }) {
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
      {onOpenLearn && (
        <div
          className="card"
          style={{
            background: "linear-gradient(135deg, rgba(0,82,255,0.12) 0%, rgba(0,212,255,0.08) 100%)",
            borderColor: "rgba(0,212,255,0.3)",
            padding: "10px 14px",
            marginBottom: 12,
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <div className="row gap" style={{ gap: 8 }}>
            <span style={{ fontSize: 18 }}>🎓</span>
            <div className="small">
              <strong style={{ color: "var(--cyan)" }}>New to Base Protocols?</strong> Interactive tutorial available.
            </div>
          </div>
          <button
            className="btn cyan"
            style={{ width: "auto", fontSize: 11, padding: "5px 10px", whiteSpace: "nowrap" }}
            onClick={onOpenLearn}
          >
            Start Tutorial →
          </button>
        </div>
      )}

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

  const presets = [
    { label: "AGL Token", addr: "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698" },
    { label: "wAGL Gov", addr: "0x356AbeDE92d53D9Fe5165d21A2eEB6c321CEa7b4" },
    { label: "DAO Timelock", addr: "0x231a47BE13A7862562FE14Fce5b106294aF44aD8" },
    { label: "Aerodrome Pool", addr: "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913" },
  ];

  const handlePaste = async () => {
    try {
      const text = await navigator.clipboard.readText();
      if (text) setTarget(text.trim());
    } catch {
      // clipboard permission denied or not supported
    }
  };

  const run = async (addrToRun?: string) => {
    const finalAddr = (addrToRun || target).trim();
    if (!finalAddr) return;
    setLoading(true); setResult("");
    try {
      const r = await api.auditSecurity(finalAddr);
      setResult(r.reply || r.error || "No result.");
    } catch (e) { setResult(`⚠️ ${(e as Error).message}`); }
    setLoading(false);
  };

  return (
    <div>
      <div className="card">
        <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 4 }}>🛡️ Smart Contract Security Audit</div>
        <div className="small muted" style={{ marginBottom: 12 }}>
          Paste any smart contract address on Base Mainnet (Chain ID 8453) to summarize potential vulnerabilities using the AI backend.
        </div>
        
        <div className="row gap" style={{ marginBottom: 10 }}>
          <input
            className="input"
            placeholder="0x... contract address on Base"
            value={target}
            onChange={(e) => setTarget(e.target.value)}
          />
          <button className="tab" style={{ padding: "0 14px" }} onClick={handlePaste} title="Paste from clipboard">
            📋 Paste
          </button>
        </div>

        <div className="small muted" style={{ marginBottom: 6 }}>Verified Base presets:</div>
        <div className="row gap" style={{ flexWrap: "wrap", marginBottom: 14 }}>
          {presets.map((p) => (
            <button
              key={p.addr}
              className="tab"
              style={{ fontSize: 11, padding: "4px 8px" }}
              onClick={() => {
                setTarget(p.addr);
                run(p.addr);
              }}
            >
              {p.label}
            </button>
          ))}
        </div>

        <button className="btn" disabled={loading || !target.trim()} onClick={() => run()}>
          {loading ? <><span className="loader" style={{ width: 16, height: 16 }} /> Auditing Contract…</> : "🛡️ Run AI Contract Audit"}
        </button>
      </div>

      {result && (
        <div className="card" style={{ marginTop: 12, position: "relative" }}>
          <div className="row gap" style={{ justifyContent: "space-between", marginBottom: 8 }}>
            <span style={{ fontWeight: 600, fontSize: 13, color: "var(--color-primary, #00d2ff)" }}>
              AI Vulnerability Report
            </span>
            <button
              className="tab"
              style={{ fontSize: 11, padding: "2px 8px" }}
              onClick={() => navigator.clipboard.writeText(result)}
            >
              Copy Report
            </button>
          </div>
          <Markdown text={result} />
        </div>
      )}
    </div>
  );
}
