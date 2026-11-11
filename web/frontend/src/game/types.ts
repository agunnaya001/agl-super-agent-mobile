export type GamePhase = "TITLE" | "PLAYING" | "PAUSED" | "VICTORY" | "GAMEOVER";

export interface Vector2 {
  x: number;
  y: number;
}

export interface PlayerDrone {
  x: number;
  y: number;
  vx: number;
  vy: number;
  width: number;
  height: number;
  hp: number;
  maxHp: number;
  empCharge: number; // 0 to 100
  empMax: number;
  weaponLevel: number;
  fireCooldown: number;
  invulnerableTime: number;
  squashX: number;
  squashY: number;
}

export interface Bullet {
  x: number;
  y: number;
  vx: number;
  vy: number;
  radius: number;
  isPlayer: boolean;
  color: string;
  damage: number;
  life: number;
}

export type EnemyType = "SWARMER" | "DRAINER" | "TITAN_BOSS";

export interface Enemy {
  id: number;
  type: EnemyType;
  x: number;
  y: number;
  vx: number;
  vy: number;
  width: number;
  height: number;
  hp: number;
  maxHp: number;
  scoreValue: number;
  // Windup and telegraph state
  windupTimer: number;
  isTelegraphing: boolean;
  telegraphAngle: number;
  attackCooldown: number;
  // Visual effects
  hitFlashTime: number;
  phase: number;
}

export interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  color: string;
  size: number;
  alpha: number;
  life: number;
  maxLife: number;
}

export interface ShardPickup {
  x: number;
  y: number;
  vx: number;
  vy: number;
  value: number;
  type: "AGL_GAS" | "SHIELD_REPAIR" | "OVERCLOCK";
  color: string;
  pulse: number;
}

export interface FloatingText {
  x: number;
  y: number;
  text: string;
  color: string;
  alpha: number;
  life: number;
}

export interface GameTelemetry {
  score: number;
  highScore: number;
  shardsCollected: number;
  wave: number;
  multiplier: number;
  shieldHp: number;
  maxShieldHp: number;
  empCharge: number;
  weaponLevel: number;
  activeEnemiesCount: number;
  fps: number;
}
