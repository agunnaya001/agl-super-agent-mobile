import React from "react";

interface AppLogoProps {
  size?: number;
}

/**
 * Premium AGL Super Agent logo — hexagonal badge with a neon lightning bolt.
 */
export const AppLogo: React.FC<AppLogoProps> = ({ size = 28 }) => {
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="agl-badge" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#0052FF" />
          <stop offset="0.6" stopColor="#0033B0" />
          <stop offset="1" stopColor="#001A40" />
        </linearGradient>
        <linearGradient id="agl-bolt" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#00E6FF" />
          <stop offset="1" stopColor="#00E699" />
        </linearGradient>
        <filter id="agl-glow" x="-20%" y="-20%" width="140%" height="140%">
          <feGaussianBlur stdDeviation="1.5" result="blur" />
          <feMerge>
            <feMergeNode in="blur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
      </defs>
      {/* Hex badge */}
      <path
        d="M32 4L54 17V47L32 60L10 47V17L32 4Z"
        fill="url(#agl-badge)"
        stroke="#00D4FF"
        strokeWidth="1.5"
        strokeOpacity="0.5"
      />
      {/* Lightning bolt */}
      <path
        d="M35 14L20 36h9l-2 16 17-24h-9l2-14z"
        fill="url(#agl-bolt)"
        filter="url(#agl-glow)"
      />
    </svg>
  );
};
