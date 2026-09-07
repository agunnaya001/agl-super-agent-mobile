import React from "react";

interface TokenLogoProps {
  symbol: string;
  size?: number;
}

/**
 * Premium token logos using real cryptocurrency brand SVGs.
 * ETH, USDC, and AERO use their official brand colors and iconography.
 * AGL, wAGL, and CREDITS use custom premium designs.
 */
export const TokenLogo: React.FC<TokenLogoProps> = ({ symbol, size = 32 }) => {
  const sym = symbol.toUpperCase();
  const iconSize = size * 0.56;

  let bg = "linear-gradient(135deg, #0052FF, #00D4FF)";
  let border = "rgba(0, 212, 255, 0.4)";
  let icon: React.ReactNode = null;

  if (sym === "AGL") {
    bg = "radial-gradient(circle at 35% 30%, #0066FF, #001A40)";
    border = "#00E6FF";
    icon = (
      <svg width={iconSize} height={iconSize} viewBox="0 0 24 24" fill="none">
        <defs>
          <linearGradient id="agl-star" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0" stopColor="#00E6FF" />
            <stop offset="1" stopColor="#00E699" />
          </linearGradient>
        </defs>
        <path
          d="M12 2L14.5 8.5L21.5 9.3L16.2 13.8L17.8 20.7L12 17L6.2 20.7L7.8 13.8L2.5 9.3L9.5 8.5L12 2Z"
          fill="url(#agl-star)"
          stroke="#00E6FF"
          strokeWidth="0.5"
          strokeLinejoin="round"
        />
      </svg>
    );
  } else if (sym === "WAGL") {
    bg = "linear-gradient(135deg, #0052FF, #6D28D9)";
    border = "#FFD700";
    icon = (
      <svg width={iconSize} height={iconSize} viewBox="0 0 24 24" fill="none">
        <path
          d="M12 2L4 5v6c0 5 3.5 8.5 8 10 4.5-1.5 8-5 8-10V5l-8-3z"
          fill="none"
          stroke="#FFD700"
          strokeWidth="2"
          strokeLinejoin="round"
        />
        <path
          d="M9 12l2 2 4-4"
          fill="none"
          stroke="#FFD700"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    );
  } else if (sym === "CREDITS" || sym === "AGLCREDITS") {
    bg = "linear-gradient(135deg, #FF8C00, #FFD700)";
    border = "#FFA500";
    icon = (
      <svg width={iconSize * 1.05} height={iconSize * 1.05} viewBox="0 0 24 24" fill="none">
        <path
          d="M13 2L4 14h6l-1 8 9-12h-6l1-8z"
          fill="white"
          stroke="white"
          strokeWidth="0.5"
          strokeLinejoin="round"
        />
      </svg>
    );
  } else if (sym === "ETH") {
    // Real Ethereum logo — diamond with 3D shading effect
    bg = "linear-gradient(135deg, #627EEA, #4255A4)";
    border = "rgba(98, 126, 234, 0.6)";
    icon = (
      <svg width={iconSize} height={iconSize} viewBox="0 0 24 24" fill="none">
        <path d="M12 2L6.5 12.25L12 15.25L17.5 12.25L12 2Z" fill="white" fillOpacity="0.6" />
        <path d="M12 2L17.5 12.25L12 15.25V2Z" fill="white" fillOpacity="0.9" />
        <path d="M6.5 13.25L12 16.25V22L6.5 13.25Z" fill="white" fillOpacity="0.5" />
        <path d="M12 16.25L17.5 13.25L12 22V16.25Z" fill="white" />
      </svg>
    );
  } else if (sym === "USDC") {
    // Real USDC logo — blue circle with white dollar sign
    bg = "#2775CA";
    border = "rgba(39, 117, 202, 0.6)";
    icon = (
      <svg width={iconSize} height={iconSize} viewBox="0 0 24 24" fill="none">
        <path
          d="M14.5 12.8c0-.9-.6-1.3-1.8-1.6c-1-.2-1.2-.4-1.2-.7c0-.3.3-.5.8-.5c.5 0 .8.2.9.5c.1.0.2.0.3.0h.6c.2 0 .4-.1.4-.3c0-.6-.5-1.1-1.3-1.3v-.6c0-.2-.1-.3-.3-.3h-.3c-.2 0-.3.1-.3.3v.6c-1 .2-1.6.8-1.6 1.5c0 .9.6 1.3 1.8 1.6c1 .2 1.2.4 1.2.7c0 .3-.3.5-.9.5c-.5 0-.9-.2-1-.5c-.1-.1-.2-.2-.3-.2h-.6c-.2 0-.4.1-.4.3c0 .6.1.3 1.6 1.3v.6c0 .2.1.3.3.3h.3c.2 0 .3-.1.3-.3v-.6c1-.2 1.7-.8 1.7-1.6z"
          fill="white"
        />
        <path
          d="M12 6.5v11c2.8 0 5-2.2 5-5s-2.2-5-5-5z"
          fill="white"
          fillOpacity="0.3"
        />
      </svg>
    );
  } else if (sym === "AERO") {
    // Aerodrome logo — rocket/arrow with brand gradient
    bg = "linear-gradient(135deg, #18A0FB, #5C4BFF)";
    border = "rgba(24, 160, 251, 0.6)";
    icon = (
      <svg width={iconSize} height={iconSize} viewBox="0 0 24 24" fill="none">
        <path
          d="M12 2L8 8L4 10C4 10 6 14 12 14C18 14 20 10 20 10L16 8L12 2Z"
          fill="white"
          fillOpacity="0.9"
        />
        <path
          d="M12 14L9 18L12 22L15 18L12 14Z"
          fill="white"
          fillOpacity="0.6"
        />
        <circle cx="12" cy="9" r="1.5" fill="#5C4BFF" />
      </svg>
    );
  } else if (sym === "BASE") {
    bg = "linear-gradient(135deg, #0052FF, #0042CC)";
    border = "rgba(0, 82, 255, 0.6)";
    icon = (
      <svg width={iconSize} height={iconSize} viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="9" fill="none" stroke="white" strokeWidth="2.5" />
        <circle cx="12" cy="12" r="4" fill="white" />
      </svg>
    );
  } else {
    icon = (
      <span style={{ fontSize: size * 0.36, fontWeight: 800, color: "#00E6FF", fontFamily: "Space Grotesk, Inter, sans-serif" }}>
        {sym.slice(0, 3)}
      </span>
    );
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
        boxShadow: `0 2px 8px rgba(0,0,0,0.3), 0 0 12px ${border}33`,
      }}
    >
      {icon}
    </div>
  );
};
