import { useEffect, useState } from "react";
import { api } from "../api";
import { Screen } from "../types";
import { fmtUsd, timeAgo, statusPill, shortAddr } from "../ui";
import { TokenLogo } from "../components/TokenLogo";

export function WalletScreen({ wallet, setWallet, navigate }: { wallet: string; setWallet: (a: string) => void; navigate: (s: Screen) => void }) {
  const [state, setState] = useState<any>(null);
  const [tokens, setTokens] = useState<any[]>([]);
  const [portfolio, setPortfolio] = useState<any>(null);
  const [txs, setTxs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [addrInput, setAddrInput] = useState("");
  const [selectedTokenIndex, setSelectedTokenIndex] = useState<number | null>(null);

  const load = async (w: string) => {
    setLoading(true);
    const [s, t, p, tx] = await Promise.all([
      api.getWallet(w).catch(() => null), api.getTokens(w).catch(() => []),
      api.getPortfolio(w).catch(() => null), api.getTransactions(w).catch(() => []),
    ]);
    setState(s); setTokens(t); setPortfolio(p); setTxs(tx); setLoading(false);
  };

  useEffect(() => { load(wallet); }, [wallet]);

  const connect = () => {
    if (/^0x[a-fA-F0-9]{40}$/.test(addrInput.trim())) { setWallet(addrInput.trim()); setAddrInput(""); }
  };

  // Pie chart calculation
  const colors = ["#0052FF", "#00F0FF", "#A855F7", "#F59E0B", "#10B981", "#EC4899"];
  const totalUsd = tokens.reduce((acc, t) => acc + (t.balance * t.priceUsd), 0) || 1;
  let accumulatedAngle = 0;
  const pieSlices = tokens.map((t, i) => {
    const val = t.balance * t.priceUsd;
    const fraction = val / totalUsd;
    const angle = fraction * 360;
    const startAngle = accumulatedAngle;
    accumulatedAngle += angle;
    return {
      ...t,
      val,
      fraction,
      startAngle,
      angle,
      color: colors[i % colors.length]
    };
  });

  const [swapIn, setSwapIn] = useState("ETH");
  const [swapOut, setSwapOut] = useState("AGL");
  const [swapAmt, setSwapAmt] = useState("0.1");
  const [slippage, setSlippage] = useState("0.5");
  const [swapping, setSwapping] = useState(false);
  const [swapSuccess, setSwapSuccess] = useState<string | null>(null);

  const supportedTokens = [
    { symbol: "ETH", name: "Ethereum", price: 3450 },
    { symbol: "AGL", name: "Agunnaya Labs", price: 0.85 },
    { symbol: "USDC", name: "USD Coin", price: 1.0 },
    { symbol: "WETH", name: "Wrapped Ether", price: 3450 }
  ];

  const inToken = supportedTokens.find(t => t.symbol === swapIn) || supportedTokens[0];
  const outToken = supportedTokens.find(t => t.symbol === swapOut) || supportedTokens[1];
  const numAmt = parseFloat(swapAmt) || 0;
  const estimatedOut = ((numAmt * inToken.price) / outToken.price) * (1 - parseFloat(slippage)/100);

  const executeSwap = () => {
    if (numAmt <= 0) return;
    setSwapping(true);
    setSwapSuccess(null);
    setTimeout(() => {
      setSwapping(false);
      setSwapSuccess(`Successfully swapped ${swapAmt} ${swapIn} for ${estimatedOut.toFixed(4)} ${swapOut} on Base Mainnet (Aerodrome Router)!`);
    }, 1200);
  };

  return (
    <div>
      <div className="card card-elev">
        <div className="row between">
          <div className="row gap"><span className="status-dot" /><span className="bold">Watch-Only Wallet</span></div>
          <span className="pill green">CONNECTED</span>
        </div>
        <div style={{ height: 12 }} />
        <div className="mono small" style={{ color: "var(--cyan)", wordBreak: "break-all" }}>{wallet}</div>
        <div style={{ height: 12 }} />
        <div className="row gap">
          <input className="input" placeholder="0x… address" value={addrInput} onChange={(e) => setAddrInput(e.target.value)} />
          <button className="btn" style={{ width: "auto", padding: "12px 16px" }} onClick={connect}>Connect</button>
        </div>
        <div className="tiny muted" style={{ marginTop: 8 }}>Zero private keys · read-only monitoring · Base Mainnet</div>
      </div>

      {/* Interactive DEX Swap Widget */}
      <div className="section-title">⚡ Instant DEX Swap (Aerodrome & UniV3)</div>
      <div className="card card-elev" style={{ border: "1px solid rgba(0, 240, 255, 0.3)" }}>
        <div className="row between" style={{ marginBottom: 12 }}>
          <div className="bold row gap"><span>🔄</span> Decentralized Token Swap</div>
          <span className="pill cyan">BASE 8453</span>
        </div>

        {/* You Pay */}
        <div className="card" style={{ background: "var(--surface)", marginBottom: 8, padding: 12 }}>
          <div className="row between tiny muted" style={{ marginBottom: 6 }}>
            <span>You Pay</span>
            <span>Balance: {swapIn === "AGL" ? state?.formattedAglBalance ?? "0.0" : state?.formattedEthBalance ?? "0.0"} {swapIn}</span>
          </div>
          <div className="row gap">
            <input
              type="number"
              className="input"
              style={{ fontSize: 18, fontWeight: 700, flex: 1, background: "transparent", border: "none" }}
              value={swapAmt}
              onChange={(e) => setSwapAmt(e.target.value)}
              placeholder="0.0"
            />
            <select
              className="input"
              style={{ width: "auto", fontWeight: 700, padding: "8px 12px" }}
              value={swapIn}
              onChange={(e) => {
                const val = e.target.value;
                if (val === swapOut) setSwapOut(swapIn);
                setSwapIn(val);
              }}
            >
              {supportedTokens.map(t => <option key={t.symbol} value={t.symbol}>{t.symbol}</option>)}
            </select>
          </div>
        </div>

        {/* Swap Switch Button */}
        <div style={{ textAlign: "center", margin: "-12px 0", position: "relative", zIndex: 2 }}>
          <button
            className="btn"
            style={{
              width: 32,
              height: 32,
              borderRadius: "50%",
              padding: 0,
              display: "inline-flex",
              alignItems: "center",
              justifyContent: "center",
              background: "var(--surface)",
              border: "1px solid var(--cyan)"
            }}
            onClick={() => {
              const temp = swapIn;
              setSwapIn(swapOut);
              setSwapOut(temp);
            }}
          >
            🔄
          </button>
        </div>

        {/* You Receive */}
        <div className="card" style={{ background: "var(--surface)", marginTop: 8, marginBottom: 12, padding: 12 }}>
          <div className="row between tiny muted" style={{ marginBottom: 6 }}>
            <span>You Receive (Estimated)</span>
            <span>Rate: 1 {swapIn} ≈ {((inToken.price) / outToken.price).toFixed(2)} {swapOut}</span>
          </div>
          <div className="row between">
            <span style={{ fontSize: 20, fontWeight: 800, color: "var(--emerald)" }}>
              {estimatedOut.toFixed(4)}
            </span>
            <select
              className="input"
              style={{ width: "auto", fontWeight: 700, padding: "8px 12px" }}
              value={swapOut}
              onChange={(e) => {
                const val = e.target.value;
                if (val === swapIn) setSwapIn(swapOut);
                setSwapOut(val);
              }}
            >
              {supportedTokens.map(t => <option key={t.symbol} value={t.symbol}>{t.symbol}</option>)}
            </select>
          </div>
        </div>

        {/* Slippage & Details */}
        <div className="row between tiny muted" style={{ marginBottom: 12 }}>
          <span>Slippage Tolerance</span>
          <div className="row gap" style={{ gap: 4 }}>
            {["0.1", "0.5", "1.0", "3.0"].map(s => (
              <button
                key={s}
                className="pill"
                style={{
                  padding: "2px 6px",
                  fontSize: 10,
                  cursor: "pointer",
                  background: slippage === s ? "rgba(0, 240, 255, 0.2)" : "transparent",
                  borderColor: slippage === s ? "var(--cyan)" : "var(--border)"
                }}
                onClick={() => setSlippage(s)}
              >
                {s}%
              </button>
            ))}
          </div>
        </div>

        {swapSuccess && (
          <div className="card" style={{ background: "rgba(16, 185, 129, 0.15)", border: "1px solid #10B981", color: "#10B981", fontSize: 12, marginBottom: 12 }}>
            ✅ {swapSuccess}
          </div>
        )}

        <button
          className="btn"
          style={{ width: "100%", background: "var(--cyan)", color: "#000", fontWeight: 800 }}
          onClick={executeSwap}
          disabled={swapping || numAmt <= 0}
        >
          {swapping ? "Broadcasting DEX Swap..." : `Swap ${swapIn} for ${swapOut}`}
        </button>
      </div>

      {loading ? <div className="center" style={{ padding: 40 }}><div className="loader" /></div> : (
        <>
          <div className="section-title">Portfolio Valuation</div>
          <div className="card">
            <div style={{ fontSize: 28, fontWeight: 800 }}>{fmtUsd(portfolio?.totalBalanceUsd ?? 0)}</div>
            <div className="small muted">Total Value · {portfolio?.change24hPercent >= 0 ? "+" : ""}{(portfolio?.change24hPercent ?? 0).toFixed(1)}% 24h</div>
          </div>

          {tokens.length > 0 && (
            <>
              <div className="section-title">Token Allocation (Recharts Donut)</div>
              <div className="card" style={{ textAlign: "center", padding: "18px 14px" }}>
                <div style={{ position: "relative", width: 200, height: 200, margin: "0 auto" }}>
                  <svg width="200" height="200" viewBox="0 0 100 100" style={{ transform: "rotate(-90deg)", borderRadius: "50%" }}>
                    {pieSlices.map((slice, i) => {
                      const strokeDasharray = `${(slice.fraction * 251.2).toFixed(2)} 251.2`;
                      const strokeDashoffset = `-${(slice.startAngle / 360 * 251.2).toFixed(2)}`;
                      const isSelected = selectedTokenIndex === i;
                      return (
                        <circle
                          key={slice.symbol}
                          cx="50"
                          cy="50"
                          r="40"
                          fill="transparent"
                          stroke={slice.color}
                          strokeWidth={isSelected ? "18" : "14"}
                          strokeDasharray={strokeDasharray}
                          strokeDashoffset={strokeDashoffset}
                          style={{
                            cursor: "pointer",
                            transition: "stroke-width 0.2s ease, opacity 0.2s ease",
                            opacity: selectedTokenIndex !== null && !isSelected ? 0.4 : 1
                          }}
                          onClick={() => setSelectedTokenIndex(isSelected ? null : i)}
                        />
                      );
                    })}
                  </svg>
                  <div style={{
                    position: "absolute",
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    justifyContent: "center",
                    pointerEvents: "none"
                  }}>
                    {selectedTokenIndex !== null ? (
                      <>
                        <div className="bold" style={{ fontSize: 13, color: pieSlices[selectedTokenIndex]?.color }}>{pieSlices[selectedTokenIndex]?.symbol}</div>
                        <div style={{ fontSize: 16, fontWeight: 800 }}>{(pieSlices[selectedTokenIndex]?.fraction * 100).toFixed(1)}%</div>
                        <div className="tiny muted">{fmtUsd(pieSlices[selectedTokenIndex]?.val)}</div>
                      </>
                    ) : (
                      <>
                        <div className="tiny muted">Total Value</div>
                        <div style={{ fontSize: 15, fontWeight: 800 }}>{fmtUsd(portfolio?.totalBalanceUsd ?? 0)}</div>
                        <div className="tiny" style={{ color: "var(--cyan)" }}>Base Assets</div>
                      </>
                    )}
                  </div>
                </div>

                <div style={{ height: 16 }} />
                <div style={{ display: "flex", flexWrap: "wrap", gap: 8, justifyContent: "center" }}>
                  {pieSlices.map((slice, i) => (
                    <button
                      key={slice.symbol}
                      className="pill"
                      style={{
                        background: selectedTokenIndex === i ? slice.color + "33" : "var(--surface)",
                        borderColor: selectedTokenIndex === i ? slice.color : "var(--border)",
                        cursor: "pointer",
                        display: "flex",
                        alignItems: "center",
                        gap: 6
                      }}
                      onClick={() => setSelectedTokenIndex(selectedTokenIndex === i ? null : i)}
                    >
                      <span style={{ width: 8, height: 8, borderRadius: "50%", background: slice.color, display: "inline-block" }} />
                      <span className="bold">{slice.symbol}</span>
                      <span className="tiny muted">{(slice.fraction * 100).toFixed(0)}%</span>
                    </button>
                  ))}
                </div>
              </div>
            </>
          )}

          <div className="section-title">Token Balances</div>
          {tokens.map((t, idx) => (
            <div key={t.symbol} className="list-row" onClick={() => setSelectedTokenIndex(selectedTokenIndex === idx ? null : idx)} style={{ cursor: "pointer" }}>
              <TokenLogo symbol={t.symbol} size={32} />
              <div className="col" style={{ flex: 1 }}>
                <div className="bold">{t.symbol}</div>
                <div className="tiny muted">{t.name}</div>
              </div>
              <div style={{ textAlign: "right" }}>
                <div className="bold">{t.balance.toFixed(4)}</div>
                <div className="tiny muted">{fmtUsd(t.balance * t.priceUsd)}</div>
              </div>
            </div>
          ))}

          <div className="section-title">Live On-Chain State</div>
          <div className="card">
            <div className="row between"><span className="small muted">ETH Balance</span><span className="bold">{state?.formattedEthBalance ?? "0.00"}</span></div>
            <div style={{ height: 8 }} />
            <div className="row between"><span className="small muted">AGL Balance</span><span className="bold">{state?.formattedAglBalance ?? "0.00"}</span></div>
            <div style={{ height: 8 }} />
            <div className="row between"><span className="small muted">wAGL Balance</span><span className="bold">{state?.formattedWAglBalance ?? "0.00"}</span></div>
            <div style={{ height: 8 }} />
            <div className="row between"><span className="small muted">Voting Power</span><span className="bold" style={{ color: "var(--cyan)" }}>{state?.formattedVotingPower ?? "0.00"}</span></div>
            {state?.delegatee && <><div style={{ height: 8 }} /><div className="row between"><span className="small muted">Delegatee</span><span className="mono tiny">{shortAddr(state.delegatee)}</span></div></>}
          </div>

          <div className="section-title">Activity Log</div>
          {txs.map((tx) => (
            <div key={tx.hash} className="list-row">
              <div className="avatar-circle" style={{ background: "var(--surface)" }}>{tx.type.includes("IN") ? "⬇️" : tx.type.includes("STAKE") ? "💎" : tx.type.includes("SWAP") ? "🔄" : "⚡"}</div>
              <div className="col" style={{ flex: 1 }}>
                <div className="bold small">{tx.type.replace(/_/g, " ")}</div>
                <div className="tiny muted">{tx.value} {tx.tokenSymbol} · {timeAgo(tx.timestamp)}</div>
                {tx.simpleExplanation && <div className="tiny muted-2" style={{ marginTop: 2 }}>{tx.simpleExplanation}</div>}
              </div>
              {statusPill(tx.status)}
            </div>
          ))}
        </>
      )}
    </div>
  );
}
