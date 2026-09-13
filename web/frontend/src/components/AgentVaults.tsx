import React, { useState } from "react";
import { TokenLogo } from "./TokenLogo";

interface AgentVaultsProps {
  wallet: string;
}

export const AgentVaults: React.FC<AgentVaultsProps> = ({ wallet }) => {
  const [activeTab, setActiveTab] = useState<"vaults" | "erc6551">("vaults");
  const [depositVault, setDepositVault] = useState<any>(null);
  const [depositAmount, setDepositAmount] = useState("100.0");
  const [txSuccess, setTxSuccess] = useState<string | null>(null);

  const vaults = [
    {
      id: "aerodrome_agl_eth",
      name: "Aerodrome v3 AGL/ETH Auto-Vault",
      sym1: "AGL",
      sym2: "ETH",
      protocol: "Aerodrome Finance v3",
      apy: 84.5,
      tvlUsd: 1420000,
      burnedAgl: 42500,
      userStaked: 650.0
    },
    {
      id: "aerodrome_agl_usdc",
      name: "AGL/USDC High Yield Pool",
      sym1: "AGL",
      sym2: "USDC",
      protocol: "Aerodrome Concentrated",
      apy: 62.8,
      tvlUsd: 890000,
      burnedAgl: 28100,
      userStaked: 0
    },
    {
      id: "veaero_bribe_vault",
      name: "veAERO Voting Bribe & Burn Vault",
      sym1: "WAGL",
      sym2: "AERO",
      protocol: "Base Bribe Protocol",
      apy: 112.4,
      tvlUsd: 2150000,
      burnedAgl: 115000,
      userStaked: 250.0
    }
  ];

  const agentAccount = {
    agentId: "#6551-AGL-8842",
    agentName: "Base Autonomous Arbitrage Agent",
    smartAccountAddress: "0x6551D034E94465Db1669f80D817c66e58cF194d027",
    aglBalance: 850.0,
    ethBalance: 0.15,
    activeStrategy: "DEX Triangular Arbitrage & Liquidity Rebalancing",
    totalTrades: 142,
    winRate: 94.2
  };

  const handleDeposit = () => {
    setTxSuccess(`Successfully deposited ${depositAmount} into ${depositVault.name}!`);
    setTimeout(() => {
      setDepositVault(null);
      setTxSuccess(null);
    }, 2500);
  };

  return (
    <div className="card card-elev" style={{ marginTop: 14 }}>
      {/* Header */}
      <div className="row between" style={{ marginBottom: 12 }}>
        <div className="row gap">
          <span style={{ fontSize: 20 }}>🏦</span>
          <div>
            <div className="bold">Yield Vaults & ERC-6551</div>
            <div className="tiny muted">Aerodrome v3 Auto-Vaults & Agent Smart Wallets</div>
          </div>
        </div>

        <div className="row gap" style={{ background: "var(--surface)", padding: 3, borderRadius: 8 }}>
          <button
            className={`pill ${activeTab === "vaults" ? "cyan" : ""}`}
            style={{ cursor: "pointer", border: "none", fontSize: 11, padding: "4px 10px" }}
            onClick={() => setActiveTab("vaults")}
          >
            Vaults
          </button>
          <button
            className={`pill ${activeTab === "erc6551" ? "cyan" : ""}`}
            style={{ cursor: "pointer", border: "none", fontSize: 11, padding: "4px 10px" }}
            onClick={() => setActiveTab("erc6551")}
          >
            6551 Wallet
          </button>
        </div>
      </div>

      {activeTab === "vaults" ? (
        <>
          {/* Deflationary Burn Banner */}
          <div
            style={{
              background: "linear-gradient(135deg, #3B0000, #1E0A00)",
              border: "1px solid rgba(255, 77, 106, 0.5)",
              borderRadius: 12,
              padding: 12,
              marginBottom: 14
            }}
            className="row between"
          >
            <div className="row gap">
              <span style={{ fontSize: 22 }}>🔥</span>
              <div>
                <div className="tiny bold" style={{ color: "#FF4D6A" }}>
                  Deflationary Buyback & Burn Engine
                </div>
                <div className="mono bold" style={{ fontSize: 13, color: "#FFFFFF" }}>
                  185,600 AGL ($157,760) Permanently Burned
                </div>
              </div>
            </div>
            <span className="pill red" style={{ fontSize: 10 }}>
              -7.4% Supply
            </span>
          </div>

          {/* Vaults List */}
          <div className="col gap">
            {vaults.map((v) => (
              <div
                key={v.id}
                style={{
                  background: "var(--card-bg)",
                  border: "1px solid var(--border)",
                  borderRadius: 10,
                  padding: 12
                }}
              >
                <div className="row between" style={{ marginBottom: 8 }}>
                  <div className="row gap">
                    <div className="row" style={{ marginRight: 4 }}>
                      <TokenLogo symbol={v.sym1} size={24} />
                      <div style={{ marginLeft: -8 }}>
                        <TokenLogo symbol={v.sym2} size={24} />
                      </div>
                    </div>
                    <div>
                      <div className="bold small">{v.name}</div>
                      <div className="tiny muted">{v.protocol}</div>
                    </div>
                  </div>
                  <span className="pill green" style={{ fontSize: 11, fontWeight: "bold" }}>
                    {v.apy}% APY
                  </span>
                </div>

                <div className="row between" style={{ alignItems: "center" }}>
                  <div className="col">
                    <span className="tiny muted">Pool TVL</span>
                    <span className="mono bold tiny">${v.tvlUsd.toLocaleString()}</span>
                  </div>
                  <div className="col">
                    <span className="tiny muted">Auto Burned</span>
                    <span className="mono bold tiny" style={{ color: "#FF4D6A" }}>
                      {v.burnedAgl.toLocaleString()} AGL
                    </span>
                  </div>
                  <button
                    className="btn cyan"
                    style={{ fontSize: 11, padding: "4px 12px", height: "auto" }}
                    onClick={() => setDepositVault(v)}
                  >
                    Deposit
                  </button>
                </div>
              </div>
            ))}
          </div>
        </>
      ) : (
        /* ERC-6551 Agent Account View */
        <div style={{ background: "var(--card-bg)", border: "1px solid var(--border)", borderRadius: 12, padding: 14 }}>
          <div className="row between" style={{ marginBottom: 10 }}>
            <div className="row gap">
              <TokenLogo symbol="AGL" size={28} />
              <div>
                <div className="bold small">{agentAccount.agentName}</div>
                <div className="tiny cyan bold">{agentAccount.agentId}</div>
              </div>
            </div>
            <span className="pill green" style={{ fontSize: 10 }}>
              6551 Token Bound Wallet
            </span>
          </div>

          <div
            className="row between"
            style={{
              background: "var(--surface)",
              padding: "6px 10px",
              borderRadius: 8,
              fontSize: 11,
              fontFamily: "monospace",
              marginBottom: 12
            }}
          >
            <span className="muted">Contract: {agentAccount.smartAccountAddress.slice(0, 12)}...{agentAccount.smartAccountAddress.slice(-6)}</span>
            <span
              style={{ color: "#00E6FF", cursor: "pointer" }}
              onClick={() => navigator.clipboard.writeText(agentAccount.smartAccountAddress)}
            >
              📋 Copy
            </span>
          </div>

          <div className="row gap" style={{ marginBottom: 12 }}>
            <div className="col card" style={{ flex: 1, padding: 10, background: "var(--surface)" }}>
              <span className="tiny muted">Agent AGL Balance</span>
              <span className="mono bold small">{agentAccount.aglBalance} AGL</span>
            </div>
            <div className="col card" style={{ flex: 1, padding: 10, background: "var(--surface)" }}>
              <span className="tiny muted">Agent ETH Reserve</span>
              <span className="mono bold small">{agentAccount.ethBalance} ETH</span>
            </div>
          </div>

          <div className="row between" style={{ fontSize: 11, marginBottom: 12 }}>
            <div>
              <span className="tiny muted">Strategy: </span>
              <span className="bold">{agentAccount.activeStrategy}</span>
            </div>
            <div>
              <span className="tiny muted">Win Rate: </span>
              <span className="bold green">{agentAccount.winRate}%</span>
            </div>
          </div>

          <button
            className="btn blue"
            style={{ width: "100%", fontSize: 12 }}
            onClick={() => alert(`Funded 6551 Agent Account ${agentAccount.smartAccountAddress}`)}
          >
            💳 Deposit Funds to Agent ERC-6551 Wallet
          </button>
        </div>
      )}

      {/* Deposit Modal */}
      {depositVault && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: "rgba(0,0,0,0.75)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 1000,
            padding: 16
          }}
        >
          <div className="card card-elev" style={{ width: "100%", maxWidth: 420, padding: 20 }}>
            <div className="bold" style={{ fontSize: 16, marginBottom: 4 }}>
              Deposit into Vault
            </div>
            <div className="tiny cyan" style={{ marginBottom: 14 }}>
              {depositVault.name} ({depositVault.apy}% APY)
            </div>

            {txSuccess ? (
              <div className="pill green" style={{ width: "100%", textAlign: "center", padding: 10 }}>
                {txSuccess}
              </div>
            ) : (
              <>
                <label className="tiny muted" style={{ display: "block", marginBottom: 6 }}>
                  Deposit Amount ({depositVault.sym1})
                </label>
                <input
                  type="text"
                  className="input"
                  value={depositAmount}
                  onChange={(e) => setDepositAmount(e.target.value)}
                  style={{ width: "100%", marginBottom: 16 }}
                />

                <div className="row gap">
                  <button className="btn outline" style={{ flex: 1 }} onClick={() => setDepositVault(null)}>
                    Cancel
                  </button>
                  <button className="btn cyan" style={{ flex: 1 }} onClick={handleDeposit}>
                    Confirm Deposit
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
