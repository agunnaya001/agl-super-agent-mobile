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

const NAV: { screen: Screen; label: string; icon: string }[] = [
  { screen: "HOME", label: "Home", icon: "🏠" },
  { screen: "DASHBOARD", label: "Monitor", icon: "📊" },
  { screen: "WALLET", label: "Wallet", icon: "👛" },
  { screen: "AI", label: "AI Agent", icon: "🤖" },
  { screen: "QUESTS", label: "Quests", icon: "🏆" },
  { screen: "PROFILE", label: "Profile", icon: "👤" },
];

export function App() {
  const [screen, setScreen] = useState<Screen>("HOME");
  const [wallet, setWallet] = useState(DEFAULT_WALLET);
  const navigate = useCallback((s: Screen) => setScreen(s), []);

  return (
    <div className="app-shell">
      <div className="topbar">
        <div className="topbar-title">⚡ AGL Super Agent</div>
        <button className="topbar-wallet" onClick={() => setScreen("WALLET")}>
          <span className="status-dot" />
          {shortAddr(wallet)}
        </button>
      </div>

      <div className="app-content">
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
      </div>

      <nav className="bottomnav">
        {NAV.map((n) => (
          <button key={n.screen} className={`nav-item ${screen === n.screen ? "active" : ""}`} onClick={() => navigate(n.screen)}>
            <span className="nav-icon">{n.icon}</span>
            <span>{n.label}</span>
          </button>
        ))}
      </nav>
    </div>
  );
}
