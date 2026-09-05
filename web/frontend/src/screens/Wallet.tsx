import { useEffect, useState } from "react";
import { api } from "../api";
import { Screen } from "../types";
import { fmtUsd, timeAgo, statusPill, shortAddr } from "../ui";

export function WalletScreen({ wallet, setWallet, navigate }: { wallet: string; setWallet: (a: string) => void; navigate: (s: Screen) => void }) {
  const [state, setState] = useState<any>(null);
  const [tokens, setTokens] = useState<any[]>([]);
  const [portfolio, setPortfolio] = useState<any>(null);
  const [txs, setTxs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [addrInput, setAddrInput] = useState("");

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
        <div className="tiny muted" style={{ marginTop: 8 }}>Zero private keys · read-only monitoring</div>
      </div>

      {loading ? <div className="center"><div className="loader" /></div> : (
        <>
          <div className="section-title">Balances</div>
          <div className="card">
            <div style={{ fontSize: 26, fontWeight: 800 }}>{fmtUsd(portfolio?.totalBalanceUsd ?? 0)}</div>
            <div className="small muted">Total Value · {portfolio?.change24hPercent >= 0 ? "+" : ""}{(portfolio?.change24hPercent ?? 0).toFixed(1)}% 24h</div>
          </div>
          {tokens.map((t) => (
            <div key={t.symbol} className="list-row">
              <div className="avatar-circle" style={{ background: "var(--surface)" }}>{t.iconEmoji}</div>
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
