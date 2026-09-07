import { useState, useEffect } from "react";

interface Step {
  id: number;
  title: string;
  category: string;
  duration: string;
  icon: string;
  summary: string;
  concepts: { heading: string; detail: string }[];
  protocolExample: string;
  quiz: {
    question: string;
    options: string[];
    correctIndex: number;
    explanation: string;
  };
}

const STEPS: Step[] = [
  {
    id: 1,
    title: "Connecting to Base Mainnet",
    category: "Network Basics",
    duration: "2 min",
    icon: "🔵",
    summary: "Base is an Ethereum Layer 2 (L2) developed on the OP Stack. To interact with Base decentralized apps (dApps), your wallet must be configured with Base network parameters.",
    concepts: [
      {
        heading: "Chain ID & RPC Endpoint",
        detail: "Base Mainnet operates under Chain ID 8453. Remote Procedure Call (RPC) nodes route your transactions from your wallet directly to Base sequencers.",
      },
      {
        heading: "Native Gas Currency",
        detail: "Unlike some L2s that use custom governance tokens for gas, Base uses native ETH to pay transaction fees, maintaining seamless Ethereum compatibility.",
      },
      {
        heading: "Non-Custodial Architecture",
        detail: "In non-custodial or watch-only mode, your private keys remain exclusively in your possession. Protocols only read your public address.",
      },
    ],
    protocolExample: "Base RPC: https://mainnet.base.org | Chain ID: 8453 | Currency: ETH",
    quiz: {
      question: "What is the native gas currency used to pay transaction fees on Base?",
      options: ["USDC", "BaseCoin", "ETH", "AGL"],
      correctIndex: 2,
      explanation: "Base is an Ethereum Layer 2 and uses native ETH for all transaction and execution gas fees.",
    },
  },
  {
    id: 2,
    title: "Gas Fees & Optimistic Rollups",
    category: "Execution & Cost",
    duration: "3 min",
    icon: "⚡",
    summary: "Understand how Base achieves sub-cent transaction fees through Optimistic Rollups and Ethereum EIP-4844 blob data.",
    concepts: [
      {
        heading: "Off-Chain Execution with L1 Settlement",
        detail: "Base executes transactions off the main Ethereum chain at high throughput, bundles thousands of them into batches, and posts cryptographic state roots to Ethereum L1.",
      },
      {
        heading: "EIP-4844 Data Blobs",
        detail: "Since the Ethereum Dencun upgrade, Base posts transaction data using temporary 'blobs' instead of costly calldata, cutting gas fees by over 90%.",
      },
      {
        heading: "Two-Part Fee Structure",
        detail: "A Base transaction fee consists of: (1) Execution Fee on L2 + (2) L1 Data Availability Fee to publish the compressed batch.",
      },
    ],
    protocolExample: "Typical Swap Fee: Ethereum L1 ~$4.50 vs Base L2 ~$0.008",
    quiz: {
      question: "Why are transaction fees on Base a fraction of a cent compared to Ethereum L1?",
      options: [
        "Transactions are free because validators don't get paid",
        "Thousands of transactions are bundled into compressed batches using EIP-4844 blobs",
        "Base does not settle on Ethereum",
        "Transactions are processed once a week",
      ],
      correctIndex: 1,
      explanation: "Base executes off-chain and bundles thousands of compressed transactions into EIP-4844 blobs posted to Ethereum L1.",
    },
  },
  {
    id: 3,
    title: "Token Approvals & Smart Allowances",
    category: "DeFi Security",
    duration: "4 min",
    icon: "🔐",
    summary: "Before any decentralized protocol can trade or stake your ERC-20 tokens (like USDC or AGL), you must grant an approval. Managing allowances is critical for security.",
    concepts: [
      {
        heading: "Transfer vs Approve",
        detail: "transfer() directly moves tokens from your wallet. approve() delegates permission for a third-party smart contract (like a DEX router) to spend up to a specific amount.",
      },
      {
        heading: "The Danger of Unlimited Approvals",
        detail: "Many dApps prompt for 'infinite approval' (2^256 - 1) for convenience. If that smart contract gets exploited later, an attacker could drain all approved tokens.",
      },
      {
        heading: "Best Practice: Exact Allowances",
        detail: "Always approve only the exact token amount needed for your trade. Regularly revoke stale approvals using tools like Revoke.cash or the AGL Security tab.",
      },
    ],
    protocolExample: "AGL Token Contract: 0xEA1221B4d80A89BD8C75248Fae7c176BD1854698",
    quiz: {
      question: "What is the primary risk of granting 'unlimited / infinite' token approval to a dApp?",
      options: [
        "Your transaction will fail with an out-of-gas error",
        "The dApp will charge higher gas fees",
        "If that contract is exploited in the future, your approved tokens could be drained",
        "Your wallet address will be deleted",
      ],
      correctIndex: 2,
      explanation: "An infinite allowance allows the approved contract to pull tokens at any time. If that contract has a vulnerability, attackers could drain your funds.",
    },
  },
  {
    id: 4,
    title: "Decentralized Exchanges (DEXs) & AMMs",
    category: "Protocols",
    duration: "4 min",
    icon: "🔄",
    summary: "Learn how Automated Market Makers like Aerodrome and Uniswap execute swaps without centralized order books using liquidity pools on Base.",
    concepts: [
      {
        heading: "Constant Product AMM (x * y = k)",
        detail: "Instead of matching buyers and sellers, liquidity pools hold reserves of paired tokens. Trades alter the reserve ratio, automatically determining the exchange rate.",
      },
      {
        heading: "Slippage Tolerance",
        detail: "Slippage is the difference between the expected price and executed price due to pool depth and market movements. Setting a reasonable slippage (e.g. 0.5%) protects against front-running MEV bots.",
      },
      {
        heading: "Aerodrome on Base",
        detail: "Aerodrome is the primary liquidity hub on Base, featuring low-slippage trades, concentrated liquidity, and veAERO vote-directed emissions.",
      },
    ],
    protocolExample: "Aerodrome Router on Base: 0xcF664087a5bB0237a0BAd6742852ec6c8d58504e",
    quiz: {
      question: "What does setting a '0.5% slippage tolerance' mean on a DEX swap?",
      options: [
        "You pay a mandatory 0.5% tip to the developer",
        "Your swap will revert if the final received amount is more than 0.5% worse than quoted",
        "The trade is delayed by 0.5 seconds",
        "You will receive 0.5% bonus tokens",
      ],
      correctIndex: 1,
      explanation: "Slippage tolerance safeguards your trade: if price moves adversely by more than 0.5% before execution, the transaction automatically reverts.",
    },
  },
  {
    id: 5,
    title: "Liquidity Provision & Yield Staking",
    category: "DeFi Yield",
    duration: "4 min",
    icon: "📈",
    summary: "Discover how Liquidity Providers (LPs) earn swap fees, and how staking tokens like AGL generates governance power (wAGL) and yields on Base.",
    concepts: [
      {
        heading: "Liquidity Pool Tokens (LP Tokens)",
        detail: "When you deposit paired tokens (e.g. AGL + ETH) into a liquidity pool, you receive LP tokens representing your proportional share of the pool's reserves.",
      },
      {
        heading: "Fee APR & Rewards",
        detail: "Every trade across that pool pays a swap fee (e.g. 0.05% - 0.30%) distributed to LPs. Additionally, protocols offer liquidity mining incentives.",
      },
      {
        heading: "Impermanent Loss Awareness",
        detail: "If the relative price of paired tokens diverges sharply after deposit, holding the tokens individually may have yielded more than the pool—known as impermanent loss.",
      },
    ],
    protocolExample: "wAGL Governance Staking Contract: 0x356AbeDE92d53D9Fe5165d21A2eEB6c321CEa7b4",
    quiz: {
      question: "How do Liquidity Providers (LPs) earn yield in decentralized pools?",
      options: [
        "From government subsidies",
        "By collecting a portion of trading fees paid by swappers and protocol incentives",
        "By borrowing funds from centralized banks",
        "Through fixed interest paid by miners",
      ],
      correctIndex: 1,
      explanation: "LPs earn a proportional share of trading fees generated by users swapping through the pool, alongside protocol rewards.",
    },
  },
  {
    id: 6,
    title: "Security Hygiene & AI Contract Audits",
    category: "Safety Masterclass",
    duration: "3 min",
    icon: "🛡️",
    summary: "Master essential security habits on Base: verifying contract source code, spotting honeypots, and leveraging the AGL AI Agent for pre-flight security audits.",
    concepts: [
      {
        heading: "Basescan Verification",
        detail: "Always ensure the protocol's contract source code is verified and public on Basescan. Unverified bytecode may conceal malicious drainers or unverified mint functions.",
      },
      {
        heading: "Phishing & Fake Tokens",
        detail: "Anyone can deploy a token named 'USDC' or 'AGL' on Base. Always verify the exact contract address from trusted documentation or the AGL registry.",
      },
      {
        heading: "Pre-Flight AI Security Audits",
        detail: "Use the AI Agent's Audit tab to scan target contracts for reentrancy bugs, centralized ownership keys, fee changes, or hidden blacklist functions before interacting.",
      },
    ],
    protocolExample: "AGL Super Agent AI Auditor: Real-time static analysis & Gemini-grounded heuristic scans.",
    quiz: {
      question: "What is the best way to ensure you are interacting with the genuine token or protocol on Base?",
      options: [
        "Look for the coolest logo in a telegram channel",
        "Search by name only on social media",
        "Verify the exact contract hexadecimal address against official documentation or trusted registries",
        "Assume all tokens with the same name are identical",
      ],
      correctIndex: 2,
      explanation: "Because token names are non-unique on EVM blockchains, verifying the exact contract address is the only way to avoid counterfeit tokens.",
    },
  },
];

export function Web3LearningModule({ onJumpToAudit }: { onJumpToAudit?: () => void }) {
  const [currentStepIndex, setCurrentStepIndex] = useState(0);
  const [completedSteps, setCompletedSteps] = useState<number[]>(() => {
    try {
      const saved = localStorage.getItem("agl_web3_learn_completed");
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  // Quiz state
  const [selectedAnswer, setSelectedAnswer] = useState<number | null>(null);
  const [quizSubmitted, setQuizSubmitted] = useState(false);

  // Interactive Simulator states
  const [simNetConnected, setSimNetConnected] = useState(false);
  const [simEthAmount, setSimEthAmount] = useState("0.05");
  const [simSlippage, setSimSlippage] = useState("0.5");
  const [simApprovalType, setSimApprovalType] = useState<"exact" | "infinite">("exact");
  const [simApprovalDone, setSimApprovalDone] = useState(false);
  const [simSwapDone, setSimSwapDone] = useState(false);
  const [simScanning, setSimScanning] = useState(false);
  const [simScanResult, setSimScanResult] = useState<string | null>(null);

  const step = STEPS[currentStepIndex];
  const isCompleted = completedSteps.includes(step.id);
  const totalXp = completedSteps.length * 50;
  const progressPercent = Math.round((completedSteps.length / STEPS.length) * 100);

  useEffect(() => {
    try {
      localStorage.setItem("agl_web3_learn_completed", JSON.stringify(completedSteps));
    } catch {
      // ignore
    }
  }, [completedSteps]);

  // Reset quiz state on step change
  const handleSelectStep = (idx: number) => {
    setCurrentStepIndex(idx);
    setSelectedAnswer(null);
    setQuizSubmitted(false);
  };

  const handleAnswerSubmit = (idx: number) => {
    setSelectedAnswer(idx);
    setQuizSubmitted(true);
    if (idx === step.quiz.correctIndex && !completedSteps.includes(step.id)) {
      setCompletedSteps((prev) => [...prev, step.id]);
    }
  };

  const handleNext = () => {
    if (currentStepIndex < STEPS.length - 1) {
      handleSelectStep(currentStepIndex + 1);
    }
  };

  const handlePrev = () => {
    if (currentStepIndex > 0) {
      handleSelectStep(currentStepIndex - 1);
    }
  };

  const handleResetProgress = () => {
    if (window.confirm("Reset your Web3 Learning progress and start over?")) {
      setCompletedSteps([]);
      setSelectedAnswer(null);
      setQuizSubmitted(false);
      setCurrentStepIndex(0);
      try {
        localStorage.removeItem("agl_web3_learn_completed");
      } catch {
        // ignore
      }
    }
  };

  // Simulation handlers
  const handleSimulateApproval = () => {
    setSimApprovalDone(true);
    setTimeout(() => {
      if (!completedSteps.includes(step.id)) {
        setCompletedSteps((prev) => [...prev, step.id]);
      }
    }, 600);
  };

  const handleSimulateSwap = () => {
    setSimSwapDone(true);
  };

  const handleSimulateScan = () => {
    setSimScanning(true);
    setSimScanResult(null);
    setTimeout(() => {
      setSimScanning(false);
      setSimScanResult("✅ Contract Verified on Basescan • No Malicious Reentrancy • Safe Allowance Policy Detected");
    }, 1200);
  };

  return (
    <div className="col gap" style={{ gap: 14 }}>
      {/* Module Hero Banner */}
      <div
        className="card card-elev"
        style={{
          background: "linear-gradient(135deg, rgba(0,82,255,0.18) 0%, rgba(0,212,255,0.12) 100%)",
          borderColor: "rgba(0,212,255,0.4)",
          padding: 18,
        }}
      >
        <div className="row between" style={{ alignItems: "flex-start", marginBottom: 8 }}>
          <div>
            <div className="row gap" style={{ gap: 8, marginBottom: 4 }}>
              <span style={{ fontSize: 24 }}>🎓</span>
              <h2 style={{ fontSize: 18, fontWeight: 800, color: "var(--text)" }}>
                Web3 Learning Academy
              </h2>
              <span className="pill cyan">Base Protocol 101</span>
            </div>
            <p className="small muted" style={{ maxWidth: 540 }}>
              Master decentralized finance on Base. Interactive, step-by-step guides with hands-on
              transaction sandboxes and verified protocol best practices.
            </p>
          </div>
          <div className="col" style={{ alignItems: "flex-end" }}>
            <span className="pill gold" style={{ fontSize: 12 }}>⚡ {totalXp} / 300 XP</span>
            <span className="tiny muted" style={{ marginTop: 4 }}>{completedSteps.length} of {STEPS.length} Completed</span>
          </div>
        </div>

        {/* Progress Bar */}
        <div className="progress-track" style={{ height: 6, marginTop: 6, marginBottom: 8 }}>
          <div
            className="progress-fill"
            style={{
              width: `${progressPercent}%`,
              transition: "width 0.4s ease",
            }}
          />
        </div>

        {/* Step Selector Pills */}
        <div
          className="row gap"
          style={{
            overflowX: "auto",
            paddingBottom: 4,
            gap: 6,
            marginTop: 4,
          }}
        >
          {STEPS.map((s, idx) => {
            const isDone = completedSteps.includes(s.id);
            const isCurrent = idx === currentStepIndex;
            return (
              <button
                key={s.id}
                onClick={() => handleSelectStep(idx)}
                className="tab"
                style={{
                  fontSize: 11,
                  padding: "6px 10px",
                  display: "flex",
                  alignItems: "center",
                  gap: 5,
                  background: isCurrent
                    ? "var(--blue)"
                    : isDone
                    ? "rgba(0,230,153,0.12)"
                    : "var(--card)",
                  color: isCurrent ? "#fff" : isDone ? "var(--neon)" : "var(--text-2)",
                  borderColor: isCurrent
                    ? "var(--cyan)"
                    : isDone
                    ? "rgba(0,230,153,0.3)"
                    : "var(--border)",
                }}
              >
                <span>{isDone ? "✓" : s.id}</span>
                <span>{s.title.split(" ")[0]}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Main Active Step Card */}
      <div className="card" style={{ borderColor: isCompleted ? "rgba(0,230,153,0.4)" : "var(--border)" }}>
        {/* Step Header */}
        <div className="row between" style={{ marginBottom: 12, flexWrap: "wrap", gap: 8 }}>
          <div className="row gap" style={{ gap: 8 }}>
            <span style={{ fontSize: 24 }}>{step.icon}</span>
            <div>
              <div className="row gap" style={{ gap: 6 }}>
                <span className="tiny bold" style={{ color: "var(--cyan)", textTransform: "uppercase" }}>
                  Step {step.id} of {STEPS.length} • {step.category}
                </span>
                {isCompleted && <span className="pill green" style={{ fontSize: 10 }}>Completed</span>}
              </div>
              <h3 style={{ fontSize: 16, fontWeight: 800 }}>{step.title}</h3>
            </div>
          </div>
          <div className="row gap" style={{ gap: 8 }}>
            <span className="tiny muted">⏱️ {step.duration}</span>
            <span className="pill purple" style={{ fontSize: 11 }}>+50 XP</span>
          </div>
        </div>

        {/* Step Summary Box */}
        <div
          style={{
            background: "rgba(13,20,38,0.7)",
            border: "1px solid var(--border)",
            borderRadius: 12,
            padding: 12,
            marginBottom: 16,
            fontSize: 13,
            lineHeight: 1.5,
          }}
        >
          {step.summary}
        </div>

        {/* Core Concepts Breakdown */}
        <div style={{ marginBottom: 16 }}>
          <div className="small bold muted" style={{ marginBottom: 8, textTransform: "uppercase" }}>
            📖 Key Architectural Concepts
          </div>
          <div className="col gap" style={{ gap: 10 }}>
            {step.concepts.map((c, i) => (
              <div
                key={i}
                style={{
                  background: "var(--surface)",
                  borderRadius: 10,
                  padding: 12,
                  border: "1px solid var(--border)",
                }}
              >
                <div className="bold small" style={{ color: "var(--cyan)", marginBottom: 4 }}>
                  {i + 1}. {c.heading}
                </div>
                <div className="tiny" style={{ color: "var(--text-2)", lineHeight: 1.5 }}>
                  {c.detail}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Protocol Spec Box */}
        <div
          style={{
            background: "rgba(0, 82, 255, 0.08)",
            border: "1px dashed rgba(0, 212, 255, 0.4)",
            borderRadius: 10,
            padding: "8px 12px",
            marginBottom: 18,
            fontSize: 11,
            fontFamily: '"JetBrains Mono", ui-monospace, monospace',
            color: "var(--cyan)",
            wordBreak: "break-all",
          }}
        >
          <strong>Base Reference:</strong> {step.protocolExample}
        </div>

        {/* Hands-On Interactive Simulator (Sandbox) */}
        <div
          style={{
            background: "var(--card-elev)",
            borderRadius: 14,
            border: "1px solid rgba(0,212,255,0.3)",
            padding: 14,
            marginBottom: 18,
          }}
        >
          <div className="row between" style={{ marginBottom: 10 }}>
            <div className="row gap" style={{ gap: 6 }}>
              <span style={{ fontSize: 16 }}>🛠️</span>
              <span className="small bold" style={{ color: "var(--cyan)" }}>
                Interactive Protocol Sandbox
              </span>
            </div>
            <span className="tiny muted">Simulated on Base Mainnet</span>
          </div>

          {/* Sandbox for Step 1: Network Switch */}
          {step.id === 1 && (
            <div className="col gap" style={{ gap: 8 }}>
              <div className="tiny muted">Test your wallet's capability to identify and link to Base RPC parameters:</div>
              <div className="row between" style={{ background: "var(--surface)", padding: "8px 12px", borderRadius: 8 }}>
                <span className="tiny">Target Network</span>
                <span className="tiny bold" style={{ color: "var(--cyan)" }}>Base Mainnet (Chain ID 8453)</span>
              </div>
              <div className="row between" style={{ background: "var(--surface)", padding: "8px 12px", borderRadius: 8 }}>
                <span className="tiny">Connection State</span>
                <span className="tiny bold" style={{ color: simNetConnected ? "var(--neon)" : "var(--amber)" }}>
                  {simNetConnected ? "🟢 Connected to Base Sequencer" : "⚪ Standby"}
                </span>
              </div>
              <button
                className="btn cyan"
                style={{ fontSize: 12, padding: "8px 14px", marginTop: 4 }}
                onClick={() => setSimNetConnected(!simNetConnected)}
              >
                {simNetConnected ? "Disconnect Simulator" : "Simulate Wallet Switch to Base →"}
              </button>
            </div>
          )}

          {/* Sandbox for Step 2: Gas Comparison */}
          {step.id === 2 && (
            <div className="col gap" style={{ gap: 10 }}>
              <div className="tiny muted">Compare live execution cost for a 150,000 gas Uniswap swap on Ethereum L1 vs Base L2:</div>
              <div className="metrics" style={{ gap: 8 }}>
                <div style={{ background: "var(--surface)", padding: 10, borderRadius: 8, textAlign: "center" }}>
                  <div className="tiny muted">Ethereum L1 (30 Gwei)</div>
                  <div style={{ fontSize: 16, fontWeight: 800, color: "var(--rose)", marginTop: 4 }}>~$4.50</div>
                  <div className="tiny muted">High execution overhead</div>
                </div>
                <div style={{ background: "var(--surface)", padding: 10, borderRadius: 8, textAlign: "center", border: "1px solid rgba(0,230,153,0.3)" }}>
                  <div className="tiny muted">Base L2 (EIP-4844)</div>
                  <div style={{ fontSize: 16, fontWeight: 800, color: "var(--neon)", marginTop: 4 }}>~$0.008</div>
                  <div className="tiny" style={{ color: "var(--neon)" }}>99.8% Cost Savings</div>
                </div>
              </div>
            </div>
          )}

          {/* Sandbox for Step 3: Allowance Policy */}
          {step.id === 3 && (
            <div className="col gap" style={{ gap: 10 }}>
              <div className="tiny muted">Select your ERC-20 approval policy before interacting with an Aerodrome router:</div>
              <div className="row gap" style={{ gap: 8 }}>
                <button
                  className={`tab ${simApprovalType === "exact" ? "active" : ""}`}
                  style={{ flex: 1, fontSize: 12 }}
                  onClick={() => { setSimApprovalType("exact"); setSimApprovalDone(false); }}
                >
                  🛡️ Exact Amount (100 AGL)
                </button>
                <button
                  className={`tab ${simApprovalType === "infinite" ? "active" : ""}`}
                  style={{ flex: 1, fontSize: 12 }}
                  onClick={() => { setSimApprovalType("infinite"); setSimApprovalDone(false); }}
                >
                  ⚠️ Infinite (2^256 - 1)
                </button>
              </div>
              <div
                style={{
                  background: simApprovalType === "exact" ? "rgba(0,230,153,0.08)" : "rgba(255,51,102,0.08)",
                  border: `1px solid ${simApprovalType === "exact" ? "rgba(0,230,153,0.3)" : "rgba(255,51,102,0.3)"}`,
                  borderRadius: 8,
                  padding: 10,
                  fontSize: 12,
                }}
              >
                {simApprovalType === "exact" ? (
                  <span style={{ color: "var(--neon)" }}>
                    ✓ <strong>Safe Policy:</strong> The contract can only spend 100 AGL for this specific transaction. Your remaining balance is never exposed.
                  </span>
                ) : (
                  <span style={{ color: "var(--rose)" }}>
                    ⚠️ <strong>High Risk:</strong> The protocol contract can transfer any future AGL balance in your wallet at any time without further confirmation.
                  </span>
                )}
              </div>
              <button
                className="btn"
                style={{ fontSize: 12, padding: "8px 14px" }}
                disabled={simApprovalDone}
                onClick={handleSimulateApproval}
              >
                {simApprovalDone ? "✓ Allowance Granted (Simulated)" : "Simulate approve(spender, amount)"}
              </button>
            </div>
          )}

          {/* Sandbox for Step 4: DEX AMM Swap */}
          {step.id === 4 && (
            <div className="col gap" style={{ gap: 8 }}>
              <div className="tiny muted">Simulate a decentralized token swap on Aerodrome Base:</div>
              <div className="row gap" style={{ gap: 8 }}>
                <div style={{ flex: 1 }}>
                  <div className="tiny muted" style={{ marginBottom: 4 }}>You Pay (ETH)</div>
                  <input
                    className="input"
                    type="number"
                    value={simEthAmount}
                    onChange={(e) => { setSimEthAmount(e.target.value); setSimSwapDone(false); }}
                    style={{ fontSize: 13, padding: "8px 10px" }}
                  />
                </div>
                <div style={{ flex: 1 }}>
                  <div className="tiny muted" style={{ marginBottom: 4 }}>You Receive (Est. AGL)</div>
                  <div
                    style={{
                      background: "var(--surface)",
                      border: "1px solid var(--border)",
                      borderRadius: 12,
                      padding: "8px 10px",
                      fontSize: 13,
                      fontWeight: 700,
                      color: "var(--cyan)",
                    }}
                  >
                    ~{(parseFloat(simEthAmount || "0") * 985.4).toFixed(2)} AGL
                  </div>
                </div>
              </div>

              <div className="row between" style={{ marginTop: 2 }}>
                <span className="tiny muted">Slippage Tolerance:</span>
                <div className="row gap" style={{ gap: 6 }}>
                  {["0.1", "0.5", "1.0"].map((s) => (
                    <button
                      key={s}
                      className="tab"
                      style={{
                        padding: "2px 8px",
                        fontSize: 11,
                        background: simSlippage === s ? "var(--blue)" : "var(--surface)",
                      }}
                      onClick={() => setSimSlippage(s)}
                    >
                      {s}%
                    </button>
                  ))}
                </div>
              </div>

              <div className="row between" style={{ background: "var(--surface)", padding: "6px 10px", borderRadius: 8, fontSize: 11 }}>
                <span className="muted">Est. Gas on Base</span>
                <span className="bold" style={{ color: "var(--neon)" }}>$0.0071 ETH</span>
              </div>

              <button
                className="btn cyan"
                style={{ fontSize: 12, padding: "8px 14px", marginTop: 4 }}
                onClick={handleSimulateSwap}
              >
                {simSwapDone ? "✓ Swap Confirmed on Base Block #1849204" : "Execute Test Swap →"}
              </button>
            </div>
          )}

          {/* Sandbox for Step 5: Liquidity Yield */}
          {step.id === 5 && (
            <div className="col gap" style={{ gap: 8 }}>
              <div className="tiny muted">Interactive Staking & Liquidity Vault Simulator:</div>
              <div className="metrics" style={{ gap: 8 }}>
                <div style={{ background: "var(--surface)", padding: 10, borderRadius: 8 }}>
                  <div className="tiny muted">AGL Staking APR</div>
                  <div style={{ fontSize: 17, fontWeight: 800, color: "var(--gold)", marginTop: 2 }}>18.4% APR</div>
                  <div className="tiny muted">Compounded weekly</div>
                </div>
                <div style={{ background: "var(--surface)", padding: 10, borderRadius: 8 }}>
                  <div className="tiny muted">Governance Power</div>
                  <div style={{ fontSize: 17, fontWeight: 800, color: "var(--purple)", marginTop: 2 }}>1 : 1 wAGL</div>
                  <div className="tiny muted">Base DAO voting rights</div>
                </div>
              </div>
              <div className="tiny" style={{ color: "var(--text-2)", background: "var(--surface)", padding: 8, borderRadius: 8 }}>
                💡 <em>Pro-tip:</em> Staking AGL locks tokens into ERC-20Votes wrapper (wAGL), allowing you to participate in on-chain governance while earning protocol rewards.
              </div>
            </div>
          )}

          {/* Sandbox for Step 6: Security Scanner */}
          {step.id === 6 && (
            <div className="col gap" style={{ gap: 8 }}>
              <div className="tiny muted">Simulate an AI Pre-flight Security Scan on an unknown contract:</div>
              <div className="row gap" style={{ gap: 6 }}>
                <input
                  className="input"
                  readOnly
                  value="0xEA1221B4d80A89BD8C75248Fae7c176BD1854698 (AGL Token)"
                  style={{ fontSize: 11, padding: "8px 10px" }}
                />
                <button
                  className="btn"
                  style={{ width: "auto", padding: "8px 14px", fontSize: 12, whiteSpace: "nowrap" }}
                  disabled={simScanning}
                  onClick={handleSimulateScan}
                >
                  {simScanning ? "Auditing…" : "🛡️ Scan"}
                </button>
              </div>
              {simScanResult && (
                <div
                  style={{
                    background: "rgba(0,230,153,0.1)",
                    border: "1px solid rgba(0,230,153,0.3)",
                    borderRadius: 8,
                    padding: 8,
                    fontSize: 11,
                    color: "var(--neon)",
                  }}
                >
                  {simScanResult}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Knowledge Check / Interactive Quiz */}
        <div
          style={{
            background: "var(--surface)",
            borderRadius: 14,
            border: "1px solid var(--border)",
            padding: 14,
            marginBottom: 16,
          }}
        >
          <div className="row gap" style={{ gap: 6, marginBottom: 8 }}>
            <span style={{ fontSize: 16 }}>🎯</span>
            <span className="small bold" style={{ color: "var(--text)" }}>
              Step {step.id} Knowledge Check
            </span>
          </div>

          <p className="small" style={{ marginBottom: 12, fontWeight: 600 }}>
            {step.quiz.question}
          </p>

          <div className="col gap" style={{ gap: 8, marginBottom: 12 }}>
            {step.quiz.options.map((opt, oIdx) => {
              const isChosen = selectedAnswer === oIdx;
              const isCorrect = oIdx === step.quiz.correctIndex;
              let btnBg = "var(--card)";
              let btnBorder = "var(--border)";
              let btnColor = "var(--text)";

              if (quizSubmitted) {
                if (isCorrect) {
                  btnBg = "rgba(0,230,153,0.15)";
                  btnBorder = "var(--neon)";
                  btnColor = "var(--neon)";
                } else if (isChosen) {
                  btnBg = "rgba(255,51,102,0.15)";
                  btnBorder = "var(--rose)";
                  btnColor = "var(--rose)";
                }
              } else if (isChosen) {
                btnBg = "rgba(0,82,255,0.2)";
                btnBorder = "var(--blue)";
              }

              return (
                <button
                  key={oIdx}
                  onClick={() => handleAnswerSubmit(oIdx)}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 10,
                    padding: "10px 14px",
                    borderRadius: 10,
                    background: btnBg,
                    border: `1px solid ${btnBorder}`,
                    color: btnColor,
                    fontSize: 13,
                    textAlign: "left",
                    cursor: "pointer",
                    transition: "all 0.15s ease",
                  }}
                >
                  <span
                    style={{
                      width: 20,
                      height: 20,
                      borderRadius: "50%",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      fontSize: 11,
                      fontWeight: 700,
                      background: "rgba(255,255,255,0.06)",
                    }}
                  >
                    {String.fromCharCode(65 + oIdx)}
                  </span>
                  <span style={{ flex: 1 }}>{opt}</span>
                  {quizSubmitted && isCorrect && <span>✅</span>}
                  {quizSubmitted && isChosen && !isCorrect && <span>❌</span>}
                </button>
              );
            })}
          </div>

          {quizSubmitted && (
            <div
              style={{
                background:
                  selectedAnswer === step.quiz.correctIndex
                    ? "rgba(0,230,153,0.1)"
                    : "rgba(255,176,32,0.1)",
                border: `1px solid ${
                  selectedAnswer === step.quiz.correctIndex
                    ? "rgba(0,230,153,0.3)"
                    : "rgba(255,176,32,0.3)"
                }`,
                borderRadius: 10,
                padding: 10,
                fontSize: 12,
                color:
                  selectedAnswer === step.quiz.correctIndex
                    ? "var(--neon)"
                    : "var(--amber)",
              }}
            >
              <strong>
                {selectedAnswer === step.quiz.correctIndex
                  ? "🎉 Correct! (+50 XP)"
                  : "💡 Hint & Explanation:"}
              </strong>{" "}
              {step.quiz.explanation}
            </div>
          )}
        </div>

        {/* Navigation Stepper Controls */}
        <div className="row between" style={{ marginTop: 8 }}>
          <button
            className="btn ghost"
            style={{ width: "auto", padding: "10px 18px", fontSize: 13 }}
            disabled={currentStepIndex === 0}
            onClick={handlePrev}
          >
            ← Previous
          </button>

          {currentStepIndex < STEPS.length - 1 ? (
            <button
              className="btn"
              style={{ width: "auto", padding: "10px 22px", fontSize: 13 }}
              onClick={handleNext}
            >
              Next Step ({currentStepIndex + 2}/{STEPS.length}) →
            </button>
          ) : (
            <button
              className="btn"
              style={{
                width: "auto",
                padding: "10px 22px",
                fontSize: 13,
                background: "linear-gradient(90deg, var(--neon), var(--cyan))",
                color: "#070B16",
              }}
              onClick={() => {
                if (onJumpToAudit) onJumpToAudit();
              }}
            >
              🛡️ Audit Real Contracts →
            </button>
          )}
        </div>
      </div>

      {/* Completion Trophy Card */}
      {completedSteps.length === STEPS.length && (
        <div
          className="card card-elev"
          style={{
            background: "linear-gradient(135deg, rgba(0,230,153,0.15) 0%, rgba(0,212,255,0.12) 100%)",
            borderColor: "var(--neon)",
            textAlign: "center",
            padding: 20,
          }}
        >
          <div style={{ fontSize: 36, marginBottom: 8 }}>🏆</div>
          <h3 style={{ fontSize: 18, fontWeight: 800, color: "var(--neon)", marginBottom: 4 }}>
            Base Web3 Academy Certified!
          </h3>
          <p className="small muted" style={{ maxWidth: 460, margin: "0 auto 14px" }}>
            You have mastered connecting to Base, EIP-4844 gas efficiency, exact allowance safety,
            DEX AMM swaps, liquidity provision, and AI contract auditing.
          </p>
          <div className="row gap" style={{ justifyContent: "center", gap: 10 }}>
            {onJumpToAudit && (
              <button
                className="btn"
                style={{ width: "auto", padding: "8px 18px", fontSize: 12 }}
                onClick={onJumpToAudit}
              >
                Launch AI Security Auditor
              </button>
            )}
            <button
              className="btn ghost"
              style={{ width: "auto", padding: "8px 18px", fontSize: 12 }}
              onClick={handleResetProgress}
            >
              Reset Tutorial
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
