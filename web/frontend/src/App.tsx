import { useState, useCallback, useEffect } from "react";
import { Screen, DEFAULT_WALLET } from "./types";
import { shortAddr } from "./ui";
import { HomeScreen } from "./screens/Home";
import { DashboardScreen } from "./screens/Dashboard";
import { WalletScreen } from "./screens/Wallet";
import { AIScreen } from "./screens/AI";
import { QuestsScreen } from "./screens/Quests";
import { ProfileScreen } from "./screens/Profile";
import { StakingScreen, GovernanceScreen, TimelockScreen, DiagnosticsScreen, PriceAlertsScreen, AglTokenScreen, CreditsScreen, WaglScreen } from "./screens/Secondary";
import { SecurityScreen } from "./screens/Security";
import { StakingCalcScreen } from "./screens/StakingCalc";
import { ProposalSimScreen } from "./screens/ProposalSim";
import { DelegationScreen } from "./screens/Delegation";
import { WatchlistScreen } from "./screens/Watchlist";
import { AppLogo } from "./components/AppLogo";

const NAV: { screen: Screen; label: string; icon: string }[] = [
  { screen: "HOME", label: "Home", icon: "🏠" },
  { screen: "DASHBOARD", label: "Monitor", icon: "📊" },
  { screen: "WALLET", label: "Wallet", icon: "👛" },
  { screen: "AI", label: "AI Agent", icon: "🤖" },
  { screen: "QUESTS", label: "Quests", icon: "🏆" },
  { screen: "PROFILE", label: "Profile", icon: "👤" },
];

const THEME_KEY = "agl-theme";

export function App() {
  const [screen, setScreen] = useState<Screen>("HOME");
  const [wallet, setWallet] = useState(DEFAULT_WALLET);
  const [theme, setTheme] = useState<"dark" | "light">("dark");
  const navigate = useCallback((s: Screen) => setScreen(s), []);

  useEffect(() => {
    const saved = localStorage.getItem(THEME_KEY) as "dark" | "light" | null;
    if (saved) setTheme(saved);
  }, []);

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem(THEME_KEY, theme);
  }, [theme]);

  const toggleTheme = () => setTheme((t) => (t === "dark" ? "light" : "dark"));

  return (
    <div className="app-shell">
      <div className="topbar">
        <div className="row gap" style={{ gap: 8, alignItems: "center" }}>
          <AppLogo size={26} />
          <span className="topbar-title">AGL Super Agent</span>
        </div>
        <div className="row gap" style={{ gap: 8 }}>
          <button className="topbar-theme" onClick={toggleTheme} title="Toggle theme">
            {theme === "dark" ? "☀️" : "🌙"}
          </button>
          <button className="topbar-wallet" onClick={() => setScreen("WALLET")}>
            <span className="status-dot" />
            {shortAddr(wallet)}
          </button>
        </div>
      </div>

      <div className="app-content">
        {screen === "HOME" && <HomeScreen wallet={wallet} navigate={navigate} />}
        {screen === "DASHBOARD" && <DashboardScreen wallet={wallet} navigate={navigate} />}
        {screen === "WALLET" && <WalletScreen wallet={wallet} setWallet={setWallet} navigate={navigate} />}
        {screen === "AI" && <AIScreen wallet={wallet} />}
        {screen === "QUESTS" && <QuestsScreen />}
        {screen === "PROFILE" && <ProfileScreen wallet={wallet} navigate={navigate} />}
        {screen === "STAKING" && <StakingScreen navigate={navigate} />}
        {screen === "GOVERNANCE" && <GovernanceScreen navigate={navigate} />}
        {screen === "TIMELOCK" && <TimelockScreen />}
        {screen === "DIAGNOSTICS" && <DiagnosticsScreen />}
        {screen === "PRICE_ALERTS" && <PriceAlertsScreen />}
        {screen === "AGL_TOKEN" && <AglTokenScreen />}
        {screen === "CREDITS" && <CreditsScreen wallet={wallet} />}
        {screen === "WAGL" && <WaglScreen wallet={wallet} />}
        {screen === "SECURITY" && <SecurityScreen wallet={wallet} />}
        {screen === "STAKING_CALC" && <StakingCalcScreen />}
        {screen === "PROPOSAL_SIM" && <ProposalSimScreen wallet={wallet} />}
        {screen === "DELEGATION" && <DelegationScreen wallet={wallet} />}
        {screen === "WATCHLIST" && <WatchlistScreen wallet={wallet} />}
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
