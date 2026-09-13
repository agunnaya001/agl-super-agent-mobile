import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { TokenLogo } from "./TokenLogo";
import { fmtUsd, shortAddr } from "../ui";

export interface AssetPerformance {
  symbol: string;
  name: string;
  priceUsd: number;
  change24h: number;
  balance: number;
  iconEmoji: string;
  contractAddress?: string;
  isNative?: boolean;
}

interface DashboardQuickActionsProps {
  tokens?: any[];
  wallet: string;
  onNavigateWallet?: () => void;
}

const DEFAULT_TOP_ASSETS: AssetPerformance[] = [
  {
    symbol: "AGL",
    name: "Agunnaya Labs",
    priceUsd: 3.42,
    change24h: 8.65,
    balance: 420.5,
    iconEmoji: "🪙",
    contractAddress: "0x3845badb6b8b0e8957c5efc0576911c750e394f9",
  },
  {
    symbol: "ETH",
    name: "Ethereum (Base Native)",
    priceUsd: 2680.5,
    change24h: 3.12,
    balance: 1.452,
    iconEmoji: "Ξ",
    isNative: true,
  },
  {
    symbol: "wAGL",
    name: "Wrapped AGL Votes",
    priceUsd: 3.42,
    change24h: 8.65,
    balance: 150.0,
    iconEmoji: "🗳️",
  },
  {
    symbol: "USDC",
    name: "USD Coin (Base)",
    priceUsd: 1.0,
    change24h: 0.02,
    balance: 750.0,
    iconEmoji: "💵",
  },
];

export const DashboardQuickActions: React.FC<DashboardQuickActionsProps> = ({
  tokens,
  wallet,
  onNavigateWallet,
}) => {
  // Modal states
  const [activeModal, setActiveModal] = useState<"swap" | "bridge" | null>(null);
  const [selectedAsset, setSelectedAsset] = useState<AssetPerformance>(DEFAULT_TOP_ASSETS[0]);
  const [isFabOpen, setIsFabOpen] = useState(false);

  // Derive top assets from passed tokens or fallback
  const topAssets: AssetPerformance[] = React.useMemo(() => {
    if (tokens && tokens.length > 0) {
      // Map and sort by change24h descending
      const mapped = tokens.map((t) => ({
        symbol: t.symbol || "TOKEN",
        name: t.name || t.symbol,
        priceUsd: t.priceUsd || (t.symbol === "AGL" ? 3.42 : t.symbol === "ETH" ? 2680 : 1.0),
        change24h: t.change24h ?? (t.symbol === "AGL" ? 8.65 : t.symbol === "ETH" ? 3.12 : 0.05),
        balance: t.balance ?? 0,
        iconEmoji: t.iconEmoji || "🪙",
        contractAddress: t.contractAddress,
        isNative: t.isNative,
      }));
      // Sort highest performing first
      return mapped.sort((a, b) => b.change24h - a.change24h).slice(0, 4);
    }
    return DEFAULT_TOP_ASSETS;
  }, [tokens]);

  const handleOpenSwap = (asset: AssetPerformance) => {
    setSelectedAsset(asset);
    setActiveModal("swap");
    setIsFabOpen(false);
  };

  const handleOpenBridge = (asset: AssetPerformance) => {
    setSelectedAsset(asset);
    setActiveModal("bridge");
    setIsFabOpen(false);
  };

  return (
    <>
      {/* QUICK ACTIONS SECTION HEADER */}
      <div className="row between" style={{ marginTop: 20, marginBottom: 12 }}>
        <div className="row gap" style={{ gap: 8 }}>
          <div
            style={{
              width: 28,
              height: 28,
              borderRadius: "50%",
              background: "linear-gradient(135deg, rgba(0,212,255,0.25), rgba(0,82,255,0.25))",
              border: "1px solid var(--cyan)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: 14,
            }}
          >
            ⚡
          </div>
          <div>
            <div style={{ fontSize: 16, fontWeight: 800, color: "var(--text)" }}>
              Quick Actions · Top Assets
            </div>
            <div className="tiny muted">
              Instant DEX Swap & L1/L2 Cross-Chain Bridge on Base Mainnet
            </div>
          </div>
        </div>
        <button
          className="pill cyan"
          style={{ cursor: "pointer", border: "1px solid rgba(0,212,255,0.4)" }}
          onClick={() => handleOpenSwap(topAssets[0])}
        >
          🔄 Quick Swap
        </button>
      </div>

      {/* QUICK ACTION GRID */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
          gap: 12,
          marginBottom: 16,
        }}
      >
        {topAssets.map((asset) => {
          const isPositive = asset.change24h >= 0;
          const totalValUsd = asset.balance * asset.priceUsd;

          return (
            <motion.div
              key={asset.symbol}
              whileHover={{ y: -3, transition: { duration: 0.15 } }}
              className="card"
              style={{
                margin: 0,
                background: "linear-gradient(180deg, var(--card) 0%, rgba(13,20,38,0.95) 100%)",
                border: "1px solid rgba(32, 51, 94, 0.85)",
                borderRadius: 16,
                padding: "14px 16px",
                display: "flex",
                flexDirection: "column",
                justifyContent: "space-between",
                gap: 10,
                boxShadow: "0 4px 16px rgba(0, 0, 0, 0.2)",
              }}
            >
              {/* Asset Header */}
              <div className="row between">
                <div className="row gap" style={{ gap: 10 }}>
                  <TokenLogo symbol={asset.symbol} size={32} />
                  <div>
                    <div className="row gap" style={{ gap: 6 }}>
                      <span style={{ fontWeight: 800, fontSize: 15 }}>{asset.symbol}</span>
                      {asset.symbol === "AGL" && (
                        <span
                          style={{
                            fontSize: 9,
                            fontWeight: 800,
                            padding: "1px 5px",
                            borderRadius: 4,
                            background: "rgba(0,82,255,0.3)",
                            color: "var(--cyan)",
                            border: "1px solid rgba(0,212,255,0.3)",
                          }}
                        >
                          CORE
                        </span>
                      )}
                    </div>
                    <div className="tiny muted" style={{ maxWidth: 130, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                      {asset.name}
                    </div>
                  </div>
                </div>

                <div
                  style={{
                    padding: "3px 8px",
                    borderRadius: 8,
                    fontSize: 11,
                    fontWeight: 700,
                    background: isPositive ? "rgba(0, 230, 153, 0.12)" : "rgba(255, 51, 102, 0.12)",
                    color: isPositive ? "var(--neon)" : "var(--rose)",
                    border: `1px solid ${isPositive ? "rgba(0, 230, 153, 0.3)" : "rgba(255, 51, 102, 0.3)"}`,
                  }}
                >
                  {isPositive ? "↗ +" : "↘ "}
                  {asset.change24h.toFixed(2)}%
                </div>
              </div>

              {/* Price & Balance Info */}
              <div
                className="row between"
                style={{
                  background: "rgba(7, 11, 22, 0.5)",
                  padding: "8px 10px",
                  borderRadius: 10,
                  border: "1px solid rgba(32, 51, 94, 0.4)",
                }}
              >
                <div>
                  <div className="tiny muted">Current Price</div>
                  <div style={{ fontWeight: 700, fontSize: 14 }}>{fmtUsd(asset.priceUsd)}</div>
                </div>
                <div style={{ textAlign: "right" }}>
                  <div className="tiny muted">Your Balance</div>
                  <div style={{ fontWeight: 700, fontSize: 13, color: "var(--cyan)" }}>
                    {asset.balance.toFixed(asset.balance < 1 ? 4 : 2)} {asset.symbol}
                  </div>
                  <div className="tiny muted">{fmtUsd(totalValUsd)}</div>
                </div>
              </div>

              {/* Dual Action Buttons: Swap & Bridge */}
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8, marginTop: 2 }}>
                <button
                  className="btn"
                  onClick={() => handleOpenSwap(asset)}
                  style={{
                    padding: "8px 10px",
                    fontSize: 12,
                    fontWeight: 700,
                    background: "rgba(0, 212, 255, 0.15)",
                    color: "var(--cyan)",
                    border: "1px solid rgba(0, 212, 255, 0.4)",
                    borderRadius: 10,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: 5,
                    cursor: "pointer",
                  }}
                >
                  <span>🔄</span> Swap
                </button>
                <button
                  className="btn"
                  onClick={() => handleOpenBridge(asset)}
                  style={{
                    padding: "8px 10px",
                    fontSize: 12,
                    fontWeight: 700,
                    background: "rgba(139, 92, 246, 0.15)",
                    color: "var(--purple)",
                    border: "1px solid rgba(139, 92, 246, 0.4)",
                    borderRadius: 10,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: 5,
                    cursor: "pointer",
                  }}
                >
                  <span>🌉</span> Bridge
                </button>
              </div>
            </motion.div>
          );
        })}
      </div>

      {/* FLOATING ACTION BUTTON (FAB) */}
      <div
        style={{
          position: "fixed",
          bottom: 74,
          right: 20,
          zIndex: 80,
          display: "flex",
          flexDirection: "column",
          alignItems: "flex-end",
          gap: 10,
        }}
      >
        {/* Speed-dial options */}
        <AnimatePresence>
          {isFabOpen && (
            <motion.div
              initial={{ opacity: 0, scale: 0.85, y: 15 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.85, y: 15 }}
              transition={{ duration: 0.18 }}
              style={{
                background: "var(--card-elev)",
                border: "1px solid rgba(0, 212, 255, 0.4)",
                borderRadius: 16,
                padding: "10px",
                boxShadow: "0 10px 30px rgba(0, 0, 0, 0.6), 0 0 20px rgba(0, 212, 255, 0.15)",
                display: "flex",
                flexDirection: "column",
                gap: 8,
                minWidth: 200,
              }}
            >
              <div className="tiny bold muted" style={{ padding: "2px 6px", textTransform: "uppercase", letterSpacing: 0.5 }}>
                ⚡ Quick Action Menu
              </div>

              <button
                onClick={() => handleOpenSwap(topAssets[0])}
                className="row gap"
                style={{
                  background: "rgba(0, 212, 255, 0.12)",
                  border: "1px solid rgba(0, 212, 255, 0.3)",
                  borderRadius: 10,
                  padding: "8px 12px",
                  color: "#fff",
                  fontSize: 13,
                  fontWeight: 700,
                  cursor: "pointer",
                  textAlign: "left",
                  width: "100%",
                }}
              >
                <span style={{ fontSize: 16 }}>🔄</span>
                <div>
                  <div>Instant DEX Swap</div>
                  <div className="tiny muted">Aerodrome & UniV3 on Base</div>
                </div>
              </button>

              <button
                onClick={() => handleOpenBridge(topAssets[0])}
                className="row gap"
                style={{
                  background: "rgba(139, 92, 246, 0.12)",
                  border: "1px solid rgba(139, 92, 246, 0.3)",
                  borderRadius: 10,
                  padding: "8px 12px",
                  color: "#fff",
                  fontSize: 13,
                  fontWeight: 700,
                  cursor: "pointer",
                  textAlign: "left",
                  width: "100%",
                }}
              >
                <span style={{ fontSize: 16 }}>🌉</span>
                <div>
                  <div>Cross-Chain Bridge</div>
                  <div className="tiny muted">L1 / L2 ⇄ Base Mainnet</div>
                </div>
              </button>

              <div style={{ height: 1, background: "var(--border)", margin: "2px 0" }} />

              <div className="tiny muted" style={{ padding: "0 6px" }}>
                Top Assets:
              </div>
              <div style={{ display: "flex", gap: 6, flexWrap: "wrap", padding: "0 4px" }}>
                {topAssets.slice(0, 3).map((a) => (
                  <button
                    key={a.symbol}
                    onClick={() => handleOpenSwap(a)}
                    style={{
                      padding: "4px 8px",
                      borderRadius: 8,
                      background: "var(--surface)",
                      border: "1px solid var(--border)",
                      color: "var(--cyan)",
                      fontSize: 11,
                      fontWeight: 700,
                      cursor: "pointer",
                    }}
                  >
                    Swap {a.symbol}
                  </button>
                ))}
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* The Main FAB Button */}
        <motion.button
          whileHover={{ scale: 1.06 }}
          whileTap={{ scale: 0.95 }}
          onClick={() => setIsFabOpen(!isFabOpen)}
          style={{
            height: 48,
            padding: isFabOpen ? "0 18px" : "0 18px",
            borderRadius: 24,
            border: "1px solid rgba(0, 230, 255, 0.6)",
            background: isFabOpen
              ? "linear-gradient(135deg, #FF3366, #A855F7)"
              : "linear-gradient(135deg, #0052FF 0%, #00D4FF 100%)",
            color: "#FFFFFF",
            boxShadow: isFabOpen
              ? "0 6px 24px rgba(255, 51, 102, 0.4)"
              : "0 6px 24px rgba(0, 212, 255, 0.45)",
            display: "flex",
            alignItems: "center",
            gap: 8,
            cursor: "pointer",
            fontWeight: 800,
            fontSize: 13,
            letterSpacing: 0.3,
          }}
        >
          <span style={{ fontSize: 18 }}>{isFabOpen ? "✕" : "⚡"}</span>
          <span>{isFabOpen ? "Close" : "Quick Actions"}</span>
        </motion.button>
      </div>

      {/* QUICK SWAP MODAL */}
      <AnimatePresence>
        {activeModal === "swap" && (
          <QuickSwapModal
            initialAsset={selectedAsset}
            allAssets={topAssets}
            wallet={wallet}
            onClose={() => setActiveModal(null)}
            onNavigateWallet={onNavigateWallet}
          />
        )}
      </AnimatePresence>

      {/* QUICK BRIDGE MODAL */}
      <AnimatePresence>
        {activeModal === "bridge" && (
          <QuickBridgeModal
            initialAsset={selectedAsset}
            wallet={wallet}
            onClose={() => setActiveModal(null)}
          />
        )}
      </AnimatePresence>
    </>
  );
};

// ----------------------------------------------------------------------
// QUICK SWAP MODAL COMPONENT
// ----------------------------------------------------------------------
interface QuickSwapModalProps {
  initialAsset: AssetPerformance;
  allAssets: AssetPerformance[];
  wallet: string;
  onClose: () => void;
  onNavigateWallet?: () => void;
}

const QuickSwapModal: React.FC<QuickSwapModalProps> = ({
  initialAsset,
  allAssets,
  wallet,
  onClose,
}) => {
  const [tokenIn, setTokenIn] = useState<string>(initialAsset.symbol);
  const [tokenOut, setTokenOut] = useState<string>(
    initialAsset.symbol === "AGL" ? "ETH" : "AGL"
  );
  const [amountIn, setAmountIn] = useState<string>("10");
  const [slippage, setSlippage] = useState<string>("0.5");
  const [isSwapping, setIsSwapping] = useState<boolean>(false);
  const [swapResult, setSwapResult] = useState<{
    txHash: string;
    amountIn: string;
    amountOut: string;
    tokenIn: string;
    tokenOut: string;
  } | null>(null);

  const tokenInObj = allAssets.find((a) => a.symbol === tokenIn) || initialAsset;
  const tokenOutObj = allAssets.find((a) => a.symbol === tokenOut) || (
    tokenIn === "ETH" ? allAssets.find((a) => a.symbol === "AGL") || initialAsset : initialAsset
  );

  const parsedAmount = parseFloat(amountIn) || 0;
  const inPrice = tokenInObj.priceUsd || 1;
  const outPrice = tokenOutObj.priceUsd || 1;
  const slipMultiplier = 1 - parseFloat(slippage) / 100;
  const estimatedOut = ((parsedAmount * inPrice) / outPrice) * slipMultiplier;

  const handlePercentage = (pct: number) => {
    const val = (tokenInObj.balance * pct).toFixed(tokenInObj.balance < 1 ? 4 : 2);
    setAmountIn(val);
  };

  const executeSwap = () => {
    if (parsedAmount <= 0) return;
    setIsSwapping(true);
    setSwapResult(null);

    setTimeout(() => {
      setIsSwapping(false);
      const fakeHash = "0x" + Array.from({ length: 64 }, () => Math.floor(Math.random() * 16).toString(16)).join("");
      setSwapResult({
        txHash: fakeHash,
        amountIn,
        amountOut: estimatedOut.toFixed(4),
        tokenIn,
        tokenOut,
      });
    }, 1400);
  };

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        backgroundColor: "rgba(5, 9, 20, 0.8)",
        backdropFilter: "blur(6px)",
        zIndex: 100,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 16,
      }}
      onClick={onClose}
    >
      <motion.div
        initial={{ opacity: 0, scale: 0.95, y: 10 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95, y: 10 }}
        transition={{ duration: 0.2 }}
        className="card card-elev"
        style={{
          width: "100%",
          maxWidth: 460,
          background: "var(--card-elev)",
          border: "1px solid rgba(0, 212, 255, 0.35)",
          boxShadow: "0 20px 50px rgba(0, 0, 0, 0.7), 0 0 30px rgba(0, 212, 255, 0.15)",
          margin: 0,
          padding: 20,
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="row between" style={{ marginBottom: 16 }}>
          <div className="row gap" style={{ gap: 8 }}>
            <span style={{ fontSize: 20 }}>🔄</span>
            <div>
              <div style={{ fontWeight: 800, fontSize: 17 }}>Instant DEX Swap</div>
              <div className="tiny muted">Aerodrome & UniV3 Router · Base Mainnet</div>
            </div>
          </div>
          <button
            onClick={onClose}
            style={{
              background: "var(--card)",
              border: "1px solid var(--border)",
              color: "var(--text-2)",
              borderRadius: "50%",
              width: 28,
              height: 28,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              cursor: "pointer",
              fontSize: 14,
            }}
          >
            ✕
          </button>
        </div>

        {swapResult ? (
          <div style={{ textAlign: "center", padding: "16px 0" }}>
            <div
              style={{
                width: 54,
                height: 54,
                borderRadius: "50%",
                background: "rgba(0, 230, 153, 0.15)",
                border: "2px solid var(--neon)",
                color: "var(--neon)",
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: 26,
                marginBottom: 14,
              }}
            >
              ✓
            </div>
            <div style={{ fontSize: 18, fontWeight: 800, color: "var(--neon)" }}>
              Swap Successfully Broadcasted!
            </div>
            <div className="small muted" style={{ marginTop: 6, marginBottom: 16 }}>
              Swapped {swapResult.amountIn} {swapResult.tokenIn} for ≈{swapResult.amountOut}{" "}
              {swapResult.tokenOut} on Base Mainnet.
            </div>

            <div
              className="card"
              style={{
                background: "var(--surface)",
                textAlign: "left",
                fontSize: 12,
                padding: 12,
                marginBottom: 16,
              }}
            >
              <div className="row between">
                <span className="muted">Tx Hash:</span>
                <span className="mono bold" style={{ color: "var(--cyan)" }}>
                  {shortAddr(swapResult.txHash)}
                </span>
              </div>
              <div style={{ height: 6 }} />
              <div className="row between">
                <span className="muted">Network Fee:</span>
                <span className="bold" style={{ color: "var(--neon)" }}>
                  $0.0012 (Base Blob Gas)
                </span>
              </div>
              <div style={{ height: 6 }} />
              <div className="row between">
                <span className="muted">Settlement:</span>
                <span className="bold">Aerodrome CL-Pool (0.05%)</span>
              </div>
            </div>

            <button
              className="btn"
              onClick={onClose}
              style={{ background: "var(--cyan)", color: "#000", fontWeight: 800 }}
            >
              Done
            </button>
          </div>
        ) : (
          <>
            {/* Pay Input Card */}
            <div
              className="card"
              style={{
                background: "var(--surface)",
                border: "1px solid var(--border)",
                padding: 12,
                marginBottom: 8,
              }}
            >
              <div className="row between tiny muted" style={{ marginBottom: 6 }}>
                <span>You Pay</span>
                <span>
                  Balance: {tokenInObj.balance.toFixed(2)} {tokenIn}
                </span>
              </div>

              <div className="row gap" style={{ gap: 8 }}>
                <input
                  type="number"
                  value={amountIn}
                  onChange={(e) => setAmountIn(e.target.value)}
                  placeholder="0.0"
                  style={{
                    flex: 1,
                    background: "transparent",
                    border: "none",
                    color: "var(--text)",
                    fontSize: 20,
                    fontWeight: 800,
                    outline: "none",
                    width: "100%",
                  }}
                />
                <select
                  value={tokenIn}
                  onChange={(e) => {
                    const val = e.target.value;
                    if (val === tokenOut) setTokenOut(tokenIn);
                    setTokenIn(val);
                  }}
                  className="input"
                  style={{ width: "auto", fontWeight: 700, padding: "6px 10px" }}
                >
                  {allAssets.map((a) => (
                    <option key={a.symbol} value={a.symbol}>
                      {a.symbol}
                    </option>
                  ))}
                </select>
              </div>

              <div className="row gap" style={{ gap: 6, marginTop: 8 }}>
                {[0.25, 0.5, 1.0].map((pct) => (
                  <button
                    key={pct}
                    onClick={() => handlePercentage(pct)}
                    className="pill"
                    style={{
                      background: "var(--card)",
                      border: "1px solid var(--border)",
                      color: "var(--text-2)",
                      cursor: "pointer",
                      fontSize: 10,
                      padding: "2px 8px",
                    }}
                  >
                    {pct === 1.0 ? "MAX" : `${pct * 100}%`}
                  </button>
                ))}
              </div>
            </div>

            {/* Switch button */}
            <div style={{ textAlign: "center", margin: "-12px 0", position: "relative", zIndex: 5 }}>
              <button
                onClick={() => {
                  const t = tokenIn;
                  setTokenIn(tokenOut);
                  setTokenOut(t);
                }}
                style={{
                  width: 32,
                  height: 32,
                  borderRadius: "50%",
                  background: "var(--card-elev)",
                  border: "1px solid var(--cyan)",
                  color: "var(--cyan)",
                  display: "inline-flex",
                  alignItems: "center",
                  justifyContent: "center",
                  cursor: "pointer",
                }}
              >
                ⇅
              </button>
            </div>

            {/* Receive Output Card */}
            <div
              className="card"
              style={{
                background: "var(--surface)",
                border: "1px solid var(--border)",
                padding: 12,
                marginTop: 8,
                marginBottom: 12,
              }}
            >
              <div className="row between tiny muted" style={{ marginBottom: 6 }}>
                <span>You Receive (Estimated)</span>
                <span>
                  1 {tokenIn} ≈ {(inPrice / outPrice).toFixed(3)} {tokenOut}
                </span>
              </div>

              <div className="row between">
                <span style={{ fontSize: 20, fontWeight: 800, color: "var(--neon)" }}>
                  {estimatedOut > 0 ? estimatedOut.toFixed(4) : "0.00"}
                </span>
                <select
                  value={tokenOut}
                  onChange={(e) => {
                    const val = e.target.value;
                    if (val === tokenIn) setTokenIn(tokenOut);
                    setTokenOut(val);
                  }}
                  className="input"
                  style={{ width: "auto", fontWeight: 700, padding: "6px 10px" }}
                >
                  {allAssets.map((a) => (
                    <option key={a.symbol} value={a.symbol}>
                      {a.symbol}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Slippage & Details */}
            <div
              className="row between tiny muted"
              style={{ marginBottom: 14, padding: "0 2px" }}
            >
              <span>Slippage Tolerance</span>
              <div className="row gap" style={{ gap: 4 }}>
                {["0.1", "0.5", "1.0"].map((s) => (
                  <button
                    key={s}
                    onClick={() => setSlippage(s)}
                    className="pill"
                    style={{
                      background: slippage === s ? "rgba(0,212,255,0.2)" : "transparent",
                      borderColor: slippage === s ? "var(--cyan)" : "var(--border)",
                      color: slippage === s ? "var(--cyan)" : "var(--text-3)",
                      cursor: "pointer",
                      fontSize: 10,
                      padding: "2px 6px",
                    }}
                  >
                    {s}%
                  </button>
                ))}
              </div>
            </div>

            <div
              className="card"
              style={{
                background: "rgba(0, 82, 255, 0.08)",
                border: "1px solid rgba(0, 82, 255, 0.25)",
                padding: "8px 12px",
                marginBottom: 16,
                fontSize: 11,
              }}
            >
              <div className="row between">
                <span className="muted">Route:</span>
                <span className="bold">Aerodrome v2 Concentrated Liquidity</span>
              </div>
              <div className="row between" style={{ marginTop: 4 }}>
                <span className="muted">Est. Gas Fee:</span>
                <span className="bold" style={{ color: "var(--neon)" }}>
                  ~$0.0012 (Base Blob Savings)
                </span>
              </div>
            </div>

            <button
              className="btn"
              disabled={isSwapping || parsedAmount <= 0}
              onClick={executeSwap}
              style={{
                background: "linear-gradient(135deg, #0052FF 0%, #00D4FF 100%)",
                fontWeight: 800,
                fontSize: 15,
                boxShadow: "0 4px 16px rgba(0, 212, 255, 0.3)",
              }}
            >
              {isSwapping ? "Executing DEX Swap on Base…" : `Swap ${tokenIn} → ${tokenOut}`}
            </button>
          </>
        )}
      </motion.div>
    </div>
  );
};

// ----------------------------------------------------------------------
// QUICK BRIDGE MODAL COMPONENT
// ----------------------------------------------------------------------
interface QuickBridgeModalProps {
  initialAsset: AssetPerformance;
  wallet: string;
  onClose: () => void;
}

const QuickBridgeModal: React.FC<QuickBridgeModalProps> = ({
  initialAsset,
  wallet,
  onClose,
}) => {
  const [assetSymbol, setAssetSymbol] = useState<string>(
    initialAsset.symbol === "wAGL" ? "AGL" : initialAsset.symbol
  );
  const [sourceChain, setSourceChain] = useState<string>("Ethereum Mainnet (L1)");
  const [destChain, setDestChain] = useState<string>("Base Mainnet (8453)");
  const [amount, setAmount] = useState<string>("0.5");
  const [provider, setProvider] = useState<string>("Base Official Bridge");
  const [isBridging, setIsBridging] = useState<boolean>(false);
  const [bridgeResult, setBridgeResult] = useState<{
    txHash: string;
    depositId: string;
    sourceChain: string;
    destChain: string;
    amount: string;
    asset: string;
  } | null>(null);

  const parsedAmount = parseFloat(amount) || 0;

  const executeBridge = () => {
    if (parsedAmount <= 0) return;
    setIsBridging(true);
    setBridgeResult(null);

    setTimeout(() => {
      setIsBridging(false);
      const fakeTx = "0x" + Array.from({ length: 64 }, () => Math.floor(Math.random() * 16).toString(16)).join("");
      const depId = "DEP-" + Math.floor(100000 + Math.random() * 900000);
      setBridgeResult({
        txHash: fakeTx,
        depositId: depId,
        sourceChain,
        destChain,
        amount,
        asset: assetSymbol,
      });
    }, 1800);
  };

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        backgroundColor: "rgba(5, 9, 20, 0.8)",
        backdropFilter: "blur(6px)",
        zIndex: 100,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 16,
      }}
      onClick={onClose}
    >
      <motion.div
        initial={{ opacity: 0, scale: 0.95, y: 10 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95, y: 10 }}
        transition={{ duration: 0.2 }}
        className="card card-elev"
        style={{
          width: "100%",
          maxWidth: 480,
          background: "var(--card-elev)",
          border: "1px solid rgba(139, 92, 246, 0.4)",
          boxShadow: "0 20px 50px rgba(0, 0, 0, 0.7), 0 0 30px rgba(139, 92, 246, 0.15)",
          margin: 0,
          padding: 20,
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="row between" style={{ marginBottom: 16 }}>
          <div className="row gap" style={{ gap: 8 }}>
            <span style={{ fontSize: 20 }}>🌉</span>
            <div>
              <div style={{ fontWeight: 800, fontSize: 17 }}>Cross-Chain Bridge</div>
              <div className="tiny muted">Deposit or Migrate Assets to Base (EVM 8453)</div>
            </div>
          </div>
          <button
            onClick={onClose}
            style={{
              background: "var(--card)",
              border: "1px solid var(--border)",
              color: "var(--text-2)",
              borderRadius: "50%",
              width: 28,
              height: 28,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              cursor: "pointer",
              fontSize: 14,
            }}
          >
            ✕
          </button>
        </div>

        {bridgeResult ? (
          <div style={{ textAlign: "center", padding: "16px 0" }}>
            <div
              style={{
                width: 54,
                height: 54,
                borderRadius: "50%",
                background: "rgba(139, 92, 246, 0.15)",
                border: "2px solid var(--purple)",
                color: "var(--purple)",
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: 26,
                marginBottom: 14,
              }}
            >
              🚀
            </div>
            <div style={{ fontSize: 18, fontWeight: 800, color: "var(--purple)" }}>
              Bridge Transfer Initiated!
            </div>
            <div className="small muted" style={{ marginTop: 6, marginBottom: 16 }}>
              Bridging {bridgeResult.amount} {bridgeResult.asset} from {bridgeResult.sourceChain} to{" "}
              {bridgeResult.destChain}.
            </div>

            <div
              className="card"
              style={{
                background: "var(--surface)",
                textAlign: "left",
                fontSize: 12,
                padding: 12,
                marginBottom: 16,
              }}
            >
              <div className="row between">
                <span className="muted">Deposit ID:</span>
                <span className="bold" style={{ color: "var(--cyan)" }}>
                  {bridgeResult.depositId}
                </span>
              </div>
              <div style={{ height: 6 }} />
              <div className="row between">
                <span className="muted">L1 Lock Tx:</span>
                <span className="mono bold" style={{ color: "var(--purple)" }}>
                  {shortAddr(bridgeResult.txHash)}
                </span>
              </div>
              <div style={{ height: 6 }} />
              <div className="row between">
                <span className="muted">Target Chain:</span>
                <span className="bold">Base Mainnet (8453)</span>
              </div>
              <div style={{ height: 6 }} />
              <div className="row between">
                <span className="muted">Estimated Arrival:</span>
                <span className="bold" style={{ color: "var(--neon)" }}>
                  ~2 Minutes (Optimistic Relayer)
                </span>
              </div>
            </div>

            <button
              className="btn"
              onClick={onClose}
              style={{ background: "var(--purple)", color: "#fff", fontWeight: 800 }}
            >
              Close Bridge Receipt
            </button>
          </div>
        ) : (
          <>
            {/* Asset Selector */}
            <div style={{ marginBottom: 12 }}>
              <label className="tiny muted bold" style={{ display: "block", marginBottom: 6 }}>
                SELECT ASSET TO BRIDGE
              </label>
              <div className="row gap" style={{ gap: 8 }}>
                {["ETH", "AGL", "USDC"].map((sym) => (
                  <button
                    key={sym}
                    onClick={() => setAssetSymbol(sym)}
                    className="pill"
                    style={{
                      flex: 1,
                      padding: "8px 12px",
                      justifyContent: "center",
                      background: assetSymbol === sym ? "rgba(139, 92, 246, 0.2)" : "var(--surface)",
                      borderColor: assetSymbol === sym ? "var(--purple)" : "var(--border)",
                      color: assetSymbol === sym ? "#fff" : "var(--text-2)",
                      fontWeight: 700,
                      cursor: "pointer",
                    }}
                  >
                    {sym}
                  </button>
                ))}
              </div>
            </div>

            {/* Source & Destination Route */}
            <div
              className="card"
              style={{
                background: "var(--surface)",
                border: "1px solid var(--border)",
                padding: 12,
                marginBottom: 12,
              }}
            >
              <div className="row between" style={{ marginBottom: 8 }}>
                <div style={{ flex: 1 }}>
                  <div className="tiny muted">From Source</div>
                  <select
                    value={sourceChain}
                    onChange={(e) => setSourceChain(e.target.value)}
                    className="input"
                    style={{ fontSize: 12, padding: "6px 8px", marginTop: 4 }}
                  >
                    <option value="Ethereum Mainnet (L1)">Ethereum Mainnet (L1)</option>
                    <option value="Arbitrum One (L2)">Arbitrum One (L2)</option>
                    <option value="Optimism Mainnet (L2)">Optimism Mainnet (L2)</option>
                    <option value="Polygon PoS">Polygon PoS</option>
                  </select>
                </div>

                <div
                  style={{
                    padding: "0 10px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontSize: 16,
                    color: "var(--purple)",
                    paddingTop: 16,
                  }}
                >
                  ➔
                </div>

                <div style={{ flex: 1 }}>
                  <div className="tiny muted">To Destination</div>
                  <select
                    value={destChain}
                    onChange={(e) => setDestChain(e.target.value)}
                    className="input"
                    style={{ fontSize: 12, padding: "6px 8px", marginTop: 4 }}
                  >
                    <option value="Base Mainnet (8453)">Base Mainnet (8453)</option>
                    <option value="Base Sepolia Testnet">Base Sepolia (84532)</option>
                  </select>
                </div>
              </div>

              {/* Amount input */}
              <div style={{ marginTop: 8 }}>
                <div className="row between tiny muted" style={{ marginBottom: 4 }}>
                  <span>Transfer Amount</span>
                  <span>Max Available: 2.50 {assetSymbol}</span>
                </div>
                <div className="row gap" style={{ gap: 8 }}>
                  <input
                    type="number"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    placeholder="0.0"
                    style={{
                      flex: 1,
                      background: "rgba(7, 11, 22, 0.5)",
                      border: "1px solid var(--border)",
                      color: "var(--text)",
                      fontSize: 16,
                      fontWeight: 700,
                      padding: "8px 12px",
                      borderRadius: 10,
                      outline: "none",
                    }}
                  />
                  <button
                    onClick={() => setAmount("2.50")}
                    className="pill"
                    style={{
                      background: "var(--card)",
                      borderColor: "var(--border)",
                      color: "var(--cyan)",
                      cursor: "pointer",
                      padding: "0 12px",
                      height: 38,
                    }}
                  >
                    MAX
                  </button>
                </div>
              </div>
            </div>

            {/* Bridge Provider Choice */}
            <div style={{ marginBottom: 14 }}>
              <div className="tiny muted bold" style={{ marginBottom: 6 }}>
                BRIDGE ROUTE PROVIDER
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                {[
                  {
                    id: "Base Official Bridge",
                    title: "Base Official Native Bridge",
                    time: "~10-15 min",
                    tag: "CANONICAL",
                    color: "var(--cyan)",
                  },
                  {
                    id: "Across Protocol",
                    title: "Across Protocol (Fast Intent)",
                    time: "~1-2 min",
                    tag: "FASTEST",
                    color: "var(--neon)",
                  },
                  {
                    id: "Stargate V2",
                    title: "Stargate V2 (LayerZero)",
                    time: "~3 min",
                    tag: "DEEP LIQ",
                    color: "var(--purple)",
                  },
                ].map((p) => (
                  <div
                    key={p.id}
                    onClick={() => setProvider(p.id)}
                    style={{
                      background: provider === p.id ? "rgba(139, 92, 246, 0.12)" : "var(--surface)",
                      border: `1px solid ${provider === p.id ? "var(--purple)" : "var(--border)"}`,
                      borderRadius: 10,
                      padding: "8px 12px",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "space-between",
                      cursor: "pointer",
                    }}
                  >
                    <div className="row gap" style={{ gap: 8 }}>
                      <span style={{ fontSize: 14 }}>
                        {provider === p.id ? "🔘" : "⚪"}
                      </span>
                      <div>
                        <div style={{ fontSize: 12, fontWeight: 700 }}>{p.title}</div>
                        <div className="tiny muted">Est. Time: {p.time}</div>
                      </div>
                    </div>
                    <span
                      className="pill"
                      style={{
                        background: `${p.color}20`,
                        color: p.color,
                        fontSize: 9,
                        padding: "2px 6px",
                      }}
                    >
                      {p.tag}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Savings callout */}
            <div
              className="card"
              style={{
                background: "rgba(0, 230, 153, 0.08)",
                border: "1px solid rgba(0, 230, 153, 0.3)",
                padding: "8px 12px",
                marginBottom: 16,
                fontSize: 11,
              }}
            >
              <div className="row between">
                <span className="muted">Base L2 Gas Savings:</span>
                <span className="bold" style={{ color: "var(--neon)" }}>
                  Save ~$16.50 vs L1 Execution
                </span>
              </div>
            </div>

            <button
              className="btn"
              disabled={isBridging || parsedAmount <= 0}
              onClick={executeBridge}
              style={{
                background: "linear-gradient(135deg, #8B5CF6 0%, #00D4FF 100%)",
                fontWeight: 800,
                fontSize: 15,
                boxShadow: "0 4px 16px rgba(139, 92, 246, 0.3)",
              }}
            >
              {isBridging ? "Locking & Bridging to Base…" : `Bridge ${amount} ${assetSymbol} to Base`}
            </button>
          </>
        )}
      </motion.div>
    </div>
  );
};
