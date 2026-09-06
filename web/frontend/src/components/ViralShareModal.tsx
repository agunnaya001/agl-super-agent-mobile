import React, { useState } from "react";

interface ViralShareModalProps {
  wallet: string;
  onClose: () => void;
}

export const ViralShareModal: React.FC<ViralShareModalProps> = ({ wallet, onClose }) => {
  const [copied, setCopied] = useState(false);
  const frameUrl = "https://farcaster.frame.agunnayalabs.io/agent/0xD034E94465Db1669f80D817c66e58cF194d027C8";
  const shareText = "🚀 Check out my AGL Super Agent on Base! Automated DEX arbitrage, 84.5% Aerodrome APY yield & 1-click smart audits. Join the AI revolution on Base Mainnet! $AGL #Base #AIAgent";

  const handleCopy = () => {
    navigator.clipboard.writeText(`${shareText}\n${frameUrl}`);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: "rgba(0,0,0,0.8)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1000,
        padding: 16
      }}
    >
      <div className="card card-elev" style={{ width: "100%", maxWidth: 460, padding: 20 }}>
        {/* Header */}
        <div className="row between" style={{ marginBottom: 14 }}>
          <div className="row gap">
            <span style={{ fontSize: 22 }}>🚀</span>
            <div>
              <div className="bold">Viral Growth & Rewards</div>
              <div className="tiny muted">Farcaster Frame & Proof-of-Agent Cards</div>
            </div>
          </div>
          <button
            onClick={onClose}
            style={{ background: "none", border: "none", color: "var(--text-2)", fontSize: 18, cursor: "pointer" }}
          >
            ✕
          </button>
        </div>

        {/* Farcaster Frame Card */}
        <div
          style={{
            background: "linear-gradient(135deg, #472A91, #1E1035)",
            border: "1px solid #8A63D2",
            borderRadius: 12,
            padding: 14,
            marginBottom: 14
          }}
        >
          <div className="row between" style={{ marginBottom: 10 }}>
            <span className="bold small" style={{ color: "#E4D5FF" }}>
              💜 Farcaster Frame Preview
            </span>
            <span className="pill purple" style={{ fontSize: 10 }}>
              Warpcast Ready
            </span>
          </div>

          <div style={{ background: "var(--card-bg)", border: "1px solid var(--border)", borderRadius: 10, padding: 12, marginBottom: 12 }}>
            <div className="bold small">AGL Super Agent #8842</div>
            <div className="tiny green bold" style={{ marginTop: 2 }}>
              30-Day Yield: +18.4% ($2,640.00) · Aerodrome LP: Active
            </div>

            <div className="row gap" style={{ marginTop: 10 }}>
              <div className="pill purple" style={{ flex: 1, textAlign: "center", padding: "6px 0", cursor: "pointer", fontSize: 10 }}>
                1-Tap Stake AGL
              </div>
              <div className="pill cyan" style={{ flex: 1, textAlign: "center", padding: "6px 0", cursor: "pointer", fontSize: 10 }}>
                Audit Contract
              </div>
            </div>
          </div>

          <div className="row gap">
            <a
              href={`https://warpcast.com/~/compose?text=${encodeURIComponent(shareText)}&embeds[]=${encodeURIComponent(frameUrl)}`}
              target="_blank"
              rel="noreferrer"
              className="btn purple"
              style={{ flex: 1, textDecoration: "none", textAlign: "center", fontSize: 11 }}
            >
              Post on Warpcast
            </a>
            <a
              href={`https://twitter.com/intent/tweet?text=${encodeURIComponent(shareText)}`}
              target="_blank"
              rel="noreferrer"
              className="btn blue"
              style={{ flex: 1, textDecoration: "none", textAlign: "center", fontSize: 11 }}
            >
              Share on X
            </a>
          </div>
        </div>

        {/* Copy Share Text */}
        <button className="btn outline" style={{ width: "100%", fontSize: 12 }} onClick={handleCopy}>
          {copied ? "✓ Copied Share Link & Text!" : "📋 Copy Frame URL & Promo Text"}
        </button>
      </div>
    </div>
  );
};
