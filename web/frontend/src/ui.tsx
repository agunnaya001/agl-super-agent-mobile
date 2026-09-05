// Shared formatting & UI helpers.

export function shortAddr(a: string): string {
  if (!a || a.length < 10) return a;
  return `${a.slice(0, 6)}…${a.slice(-4)}`;
}

export function fmtUsd(n: number, max = 2): string {
  return `$${n.toLocaleString("en-US", { minimumFractionDigits: max <= 2 ? 2 : 0, maximumFractionDigits: max })}`;
}

export function fmtNum(n: number): string {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`;
  if (n >= 1_000) return `${(n / 1_000).toFixed(1)}K`;
  return n.toLocaleString("en-US");
}

export function timeAgo(ts: number): string {
  const diff = Date.now() - ts;
  if (diff < 60_000) return "just now";
  if (diff < 3_600_000) return `${Math.floor(diff / 60_000)}m ago`;
  if (diff < 86_400_000) return `${Math.floor(diff / 3_600_000)}h ago`;
  return new Date(ts).toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

export function statusPill(status: string) {
  const s = status.toUpperCase();
  if (s === "SUCCESS") return <span className="pill green">SUCCESS</span>;
  if (s === "PENDING") return <span className="pill gold">PENDING</span>;
  if (s === "FAILED") return <span className="pill rose">FAILED</span>;
  return <span className="pill cyan">{s}</span>;
}

export function tierPill(tier: string) {
  const t = tier.toUpperCase();
  if (t === "DIAMOND") return <span className="pill cyan">Diamond</span>;
  if (t === "GOLD") return <span className="pill gold">Gold</span>;
  if (t === "SILVER") return <span className="pill purple">Silver</span>;
  return <span className="pill" style={{ background: "rgba(205,127,50,0.15)", color: "#cd7f32" }}>Bronze</span>;
}

// Very small markdown renderer for AI replies (headings, bold, lists, code, paragraphs).
export function Markdown({ text }: { text: string }) {
  const lines = text.split("\n");
  const out: React.ReactNode[] = [];
  let list: React.ReactNode[] = [];
  let listType: "ul" | "ol" | null = null;

  const flushList = () => {
    if (list.length) {
      out.push(listType === "ol" ? <ol key={out.length}>{list}</ol> : <ul key={out.length}>{list}</ul>);
      list = [];
      listType = null;
    }
  };

  const inline = (s: string): React.ReactNode => {
    const parts = s.split(/(\*\*[^*]+\*\*|`[^`]+`)/g);
    return parts.map((p, i) => {
      if (p.startsWith("**") && p.endsWith("**")) return <strong key={i}>{p.slice(2, -2)}</strong>;
      if (p.startsWith("`") && p.endsWith("`")) return <code key={i}>{p.slice(1, -1)}</code>;
      return p;
    });
  };

  lines.forEach((line, i) => {
    if (/^###\s/.test(line)) { flushList(); out.push(<h3 key={i}>{inline(line.replace(/^###\s/, ""))}</h3>); }
    else if (/^##\s/.test(line)) { flushList(); out.push(<h2 key={i}>{inline(line.replace(/^##\s/, ""))}</h2>); }
    else if (/^#\s/.test(line)) { flushList(); out.push(<h1 key={i}>{inline(line.replace(/^#\s/, ""))}</h1>); }
    else if (/^\s*[-*]\s/.test(line)) { listType = "ul"; list.push(<li key={i}>{inline(line.replace(/^\s*[-*]\s/, ""))}</li>); }
    else if (/^\s*\d+\.\s/.test(line)) { listType = "ol"; list.push(<li key={i}>{inline(line.replace(/^\s*\d+\.\s/, ""))}</li>); }
    else if (line.trim() === "") { flushList(); }
    else { flushList(); out.push(<p key={i}>{inline(line)}</p>); }
  });
  flushList();
  return <div className="md">{out}</div>;
}
