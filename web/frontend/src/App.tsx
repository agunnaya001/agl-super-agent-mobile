import { useState, useCallback } from "react";
import { Screen, DEFAULT_WALLET } from "./types";
import { shortAddr } from "./ui";
import { HomeScreen } from "./screens/Home";
import { DashboardScreen } from "./screens/Dashboard";
import { WalletScreen } from "./screens/Wallet";
import { AIScreen } from "./screens/AI";
import { QuestsScreen } from "./screens/Quests";
import { ProfileScreen } from "./screens/Profile";
import { StakingScreen, GovernanceScreen, TimelockScreen, DiagnosticsScreen, PriceAlertsScreen, AglTokenScreen, CreditsScreen, WaglScreen } from "./screens/Secondary";

const NAV: { screen: Screen; label: string; icon: string; hint: string }[] = [
  { screen: "HOME", label: "Home", icon: "⌂", hint: "Overview" },
  { screen: "DASHBOARD", label: "Monitor", icon: "◈", hint: "Network health" },
  { screen: "WALLET", label: "Wallet", icon: "◇", hint: "Assets and activity" },
  { screen: "AI", label: "AI Agent", icon: "✦", hint: "Ask your agent" },
  { screen: "QUESTS", label: "Quests", icon: "◆", hint: "Earn XP" },
  { screen: "PROFILE", label: "Profile", icon: "○", hint: "Account" },
];

export function App() {
  const [screen, setScreen] = useState<Screen>("HOME");
  const [wallet, setWallet] = useState(DEFAULT_WALLET);
  const navigate = useCallback((s: Screen) => setScreen(s), []);

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand-lockup">
          <span className="brand-mark" aria-hidden="true">A</span>
          <div>
            <div className="topbar-title">AGL Super Agent</div>
            <div className="topbar-subtitle">Base intelligence layer</div>
          </div>
        </div>
        <button className="topbar-wallet" onClick={() => setScreen("WALLET")} aria-label={`Open wallet ${shortAddr(wallet)}`}>
          <span className="status-dot" aria-hidden="true" />
          <span>{shortAddr(wallet)}</span>
          <span className="wallet-chevron" aria-hidden="true">↗</span>
        </button>
      </header>

      <main className="app-content">
        {screen === "HOME" && <HomeScreen wallet={wallet} navigate={navigate} />}
        {screen === "DASHBOARD" && <DashboardScreen wallet={wallet} navigate={navigate} />}
        {screen === "WALLET" && <WalletScreen wallet={wallet} setWallet={setWallet} navigate={navigate} />}
        {screen === "AI" && <AIScreen />}
        {screen === "QUESTS" && <QuestsScreen />}
        {screen === "PROFILE" && <ProfileScreen wallet={wallet} navigate={navigate} />}
        {screen === "STAKING" && <StakingScreen />}
        {screen === "GOVERNANCE" && <GovernanceScreen />}
        {screen === "TIMELOCK" && <TimelockScreen />}
        {screen === "DIAGNOSTICS" && <DiagnosticsScreen />}
        {screen === "PRICE_ALERTS" && <PriceAlertsScreen />}
        {screen === "AGL_TOKEN" && <AglTokenScreen />}
        {screen === "CREDITS" && <CreditsScreen wallet={wallet} />}
        {screen === "WAGL" && <WaglScreen wallet={wallet} />}
      </main>

      <nav className="bottomnav" aria-label="Primary navigation">
        {NAV.map((n) => (
          <button key={n.screen} className={`nav-item ${screen === n.screen ? "active" : ""}`} onClick={() => navigate(n.screen)} aria-current={screen === n.screen ? "page" : undefined}>
            <span className="nav-icon" aria-hidden="true">{n.icon}</span>
            <span>{n.label}</span>
            <span className="sr-only">{n.hint}</span>
          </button>
        ))}
      </nav>
    </div>
  );
}
