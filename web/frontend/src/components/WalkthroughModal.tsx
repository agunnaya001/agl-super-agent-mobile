import React, { useState } from "react";
import { Screen } from "../types";

interface WalkthroughModalProps {
  onClose: () => void;
  onNavigate?: (s: Screen) => void;
}

export const WalkthroughModal: React.FC<WalkthroughModalProps> = ({ onClose, onNavigate }) => {
  const [activeStep, setActiveStep] = useState<number>(0);

  const steps = [
    {
      id: "welcome",
      badge: "Welcome to AGL Super Agent",
      title: "Your Base Mainnet Co-Pilot",
      icon: "⚡",
      headline: "Autonomous intelligence for decentralized finance on Base",
      content:
        "AGL Super Agent combines cutting-edge AI reasoning with real-time blockchain execution. Explore how the autonomous agent protects your assets, maximizes yield, and simplifies on-chain actions.",
      bullets: [
        "Non-custodial smart portfolio tracking on Base (Chain 8453)",
        "Zero-latency multi-RPC failover for maximum reliability",
        "Interactive analytics and decentralized yield optimization",
      ],
      actionText: "Next: AI Agent →",
      route: null,
    },
    {
      id: "ai",
      badge: "Feature 1: AI Agent",
      title: "Autonomous Agent & Security Audits",
      icon: "🤖",
      headline: "Gemini-powered blockchain analysis and automated vigilance",
      content:
        "The AI Agent continuously scans verified smart contracts on Base Mainnet, monitors liquidity pools, and generates automated suggestions tailored to your wallet.",
      bullets: [
        "Contract Auditor: Inspect smart contracts for honeypots, reentrancy bugs, and owner privileges",
        "Actionable Recommendations: One-tap execution of suggested yield rebalances and gas optimizations",
        "Conversational Assistant: Ask questions in plain English about any transaction, token, or protocol",
      ],
      actionText: "Next: Wallet Analytics →",
      route: "AI" as Screen,
      routeLabel: "Try AI Assistant",
    },
    {
      id: "wallet",
      badge: "Feature 2: Wallet Analytics",
      title: "Wallet Balance & 30-Day Trend Lines",
      icon: "👛",
      headline: "Comprehensive visual portfolio trajectory and on-chain telemetry",
      content:
        "Track your net worth in real-time with integrated balance trend lines. See daily historical trajectories across AGL, ETH, and Aerodrome vault tokens.",
      bullets: [
        "Visual 30-Day Trend: Powered by Recharts with interactive hover tooltips and range filters",
        "Asset Allocation Breakdown: Real-time division between native AGL, staked wAGL, and liquidity positions",
        "Live Transaction Indexer: Instant status receipts for swaps, claims, and bridge operations",
      ],
      actionText: "Next: Dashboard & Vaults →",
      route: "WALLET" as Screen,
      routeLabel: "View Wallet",
    },
    {
      id: "monitor",
      badge: "Feature 3: Monitor & Yield",
      title: "Node Diagnostics & Aerodrome Vaults",
      icon: "📊",
      headline: "Full transparency into Base network telemetry and decentralized yields",
      content:
        "Verify node latency, block production timestamps, and active validator health. Earn automated yield via ERC-6551 Token Bound Accounts and Aerodrome LP pools.",
      bullets: [
        "Real-Time Diagnostics: Ping Base Mainnet RPC endpoints with instant latency benchmarks",
        "Aerodrome Concentrated Liquidity: High-APR automated yield vaults auto-managed by your agent",
        "ERC-6551 Agent Accounts: Own smart contracts that hold assets and execute gasless transactions",
      ],
      actionText: "Next: Quests & Rewards →",
      route: "MONITOR" as Screen,
      routeLabel: "Explore Dashboard",
    },
    {
      id: "quests",
      badge: "Feature 4: Quests & Staking",
      title: "Earn Rewards & Level Up",
      icon: "🏆",
      headline: "Gamified on-chain learning, daily check-ins, and governance",
      content:
        "Complete daily blockchain challenges, earn AGL tokens and XP, and vote in decentralized governance proposals.",
      bullets: [
        "Daily Check-In: Claim streak bonuses and gas subsidies every 24 hours",
        "Staking APR: Lock AGL into wAGL to earn passive rewards and governance voting power",
        "Leaderboard Badges: Compete with other agents on Base for top reputation ranks",
      ],
      actionText: "Finish Walkthrough ✨",
      route: "QUESTS" as Screen,
      routeLabel: "View Quests",
    },
  ];

  const current = steps[activeStep];
  const isLast = activeStep === steps.length - 1;

  const handleNext = () => {
    if (isLast) {
      onClose();
    } else {
      setActiveStep((prev) => prev + 1);
    }
  };

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: "rgba(3, 6, 15, 0.85)",
        backdropFilter: "blur(8px)",
        zIndex: 9999,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 16,
      }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        className="card card-elev"
        style={{
          maxWidth: 520,
          width: "100%",
          maxHeight: "90vh",
          overflowY: "auto",
          border: "1px solid rgba(0, 210, 255, 0.4)",
          boxShadow: "0 16px 48px rgba(0, 210, 255, 0.15), 0 24px 64px rgba(0,0,0,0.8)",
          padding: 24,
          position: "relative",
          animation: "fadeIn 0.2s ease-out",
        }}
      >
        {/* Close button */}
        <button
          onClick={onClose}
          style={{
            position: "absolute",
            top: 16,
            right: 16,
            background: "transparent",
            border: "none",
            color: "var(--text-2)",
            fontSize: 20,
            cursor: "pointer",
            padding: 4,
            lineHeight: 1,
          }}
          title="Close Walkthrough"
        >
          ✕
        </button>

        {/* Step Indicator Pills */}
        <div className="row gap" style={{ marginBottom: 16, gap: 6 }}>
          {steps.map((s, idx) => (
            <button
              key={s.id}
              onClick={() => setActiveStep(idx)}
              style={{
                flex: 1,
                height: 4,
                borderRadius: 2,
                border: "none",
                background:
                  idx === activeStep
                    ? "var(--cyan, #00d2ff)"
                    : idx < activeStep
                    ? "var(--neon, #00e699)"
                    : "rgba(255, 255, 255, 0.15)",
                cursor: "pointer",
                transition: "background 0.2s",
              }}
              title={s.badge}
            />
          ))}
        </div>

        {/* Header Badge & Icon */}
        <div className="row between" style={{ alignItems: "center", marginBottom: 12 }}>
          <span className="pill cyan" style={{ fontSize: 11, fontWeight: 700 }}>
            {current.badge}
          </span>
          <span style={{ fontSize: 12, color: "var(--text-2)" }}>
            Step {activeStep + 1} of {steps.length}
          </span>
        </div>

        {/* Title */}
        <div className="row gap" style={{ marginBottom: 8, alignItems: "center" }}>
          <div
            className="avatar-circle"
            style={{
              width: 44,
              height: 44,
              background: "rgba(0, 210, 255, 0.15)",
              border: "1px solid rgba(0, 210, 255, 0.3)",
              fontSize: 22,
            }}
          >
            {current.icon}
          </div>
          <div>
            <div style={{ fontSize: 19, fontWeight: 800, color: "#fff" }}>
              {current.title}
            </div>
            <div className="tiny" style={{ color: "var(--neon, #00e699)", fontWeight: 600 }}>
              {current.headline}
            </div>
          </div>
        </div>

        {/* Description */}
        <p style={{ fontSize: 13, lineHeight: 1.55, color: "var(--text-1)", marginTop: 10 }}>
          {current.content}
        </p>

        {/* Key Feature Bullets */}
        <div
          style={{
            background: "rgba(255, 255, 255, 0.03)",
            border: "1px solid rgba(255, 255, 255, 0.08)",
            borderRadius: 10,
            padding: "12px 14px",
            marginTop: 14,
            marginBottom: 16,
          }}
        >
          <div className="tiny bold" style={{ color: "var(--cyan)", marginBottom: 8, textTransform: "uppercase", letterSpacing: "0.5px" }}>
            Key Highlights:
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {current.bullets.map((bullet, i) => (
              <div key={i} className="row gap" style={{ alignItems: "flex-start", gap: 8 }}>
                <span style={{ color: "var(--neon)", fontSize: 14, lineHeight: 1.2 }}>✓</span>
                <span style={{ fontSize: 12, color: "var(--text-2)", lineHeight: 1.4 }}>
                  {bullet}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Navigation & Action Footer */}
        <div className="row between" style={{ gap: 10, marginTop: 8 }}>
          {activeStep > 0 ? (
            <button
              className="btn ghost"
              style={{ fontSize: 12, padding: "8px 14px" }}
              onClick={() => setActiveStep((p) => p - 1)}
            >
              ← Back
            </button>
          ) : (
            <button
              className="btn ghost"
              style={{ fontSize: 12, padding: "8px 14px" }}
              onClick={onClose}
            >
              Skip
            </button>
          )}

          <div className="row gap" style={{ gap: 8 }}>
            {current.route && onNavigate && (
              <button
                className="btn purple"
                style={{ fontSize: 12, padding: "8px 14px" }}
                onClick={() => {
                  onClose();
                  onNavigate(current.route!);
                }}
              >
                {current.routeLabel} ↗
              </button>
            )}

            <button
              className="btn cyan"
              style={{ fontSize: 12, padding: "8px 18px", fontWeight: 700 }}
              onClick={handleNext}
            >
              {current.actionText}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
