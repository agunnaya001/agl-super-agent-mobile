import React, { useEffect, useRef, useState, useCallback } from "react";
import { CyberSentinelEngine } from "./engine";
import { GamePhase, GameTelemetry } from "./types";
import { sound } from "./audio";

interface CyberSentinelGameProps {
  onExit: () => void;
}

export function CyberSentinelGame({ onExit }: CyberSentinelGameProps) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const engineRef = useRef<CyberSentinelEngine | null>(null);

  const [phase, setPhase] = useState<GamePhase>("TITLE");
  const [telemetry, setTelemetry] = useState<GameTelemetry>({
    score: 0,
    highScore: 0,
    shardsCollected: 0,
    wave: 1,
    multiplier: 1.0,
    shieldHp: 100,
    maxShieldHp: 100,
    empCharge: 100,
    weaponLevel: 1,
    activeEnemiesCount: 0,
    fps: 60,
  });

  const [isMuted, setIsMuted] = useState(sound.isMuted());

  // Mount engine in isolated canvas
  useEffect(() => {
    if (!canvasRef.current) return;

    const engine = new CyberSentinelEngine(
      canvasRef.current,
      (data) => setTelemetry(data),
      (newPhase) => setPhase(newPhase)
    );
    engineRef.current = engine;
    engine.startLoop();

    return () => {
      engine.destroy();
    };
  }, []);

  const handleStartGame = useCallback(() => {
    engineRef.current?.startGame();
  }, []);

  const handleTogglePause = useCallback(() => {
    engineRef.current?.togglePause();
  }, []);

  const handleTriggerEmp = useCallback(() => {
    engineRef.current?.triggerEmp();
  }, []);

  const handleToggleMute = useCallback(() => {
    const next = sound.toggleMute();
    setIsMuted(next);
  }, []);

  // Touch joystick listeners for mobile support
  const handleTouchStart = (e: React.TouchEvent<HTMLDivElement>) => {
    if (!engineRef.current || phase !== "PLAYING") return;
    const touch = e.touches[0];
    const rect = canvasRef.current?.getBoundingClientRect();
    if (rect) {
      engineRef.current.touchX = touch.clientX - rect.left;
      engineRef.current.touchY = touch.clientY - rect.top;
      engineRef.current.touchActive = true;
    }
  };

  const handleTouchMove = (e: React.TouchEvent<HTMLDivElement>) => {
    if (!engineRef.current || phase !== "PLAYING") return;
    const touch = e.touches[0];
    const rect = canvasRef.current?.getBoundingClientRect();
    if (rect) {
      engineRef.current.touchX = touch.clientX - rect.left;
      engineRef.current.touchY = touch.clientY - rect.top;
    }
  };

  const handleTouchEnd = () => {
    if (!engineRef.current) return;
    engineRef.current.touchActive = false;
  };

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        zIndex: 9999,
        background: "#030712",
        overflow: "hidden",
        userSelect: "none",
        fontFamily: 'ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      }}
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
    >
      {/* 1. Canvas 60fps Isolated Viewport */}
      <canvas
        ref={canvasRef}
        style={{
          width: "100%",
          height: "100%",
          display: "block",
        }}
      />

      {/* 2. Low-Occlusion Playing HUD (At least 85% playfield unobstructed, pointer-events: none on wrapper) */}
      {phase === "PLAYING" && (
        <div
          style={{
            position: "absolute",
            inset: 0,
            pointerEvents: "none",
            display: "flex",
            flexDirection: "column",
            justifyContent: "space-between",
            padding: "16px 20px",
          }}
        >
          {/* Top Status Clusters */}
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
            {/* Top-Left: Shield, EMP, Score */}
            <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
              {/* Shield Bar */}
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <span style={{ fontSize: 13, color: "var(--cyan, #00d4ff)", fontWeight: 800 }}>🛡️ SHIELD</span>
                <div
                  style={{
                    width: 140,
                    height: 10,
                    borderRadius: 5,
                    background: "rgba(255, 255, 255, 0.12)",
                    overflow: "hidden",
                    border: "1px solid rgba(0, 212, 255, 0.35)",
                  }}
                >
                  <div
                    style={{
                      height: "100%",
                      width: `${(telemetry.shieldHp / telemetry.maxShieldHp) * 100}%`,
                      background:
                        telemetry.shieldHp > 40
                          ? "linear-gradient(90deg, #0052ff, #00d4ff)"
                          : "linear-gradient(90deg, #ff3366, #ffaa00)",
                      transition: "width 0.15s ease",
                    }}
                  />
                </div>
                <span
                  style={{
                    fontFamily: "ui-monospace, monospace",
                    fontSize: 12,
                    fontWeight: 700,
                    color: "#ffffff",
                    fontVariantNumeric: "tabular-nums",
                  }}
                >
                  {telemetry.shieldHp}%
                </span>
              </div>

              {/* Overdrive EMP Gauge */}
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <span style={{ fontSize: 11, color: telemetry.empCharge >= 100 ? "#00e699" : "#a1a1aa", fontWeight: 700 }}>
                  ⚡ EMP
                </span>
                <div
                  style={{
                    width: 140,
                    height: 6,
                    borderRadius: 3,
                    background: "rgba(255, 255, 255, 0.1)",
                    overflow: "hidden",
                  }}
                >
                  <div
                    style={{
                      height: "100%",
                      width: `${telemetry.empCharge}%`,
                      background: telemetry.empCharge >= 100 ? "#00e699" : "#00d4ff",
                    }}
                  />
                </div>
                {telemetry.empCharge >= 100 && (
                  <span
                    style={{
                      fontSize: 10,
                      fontWeight: 800,
                      color: "#00e699",
                      background: "rgba(0, 230, 153, 0.2)",
                      padding: "1px 5px",
                      borderRadius: 4,
                    }}
                  >
                    READY [E]
                  </span>
                )}
              </div>

              {/* Score Display */}
              <div style={{ marginTop: 2 }}>
                <div style={{ fontSize: 10, color: "rgba(255, 255, 255, 0.5)", textTransform: "uppercase", letterSpacing: "0.08em" }}>
                  Defense Score
                </div>
                <div
                  style={{
                    fontFamily: "ui-monospace, monospace",
                    fontSize: 24,
                    fontWeight: 900,
                    color: "#ffffff",
                    fontVariantNumeric: "tabular-nums",
                    textShadow: "0 0 12px rgba(0, 212, 255, 0.6)",
                  }}
                >
                  {telemetry.score.toLocaleString()}
                </div>
              </div>
            </div>

            {/* Top-Right: Wave Block & Multiplier */}
            <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: 4 }}>
              <div
                style={{
                  display: "inline-flex",
                  alignItems: "center",
                  gap: 6,
                  padding: "4px 10px",
                  borderRadius: 8,
                  background: "rgba(0, 82, 255, 0.2)",
                  border: "1px solid rgba(0, 82, 255, 0.45)",
                  color: "#00d4ff",
                  fontSize: 12,
                  fontWeight: 700,
                }}
              >
                <span>🌐 BASE BLOCK #{8453000 + telemetry.wave}</span>
              </div>

              <div style={{ display: "flex", gap: 8, alignItems: "center", marginTop: 2 }}>
                <span
                  style={{
                    fontFamily: "ui-monospace, monospace",
                    fontSize: 13,
                    fontWeight: 800,
                    color: "#ffd700",
                    fontVariantNumeric: "tabular-nums",
                  }}
                >
                  🔥 {telemetry.multiplier.toFixed(1)}x
                </span>
                <span style={{ fontSize: 11, color: "rgba(255,255,255,0.4)" }}>
                  HIGH: {telemetry.highScore.toLocaleString()}
                </span>
              </div>
            </div>
          </div>

          {/* Bottom Perimeter: Weapon Status, Shards, Controls */}
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end" }}>
            {/* Left: Weapon status & Shards */}
            <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
              <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <span style={{ fontSize: 12 }}>💎</span>
                <span style={{ fontSize: 12, fontWeight: 700, color: "#ffd700" }}>
                  {telemetry.shardsCollected} AGL Shards
                </span>
              </div>
              <div style={{ fontSize: 10, color: "rgba(255, 255, 255, 0.5)" }}>
                {telemetry.weaponLevel >= 2 ? "⚡ OVERCLOCKED PLASMA BLASTER" : "🔹 DUAL PLASMA BLASTER"}
              </div>
            </div>

            {/* Right: Quick Action Buttons (pointer-events: auto) */}
            <div style={{ pointerEvents: "auto", display: "flex", gap: 8 }}>
              {/* Virtual EMP Button for Mobile */}
              <button
                onClick={handleTriggerEmp}
                disabled={telemetry.empCharge < 100}
                style={{
                  background: telemetry.empCharge >= 100 ? "rgba(0, 230, 153, 0.25)" : "rgba(255, 255, 255, 0.08)",
                  border: telemetry.empCharge >= 100 ? "1px solid #00e699" : "1px solid rgba(255, 255, 255, 0.15)",
                  color: telemetry.empCharge >= 100 ? "#00e699" : "rgba(255, 255, 255, 0.4)",
                  padding: "8px 12px",
                  borderRadius: 10,
                  fontSize: 12,
                  fontWeight: 800,
                  cursor: telemetry.empCharge >= 100 ? "pointer" : "not-allowed",
                }}
              >
                ⚡ EMP
              </button>

              {/* Sound Mute Toggle */}
              <button
                onClick={handleToggleMute}
                style={{
                  background: "rgba(255, 255, 255, 0.08)",
                  border: "1px solid rgba(255, 255, 255, 0.15)",
                  color: isMuted ? "#ff3366" : "#ffffff",
                  padding: "8px 10px",
                  borderRadius: 10,
                  fontSize: 13,
                  cursor: "pointer",
                }}
                title={isMuted ? "Unmute" : "Mute Sound"}
              >
                {isMuted ? "🔇" : "🔊"}
              </button>

              {/* Pause Button */}
              <button
                onClick={handleTogglePause}
                style={{
                  background: "rgba(255, 255, 255, 0.08)",
                  border: "1px solid rgba(255, 255, 255, 0.15)",
                  color: "#ffffff",
                  padding: "8px 12px",
                  borderRadius: 10,
                  fontSize: 12,
                  fontWeight: 700,
                  cursor: "pointer",
                }}
              >
                ⏸️ Pause
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 3. Title Screen Overlay (With bespoke SVG title lockup over 35% negative space) */}
      {phase === "TITLE" && (
        <div
          style={{
            position: "absolute",
            inset: 0,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            background: "radial-gradient(circle at center, rgba(0, 82, 255, 0.12) 0%, rgba(3, 7, 18, 0.85) 70%)",
            padding: "24px",
            textAlign: "center",
          }}
        >
          {/* Bespoke SVG Title Lockup */}
          <div style={{ maxWidth: 440, width: "100%", marginBottom: 16 }}>
            <svg viewBox="0 0 440 100" style={{ width: "100%", height: "auto" }}>
              <defs>
                <linearGradient id="cyberGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stopColor="#00d4ff" />
                  <stop offset="50%" stopColor="#0052ff" />
                  <stop offset="100%" stopColor="#00e699" />
                </linearGradient>
                <filter id="neonGlow" x="-20%" y="-20%" width="140%" height="140%">
                  <feGaussianBlur stdDeviation="3" result="blur" />
                  <feMerge>
                    <feMergeNode in="blur" />
                    <feMergeNode in="SourceGraphic" />
                  </feMerge>
                </filter>
              </defs>

              <text
                x="220"
                y="38"
                textAnchor="middle"
                fill="url(#cyberGrad)"
                filter="url(#neonGlow)"
                style={{
                  fontFamily: 'system-ui, -apple-system, sans-serif',
                  fontWeight: 900,
                  fontSize: "36px",
                  letterSpacing: "0.18em",
                }}
              >
                CYBER SENTINEL
              </text>
              <text
                x="220"
                y="68"
                textAnchor="middle"
                fill="#ffd700"
                style={{
                  fontFamily: 'ui-monospace, monospace',
                  fontWeight: 700,
                  fontSize: "13px",
                  letterSpacing: "0.3em",
                }}
              >
                ⚡ BASE L2 DEFENSE PROTOCOL ⚡
              </text>
            </svg>
          </div>

          <div
            style={{
              fontSize: 14,
              color: "rgba(255, 255, 255, 0.75)",
              maxWidth: 420,
              lineHeight: 1.5,
              marginBottom: 32,
            }}
          >
            Intercept sybil drones and drainer bots infiltrating the Base Mainnet mempool.
            Defend smart contract state nodes, extract AGL gas shards, and unleash Overdrive EMP blasts.
          </div>

          {/* Primary Start CTA Button */}
          <button
            onClick={handleStartGame}
            style={{
              background: "linear-gradient(135deg, #0052ff 0%, #00d4ff 100%)",
              color: "#ffffff",
              border: "none",
              borderRadius: 14,
              padding: "16px 36px",
              fontSize: 16,
              fontWeight: 800,
              cursor: "pointer",
              boxShadow: "0 0 25px rgba(0, 212, 255, 0.45)",
              letterSpacing: "0.05em",
              display: "flex",
              alignItems: "center",
              gap: 10,
              marginBottom: 20,
              transform: "scale(1)",
              transition: "transform 0.1s ease",
            }}
            onMouseDown={(e) => (e.currentTarget.style.transform = "scale(0.96)")}
            onMouseUp={(e) => (e.currentTarget.style.transform = "scale(1)")}
          >
            <span>🚀 DEPLOY SENTINEL</span>
            <span style={{ fontSize: 12, opacity: 0.85, background: "rgba(0,0,0,0.25)", padding: "2px 8px", borderRadius: 6 }}>
              SPACE / ENTER
            </span>
          </button>

          {/* Single Flat Control-Hint Line */}
          <div
            style={{
              fontFamily: "ui-monospace, monospace",
              fontSize: 12,
              color: "rgba(255, 255, 255, 0.45)",
              letterSpacing: "0.04em",
              marginBottom: 28,
            }}
          >
            WASD / Arrows Move · Space Shoot · E Trigger EMP · Esc Pause
          </div>

          {/* Return to Command Center */}
          <button
            onClick={onExit}
            style={{
              background: "none",
              border: "1px solid rgba(255, 255, 255, 0.2)",
              color: "rgba(255, 255, 255, 0.65)",
              borderRadius: 10,
              padding: "8px 18px",
              fontSize: 13,
              cursor: "pointer",
            }}
          >
            ← Return to AGL Command Center
          </button>
        </div>
      )}

      {/* 4. Pause Modal */}
      {phase === "PAUSED" && (
        <div
          style={{
            position: "absolute",
            inset: 0,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            background: "rgba(3, 7, 18, 0.85)",
            zIndex: 1000,
          }}
        >
          <div
            style={{
              background: "#0c1222",
              border: "1px solid rgba(0, 212, 255, 0.35)",
              borderRadius: 20,
              padding: "28px 32px",
              width: "90%",
              maxWidth: 360,
              textAlign: "center",
              boxShadow: "0 20px 40px rgba(0,0,0,0.8)",
            }}
          >
            <div style={{ fontSize: 28, marginBottom: 8 }}>⏸️</div>
            <div style={{ fontSize: 20, fontWeight: 800, color: "#ffffff", marginBottom: 6 }}>SYSTEM PAUSED</div>
            <div style={{ fontSize: 12, color: "rgba(255,255,255,0.5)", marginBottom: 20 }}>
              Base Sentinel telemetry paused at Block #{8453000 + telemetry.wave}
            </div>

            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              <button
                onClick={handleTogglePause}
                style={{
                  background: "var(--blue, #0052ff)",
                  color: "#ffffff",
                  border: "none",
                  borderRadius: 12,
                  padding: "12px",
                  fontWeight: 700,
                  fontSize: 14,
                  cursor: "pointer",
                }}
              >
                ▶️ Resume Simulation
              </button>

              <button
                onClick={handleToggleMute}
                style={{
                  background: "rgba(255, 255, 255, 0.08)",
                  color: "#ffffff",
                  border: "1px solid rgba(255, 255, 255, 0.15)",
                  borderRadius: 12,
                  padding: "10px",
                  fontSize: 13,
                  cursor: "pointer",
                }}
              >
                {isMuted ? "🔇 Unmute Audio" : "🔊 Audio Muted"}
              </button>

              <button
                onClick={handleStartGame}
                style={{
                  background: "rgba(255, 255, 255, 0.08)",
                  color: "#ffffff",
                  border: "1px solid rgba(255, 255, 255, 0.15)",
                  borderRadius: 12,
                  padding: "10px",
                  fontSize: 13,
                  cursor: "pointer",
                }}
              >
                🔄 Restart Mission
              </button>

              <button
                onClick={onExit}
                style={{
                  background: "none",
                  color: "rgba(255, 51, 102, 0.85)",
                  border: "none",
                  padding: "10px",
                  fontSize: 13,
                  cursor: "pointer",
                  marginTop: 6,
                }}
              >
                Abort & Exit Game
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 5. Game Over Modal */}
      {phase === "GAMEOVER" && (
        <div
          style={{
            position: "absolute",
            inset: 0,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            background: "rgba(3, 7, 18, 0.9)",
            zIndex: 1000,
            padding: "20px",
          }}
        >
          <div
            style={{
              background: "#0c1222",
              border: "1px solid rgba(255, 51, 102, 0.4)",
              borderRadius: 20,
              padding: "28px 32px",
              width: "100%",
              maxWidth: 380,
              textAlign: "center",
              boxShadow: "0 20px 40px rgba(0,0,0,0.9)",
            }}
          >
            <div style={{ fontSize: 32, marginBottom: 8 }}>💥</div>
            <div style={{ fontSize: 22, fontWeight: 900, color: "#ff3366", marginBottom: 4 }}>
              SENTINEL COMPROMISED
            </div>
            <div style={{ fontSize: 12, color: "rgba(255,255,255,0.5)", marginBottom: 20 }}>
              Node breached at Base Block #{8453000 + telemetry.wave}
            </div>

            {/* Scorecard */}
            <div
              style={{
                background: "rgba(0, 0, 0, 0.4)",
                borderRadius: 14,
                padding: "14px 18px",
                marginBottom: 20,
                display: "grid",
                gridTemplateColumns: "1fr 1fr",
                gap: 12,
                textAlign: "left",
              }}
            >
              <div>
                <div style={{ fontSize: 10, color: "rgba(255,255,255,0.4)", textTransform: "uppercase" }}>Final Score</div>
                <div style={{ fontSize: 18, fontWeight: 900, color: "#ffffff", fontFamily: "ui-monospace, monospace" }}>
                  {telemetry.score.toLocaleString()}
                </div>
              </div>
              <div>
                <div style={{ fontSize: 10, color: "rgba(255,255,255,0.4)", textTransform: "uppercase" }}>High Score</div>
                <div style={{ fontSize: 18, fontWeight: 900, color: "#ffd700", fontFamily: "ui-monospace, monospace" }}>
                  {telemetry.highScore.toLocaleString()}
                </div>
              </div>
              <div>
                <div style={{ fontSize: 10, color: "rgba(255,255,255,0.4)", textTransform: "uppercase" }}>Shards Extracted</div>
                <div style={{ fontSize: 15, fontWeight: 800, color: "#00d4ff" }}>
                  💎 {telemetry.shardsCollected} AGL
                </div>
              </div>
              <div>
                <div style={{ fontSize: 10, color: "rgba(255,255,255,0.4)", textTransform: "uppercase" }}>Blocks Defended</div>
                <div style={{ fontSize: 15, fontWeight: 800, color: "#00e699" }}>
                  🛡️ {telemetry.wave} Blocks
                </div>
              </div>
            </div>

            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              <button
                onClick={handleStartGame}
                style={{
                  background: "linear-gradient(135deg, #0052ff 0%, #00d4ff 100%)",
                  color: "#ffffff",
                  border: "none",
                  borderRadius: 12,
                  padding: "14px",
                  fontWeight: 800,
                  fontSize: 15,
                  cursor: "pointer",
                }}
              >
                🔄 Deploy Again [ENTER / SPACE]
              </button>

              <button
                onClick={onExit}
                style={{
                  background: "rgba(255, 255, 255, 0.08)",
                  color: "rgba(255, 255, 255, 0.8)",
                  border: "1px solid rgba(255, 255, 255, 0.15)",
                  borderRadius: 12,
                  padding: "12px",
                  fontSize: 13,
                  fontWeight: 600,
                  cursor: "pointer",
                }}
              >
                ← Return to Command Center
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
