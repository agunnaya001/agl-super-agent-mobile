import React from "react";

interface TokenLogoProps {
  symbol: string;
  size?: number;
}

export const TokenLogo: React.FC<TokenLogoProps> = ({ symbol, size = 32 }) => {
  const sym = symbol.toUpperCase();

  let bg = "linear-gradient(135deg, #0052FF, #00E6FF)";
  let border = "rgba(0, 230, 255, 0.4)";
  let iconContent = null;

  if (sym === "AGL") {
    bg = "radial-gradient(circle, #0052FF, #001A80)";
    border = "#00E6FF";
    iconContent = (
      <svg width={size * 0.55} height={size * 0.55} viewBox="0 0 24 24" fill="none" stroke="#00E6FF" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
      </svg>
    );
  } else if (sym === "WAGL") {
    bg = "linear-gradient(135deg, #0052FF, #7000FF)";
    border = "#FFD700";
    iconContent = (
      <svg width={size * 0.55} height={size * 0.55} viewBox="0 0 24 24" fill="none" stroke="#FFD700" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        <path d="M9 12l2 2 4-4" />
      </svg>
    );
  } else if (sym === "CREDITS" || sym === "AGLCREDITS") {
    bg = "linear-gradient(135deg, #FF8C00, #FFD700)";
    border = "#FFA500";
    iconContent = (
      <svg width={size * 0.6} height={size * 0.6} viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
      </svg>
    );
  } else if (sym === "ETH") {
    bg = "linear-gradient(135deg, #7000FF, #3b82f6)";
    border = "rgba(112, 0, 255, 0.6)";
    iconContent = (
      <svg width={size * 0.55} height={size * 0.55} viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2L4 12l8 4 8-4-8-10z" />
        <path d="M4 12l8 10 8-10" />
      </svg>
    );
  } else if (sym === "USDC") {
    bg = "linear-gradient(135deg, #2775CA, #00E699)";
    border = "#00E699";
    iconContent = (
      <svg width={size * 0.55} height={size * 0.55} viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="10" />
        <path d="M12 6v12M15 9.5c0-1.4-1.3-2.5-3-2.5s-3 1.1-3 2.5 1.3 2.5 3 2.5 3 1.1 3 2.5-1.3 2.5-3 2.5-3-1.1-3-2.5" />
      </svg>
    );
  } else if (sym === "AERO") {
    bg = "linear-gradient(135deg, #00E6A0, #0052FF)";
    border = "#00E6A0";
    iconContent = (
      <svg width={size * 0.55} height={size * 0.55} viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M16 3h5v5M4 20L21 3M21 16v5h-5M15 15l6 6M4 4l5 5" />
      </svg>
    );
  } else {
    iconContent = <span style={{ fontSize: size * 0.4, fontWeight: "bold", color: "#00E6FF" }}>{sym.slice(0, 2)}</span>;
  }

  return (
    <div
      style={{
        width: size,
        height: size,
        borderRadius: "50%",
        background: bg,
        border: `1px solid ${border}`,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        flexShrink: 0,
        boxShadow: `0 0 10px ${border}`
      }}
    >
      {iconContent}
    </div>
  );
};
