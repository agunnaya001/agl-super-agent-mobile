import { sound } from "./audio";
import {
  Bullet,
  Enemy,
  FloatingText,
  GamePhase,
  GameTelemetry,
  Particle,
  PlayerDrone,
  ShardPickup,
} from "./types";

export class CyberSentinelEngine {
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private animFrameId: number | null = null;
  private lastTime: number = 0;

  // Simulation state
  public phase: GamePhase = "TITLE";
  public score: number = 0;
  public highScore: number = 0;
  public shardsCollected: number = 0;
  public wave: number = 1;
  public multiplier: number = 1.0;
  public waveTimer: number = 0;
  public waveKills: number = 0;
  public waveTargetKills: number = 12;

  // Game feel
  public screenShake: number = 0;
  public hitstopTimer: number = 0;
  private nextEnemyId: number = 1;

  // Entities
  public player: PlayerDrone;
  public bullets: Bullet[] = [];
  public enemies: Enemy[] = [];
  public particles: Particle[] = [];
  public pickups: ShardPickup[] = [];
  public floaters: FloatingText[] = [];

  // Starfield background
  private stars: { x: number; y: number; speed: number; size: number; alpha: number }[] = [];
  private gridOffset: number = 0;

  // Input system
  public heldKeys: Set<string> = new Set();
  public justPressedKeys: Set<string> = new Set();
  public touchX: number = 0;
  public touchY: number = 0;
  public touchActive: boolean = false;
  public touchFiring: boolean = false;

  // Telemetry callback
  private onTelemetryUpdate?: (data: GameTelemetry) => void;
  private onPhaseChange?: (phase: GamePhase) => void;

  constructor(
    canvas: HTMLCanvasElement,
    onTelemetry?: (data: GameTelemetry) => void,
    onPhaseChange?: (phase: GamePhase) => void
  ) {
    this.canvas = canvas;
    const context = canvas.getContext("2d", { alpha: false });
    if (!context) throw new Error("Could not get 2D canvas context");
    this.ctx = context;
    this.onTelemetryUpdate = onTelemetry;
    this.onPhaseChange = onPhaseChange;

    // Load highscore from localStorage if available
    try {
      const saved = localStorage.getItem("agl_sentinel_high_score");
      if (saved) this.highScore = parseInt(saved, 10) || 0;
    } catch {}

    // Init player
    this.player = this.createPlayer();
    this.initStarfield();
    this.bindEvents();
    this.resizeCanvas();
  }

  private createPlayer(): PlayerDrone {
    return {
      x: this.canvas.width / 2,
      y: this.canvas.height - 120,
      vx: 0,
      vy: 0,
      width: 32,
      height: 38,
      hp: 100,
      maxHp: 100,
      empCharge: 100,
      empMax: 100,
      weaponLevel: 1,
      fireCooldown: 0,
      invulnerableTime: 0,
      squashX: 1,
      squashY: 1,
    };
  }

  private initStarfield() {
    this.stars = [];
    const count = 75;
    for (let i = 0; i < count; i++) {
      this.stars.push({
        x: Math.random() * (this.canvas.width || 800),
        y: Math.random() * (this.canvas.height || 600),
        speed: 0.5 + Math.random() * 2.5,
        size: Math.random() > 0.8 ? 2 : 1,
        alpha: 0.2 + Math.random() * 0.7,
      });
    }
  }

  private bindEvents() {
    window.addEventListener("keydown", this.handleKeyDown);
    window.addEventListener("keyup", this.handleKeyUp);
    window.addEventListener("blur", this.handleBlur);
    window.addEventListener("resize", this.handleResize);
  }

  public destroy() {
    if (this.animFrameId) cancelAnimationFrame(this.animFrameId);
    window.removeEventListener("keydown", this.handleKeyDown);
    window.removeEventListener("keyup", this.handleKeyUp);
    window.removeEventListener("blur", this.handleBlur);
    window.removeEventListener("resize", this.handleResize);
  }

  private handleKeyDown = (e: KeyboardEvent) => {
    const key = e.key.toLowerCase();
    if (!this.heldKeys.has(key)) {
      this.justPressedKeys.add(key);
    }
    this.heldKeys.add(key);

    // Prevent scrolling with arrows/space
    if (["arrowup", "arrowdown", "arrowleft", "arrowright", " ", "space"].includes(key)) {
      e.preventDefault();
    }

    // Edge-triggered pause
    if (key === "escape") {
      this.togglePause();
    }

    // Edge-triggered EMP blast on 'e'
    if (key === "e" && this.phase === "PLAYING") {
      this.triggerEmp();
    }

    // Edge-triggered Start on Enter/Space during TITLE
    if ((key === "enter" || key === " ") && this.phase === "TITLE") {
      this.startGame();
    }
  };

  private handleKeyUp = (e: KeyboardEvent) => {
    const key = e.key.toLowerCase();
    this.heldKeys.delete(key);
  };

  private handleBlur = () => {
    // Clear all held inputs on window blur to prevent sticky keys
    this.heldKeys.clear();
    this.justPressedKeys.clear();
    this.touchActive = false;
    this.touchFiring = false;
  };

  private handleResize = () => {
    this.resizeCanvas();
  };

  public resizeCanvas() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const rect = this.canvas.getBoundingClientRect();
    const w = rect.width || window.innerWidth;
    const h = rect.height || window.innerHeight;

    if (this.canvas.width !== Math.floor(w * dpr) || this.canvas.height !== Math.floor(h * dpr)) {
      this.canvas.width = Math.floor(w * dpr);
      this.canvas.height = Math.floor(h * dpr);
      this.ctx.scale(dpr, dpr);
    }
  }

  // Phase controls
  public startGame() {
    this.phase = "PLAYING";
    this.score = 0;
    this.shardsCollected = 0;
    this.wave = 1;
    this.multiplier = 1.0;
    this.waveKills = 0;
    this.waveTargetKills = 10;
    this.waveTimer = 0;

    this.player = this.createPlayer();
    this.bullets = [];
    this.enemies = [];
    this.particles = [];
    this.pickups = [];
    this.floaters = [];

    this.spawnFloatingText(this.canvas.width / (2 * (window.devicePixelRatio || 1)), 240, "BLOCK #8453001 INFILTRATION", "#00d4ff");
    sound.playWaveClear();
    this.onPhaseChange?.("PLAYING");
  }

  public togglePause() {
    if (this.phase === "PLAYING") {
      this.phase = "PAUSED";
      this.onPhaseChange?.("PAUSED");
    } else if (this.phase === "PAUSED") {
      this.phase = "PLAYING";
      this.onPhaseChange?.("PLAYING");
    }
  }

  public triggerEmp() {
    if (this.player.empCharge < 100) return;
    this.player.empCharge = 0;
    this.screenShake = 18;
    this.hitstopTimer = 0.08;
    sound.playEmp();

    // Clear all enemy bullets
    this.bullets = this.bullets.filter((b) => b.isPlayer);

    // Blast all enemies on screen
    for (const enemy of this.enemies) {
      enemy.hp -= 85;
      enemy.hitFlashTime = 0.15;
      this.createExplosionParticles(enemy.x, enemy.y, "#00d4ff", 14);
    }

    // EMP Shockwave visual burst
    for (let i = 0; i < 36; i++) {
      const angle = (i / 36) * Math.PI * 2;
      const speed = 7 + Math.random() * 5;
      this.particles.push({
        x: this.player.x,
        y: this.player.y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        color: "#00d4ff",
        size: 4 + Math.random() * 3,
        alpha: 1,
        life: 0,
        maxLife: 0.45,
      });
    }

    this.spawnFloatingText(this.player.x, this.player.y - 30, "⚡ OVERDRIVE EMP BLAST!", "#00e699");
  }

  public startLoop() {
    this.lastTime = performance.now();
    const loop = (now: number) => {
      const rawDt = (now - this.lastTime) / 1000;
      this.lastTime = now;
      // Mandated: clamp frame delta time to prevent physics clipping
      const dt = Math.min(rawDt, 0.05);

      this.update(dt);
      this.render();

      this.animFrameId = requestAnimationFrame(loop);
    };
    this.animFrameId = requestAnimationFrame(loop);
  }

  // 60fps Simulation Update
  private update(dt: number) {
    const width = this.canvas.width / (window.devicePixelRatio || 1);
    const height = this.canvas.height / (window.devicePixelRatio || 1);

    // Hitstop freeze handling
    if (this.hitstopTimer > 0) {
      this.hitstopTimer -= dt;
      return;
    }

    // Screen shake decay
    if (this.screenShake > 0) {
      this.screenShake = Math.max(0, this.screenShake - dt * 25);
    }

    // Background starfield scroll
    this.gridOffset = (this.gridOffset + dt * 45) % 40;
    for (const star of this.stars) {
      star.y += star.speed * (this.phase === "PLAYING" ? 1.5 : 0.6);
      if (star.y > height) {
        star.y = 0;
        star.x = Math.random() * width;
      }
    }

    // Only simulate gameplay during PLAYING (or background idle preview in TITLE)
    if (this.phase === "PLAYING") {
      this.updatePlayer(dt, width, height);
      this.updateBullets(dt, width, height);
      this.updateEnemies(dt, width, height);
      this.updatePickups(dt);
      this.waveManager(dt, width);
    } else if (this.phase === "TITLE") {
      // Idle attract mode: spawn gentle ambient background cyber pulses
      if (Math.random() < 0.04 && this.particles.length < 50) {
        this.particles.push({
          x: Math.random() * width,
          y: height + 10,
          vx: (Math.random() - 0.5) * 1.5,
          vy: -1.5 - Math.random() * 2,
          color: Math.random() > 0.5 ? "#00d4ff" : "#0052ff",
          size: 2 + Math.random() * 3,
          alpha: 0.8,
          life: 0,
          maxLife: 2.5,
        });
      }
    }

    this.updateParticles(dt);
    this.updateFloaters(dt);

    // Report telemetry to React overlay
    this.onTelemetryUpdate?.({
      score: this.score,
      highScore: Math.max(this.score, this.highScore),
      shardsCollected: this.shardsCollected,
      wave: this.wave,
      multiplier: Number(this.multiplier.toFixed(1)),
      shieldHp: Math.max(0, Math.round(this.player.hp)),
      maxShieldHp: this.player.maxHp,
      empCharge: Math.min(100, Math.round(this.player.empCharge)),
      weaponLevel: this.player.weaponLevel,
      activeEnemiesCount: this.enemies.length,
      fps: dt > 0 ? Math.round(1 / dt) : 60,
    });

    // Clear edge-triggered keys at end of tick
    this.justPressedKeys.clear();
  }

  private updatePlayer(dt: number, width: number, height: number) {
    const p = this.player;

    // Movement input
    let moveX = 0;
    let moveY = 0;

    if (this.heldKeys.has("a") || this.heldKeys.has("arrowleft")) moveX -= 1;
    if (this.heldKeys.has("d") || this.heldKeys.has("arrowright")) moveX += 1;
    if (this.heldKeys.has("w") || this.heldKeys.has("arrowup")) moveY -= 1;
    if (this.heldKeys.has("s") || this.heldKeys.has("arrowdown")) moveY += 1;

    // Touch joystick support
    if (this.touchActive) {
      const dx = this.touchX - p.x;
      const dy = this.touchY - p.y;
      const dist = Math.hypot(dx, dy);
      if (dist > 8) {
        moveX = dx / dist;
        moveY = dy / dist;
      }
    }

    // Normalize diagonal
    if (moveX !== 0 && moveY !== 0) {
      const len = Math.hypot(moveX, moveY);
      moveX /= len;
      moveY /= len;
    }

    const accel = 1800;
    const friction = 8.5;

    p.vx += moveX * accel * dt;
    p.vy += moveY * accel * dt;
    p.vx -= p.vx * friction * dt;
    p.vy -= p.vy * friction * dt;

    p.x += p.vx * dt;
    p.y += p.vy * dt;

    // Screen bounds clamp
    const pad = 24;
    p.x = Math.max(pad, Math.min(width - pad, p.x));
    p.y = Math.max(pad, Math.min(height - pad, p.y));

    // Squash and stretch decay
    p.squashX += (1 - p.squashX) * dt * 14;
    p.squashY += (1 - p.squashY) * dt * 14;

    // EMP passive recharge
    if (p.empCharge < p.empMax) {
      p.empCharge = Math.min(p.empMax, p.empCharge + dt * 4.5);
    }

    // Shield passive regeneration
    if (p.hp < p.maxHp && p.invulnerableTime <= 0) {
      p.hp = Math.min(p.maxHp, p.hp + dt * 2.2);
    }

    if (p.invulnerableTime > 0) {
      p.invulnerableTime = Math.max(0, p.invulnerableTime - dt);
    }

    // Engine thruster particles
    if (Math.random() < 0.6) {
      this.particles.push({
        x: p.x + (Math.random() - 0.5) * 10,
        y: p.y + 16,
        vx: (Math.random() - 0.5) * 20,
        vy: 40 + Math.random() * 60,
        color: Math.random() > 0.4 ? "#00d4ff" : "#0052ff",
        size: 2.5 + Math.random() * 2,
        alpha: 0.9,
        life: 0,
        maxLife: 0.25,
      });
    }

    // Firing logic
    p.fireCooldown -= dt;
    const wantsToFire = this.heldKeys.has(" ") || this.heldKeys.has("space") || this.touchFiring;

    if (wantsToFire && p.fireCooldown <= 0) {
      this.firePlayerWeapon();
      p.fireCooldown = p.weaponLevel >= 2 ? 0.13 : 0.17;
      p.squashX = 0.85;
      p.squashY = 1.25;
    }
  }

  private firePlayerWeapon() {
    const p = this.player;
    sound.playLaser(p.weaponLevel >= 2 ? 960 : 840);

    if (p.weaponLevel === 1) {
      // Dual blaster
      this.bullets.push({
        x: p.x - 8,
        y: p.y - 14,
        vx: 0,
        vy: -720,
        radius: 3.5,
        isPlayer: true,
        color: "#00d4ff",
        damage: 25,
        life: 1.5,
      });
      this.bullets.push({
        x: p.x + 8,
        y: p.y - 14,
        vx: 0,
        vy: -720,
        radius: 3.5,
        isPlayer: true,
        color: "#00d4ff",
        damage: 25,
        life: 1.5,
      });
    } else {
      // Triple angled plasma blast
      this.bullets.push({
        x: p.x,
        y: p.y - 16,
        vx: 0,
        vy: -760,
        radius: 4,
        isPlayer: true,
        color: "#00e699",
        damage: 32,
        life: 1.5,
      });
      this.bullets.push({
        x: p.x - 10,
        y: p.y - 12,
        vx: -90,
        vy: -740,
        radius: 3.5,
        isPlayer: true,
        color: "#00d4ff",
        damage: 26,
        life: 1.5,
      });
      this.bullets.push({
        x: p.x + 10,
        y: p.y - 12,
        vx: 90,
        vy: -740,
        radius: 3.5,
        isPlayer: true,
        color: "#00d4ff",
        damage: 26,
        life: 1.5,
      });
    }
  }

  private updateBullets(dt: number, width: number, height: number) {
    for (let i = this.bullets.length - 1; i >= 0; i--) {
      const b = this.bullets[i];
      b.x += b.vx * dt;
      b.y += b.vy * dt;
      b.life -= dt;

      // Despawn offscreen
      if (b.y < -30 || b.y > height + 30 || b.x < -30 || b.x > width + 30 || b.life <= 0) {
        this.bullets.splice(i, 1);
        continue;
      }

      // Check collision
      if (b.isPlayer) {
        for (let j = this.enemies.length - 1; j >= 0; j--) {
          const e = this.enemies[j];
          const dist = Math.hypot(b.x - e.x, b.y - e.y);
          if (dist < b.radius + e.width / 2) {
            // Hit enemy
            e.hp -= b.damage;
            e.hitFlashTime = 0.08;
            sound.playHit(false);
            this.createImpactSparks(b.x, b.y, b.color);

            this.bullets.splice(i, 1);

            if (e.hp <= 0) {
              this.destroyEnemy(e, j);
            }
            break;
          }
        }
      } else {
        // Enemy bullet hits player
        const dist = Math.hypot(b.x - this.player.x, b.y - this.player.y);
        if (dist < b.radius + this.player.width / 2.5 && this.player.invulnerableTime <= 0) {
          this.damagePlayer(b.damage);
          this.bullets.splice(i, 1);
        }
      }
    }
  }

  private damagePlayer(amount: number) {
    this.player.hp -= amount;
    this.player.invulnerableTime = 0.6;
    this.player.squashX = 1.35;
    this.player.squashY = 0.75;
    this.screenShake = 12;
    this.hitstopTimer = 0.05;
    sound.playHit(true);
    this.createExplosionParticles(this.player.x, this.player.y, "#ff3366", 10);

    // Multiplier penalty
    this.multiplier = 1.0;

    if (this.player.hp <= 0) {
      this.player.hp = 0;
      this.gameOver();
    }
  }

  private gameOver() {
    this.phase = "GAMEOVER";
    sound.playGameOver();
    this.screenShake = 22;
    this.createExplosionParticles(this.player.x, this.player.y, "#ff3366", 26);
    this.createExplosionParticles(this.player.x, this.player.y, "#00d4ff", 20);

    if (this.score > this.highScore) {
      this.highScore = this.score;
      try {
        localStorage.setItem("agl_sentinel_high_score", this.highScore.toString());
      } catch {}
    }

    this.onPhaseChange?.("GAMEOVER");
  }

  private updateEnemies(dt: number, width: number, height: number) {
    for (let i = this.enemies.length - 1; i >= 0; i--) {
      const e = this.enemies[i];
      e.phase += dt;

      if (e.hitFlashTime > 0) {
        e.hitFlashTime = Math.max(0, e.hitFlashTime - dt);
      }

      if (e.type === "SWARMER") {
        // Fast zigzag motion down the screen
        e.x += Math.sin(e.phase * 4) * 85 * dt;
        e.y += e.vy * dt;

        // Occasional light blaster shot
        e.attackCooldown -= dt;
        if (e.attackCooldown <= 0 && Math.random() < 0.4) {
          e.attackCooldown = 1.8 + Math.random() * 1.2;
          this.bullets.push({
            x: e.x,
            y: e.y + 12,
            vx: (Math.random() - 0.5) * 40,
            vy: 240,
            radius: 3,
            isPlayer: false,
            color: "#ff3366",
            damage: 15,
            life: 3.5,
          });
        }
      } else if (e.type === "DRAINER") {
        // Interceptor: hover, display windup telegraph line, then high-speed dash!
        e.windupTimer -= dt;
        if (e.windupTimer <= 0.6 && !e.isTelegraphing) {
          e.isTelegraphing = true;
          e.telegraphAngle = Math.atan2(this.player.y - e.y, this.player.x - e.x);
        }

        if (e.windupTimer <= 0) {
          // Dash thrust!
          e.isTelegraphing = false;
          e.vx = Math.cos(e.telegraphAngle) * 380;
          e.vy = Math.sin(e.telegraphAngle) * 380;
          e.windupTimer = 2.4;
          sound.playHit(false);
        }

        e.x += e.vx * dt;
        e.y += e.vy * dt;
        e.vx -= e.vx * 2.5 * dt;
        e.vy -= e.vy * 2.5 * dt;
      } else if (e.type === "TITAN_BOSS") {
        // Dreadnought Boss: Sweeps side-to-side, fires radial bullet curtains
        e.x += Math.sin(e.phase * 1.5) * 110 * dt;
        e.y = Math.min(120, e.y + dt * 25);

        e.attackCooldown -= dt;
        if (e.attackCooldown <= 0) {
          e.attackCooldown = 1.6;
          // Radial bullet burst
          sound.playLaser(340);
          const bulletsCount = 10;
          for (let bIdx = 0; bIdx < bulletsCount; bIdx++) {
            const angle = (bIdx / bulletsCount) * Math.PI * 2 + e.phase;
            this.bullets.push({
              x: e.x,
              y: e.y + 20,
              vx: Math.cos(angle) * 160,
              vy: Math.sin(angle) * 160,
              radius: 4,
              isPlayer: false,
              color: "#ffaa00",
              damage: 22,
              life: 4.5,
            });
          }
        }
      }

      // Despawn if fallen below screen
      if (e.y > height + 60) {
        this.enemies.splice(i, 1);
        continue;
      }

      // Check collision with player
      const dist = Math.hypot(e.x - this.player.x, e.y - this.player.y);
      if (dist < (e.width + this.player.width) / 2.3 && this.player.invulnerableTime <= 0) {
        this.damagePlayer(30);
        e.hp -= 40;
        if (e.hp <= 0) {
          this.destroyEnemy(e, i);
        }
      }
    }
  }

  private destroyEnemy(e: Enemy, index: number) {
    this.enemies.splice(index, 1);
    this.waveKills++;

    const isBoss = e.type === "TITAN_BOSS";
    this.screenShake = isBoss ? 16 : 6;
    this.hitstopTimer = isBoss ? 0.08 : 0.03;
    sound.playExplosion(isBoss);

    // Multiplier & score ramp
    this.multiplier = Math.min(4.0, this.multiplier + 0.1);
    const addedScore = Math.round(e.scoreValue * this.multiplier);
    this.score += addedScore;

    this.createExplosionParticles(e.x, e.y, isBoss ? "#ffaa00" : "#ff3366", isBoss ? 32 : 14);
    this.spawnFloatingText(e.x, e.y - 10, `+${addedScore}`, isBoss ? "#ffd700" : "#00d4ff");

    // Spawn gas shard pickups
    const shardCount = isBoss ? 5 : Math.random() < 0.6 ? 2 : 1;
    for (let s = 0; s < shardCount; s++) {
      this.pickups.push({
        x: e.x + (Math.random() - 0.5) * 20,
        y: e.y + (Math.random() - 0.5) * 20,
        vx: (Math.random() - 0.5) * 80,
        vy: -50 - Math.random() * 80,
        value: 10,
        type: isBoss && s === 0 ? "OVERCLOCK" : Math.random() < 0.15 ? "SHIELD_REPAIR" : "AGL_GAS",
        color: isBoss && s === 0 ? "#00e699" : Math.random() < 0.15 ? "#00e699" : "#ffd700",
        pulse: Math.random() * Math.PI * 2,
      });
    }
  }

  private updatePickups(dt: number) {
    for (let i = this.pickups.length - 1; i >= 0; i--) {
      const p = this.pickups[i];
      p.pulse += dt * 5;

      // Magnet pull toward player if close
      const dx = this.player.x - p.x;
      const dy = this.player.y - p.y;
      const dist = Math.hypot(dx, dy);

      if (dist < 120) {
        p.vx += (dx / dist) * 450 * dt;
        p.vy += (dy / dist) * 450 * dt;
      } else {
        p.vy += 65 * dt; // gravity drift down
      }

      p.x += p.vx * dt;
      p.y += p.vy * dt;

      // Collect by player
      if (dist < 26) {
        sound.playShard();
        this.shardsCollected++;
        this.player.empCharge = Math.min(this.player.empMax, this.player.empCharge + 8);

        if (p.type === "SHIELD_REPAIR") {
          this.player.hp = Math.min(this.player.maxHp, this.player.hp + 25);
          this.spawnFloatingText(p.x, p.y - 12, "SHIELD REPAIR +25", "#00e699");
        } else if (p.type === "OVERCLOCK") {
          this.player.weaponLevel = 2;
          this.spawnFloatingText(p.x, p.y - 12, "⚡ WEAPON OVERCLOCK LVL 2!", "#00d4ff");
        } else {
          this.score += 50;
          this.spawnFloatingText(p.x, p.y - 12, "+1 AGL SHARD", "#ffd700");
        }

        this.createImpactSparks(p.x, p.y, p.color);
        this.pickups.splice(i, 1);
      }
    }
  }

  private waveManager(dt: number, width: number) {
    this.waveTimer += dt;

    // Advance wave once targets killed
    if (this.waveKills >= this.waveTargetKills && this.enemies.length === 0) {
      this.wave++;
      this.waveKills = 0;
      this.waveTargetKills = 10 + this.wave * 4;
      sound.playWaveClear();
      this.spawnFloatingText(width / 2, 220, `BLOCK #845300${this.wave} SECURED!`, "#00e699");

      // Spawn Boss on wave 3 and wave 5
      if (this.wave % 3 === 0) {
        this.spawnTitanBoss(width);
      }
    }

    // Spawn regular enemies on interval
    const spawnInterval = Math.max(0.7, 2.0 - this.wave * 0.18);
    if (this.waveTimer > spawnInterval && this.enemies.length < 8 + this.wave * 2) {
      this.waveTimer = 0;
      this.spawnRandomEnemy(width);
    }
  }

  private spawnRandomEnemy(width: number) {
    const isDrainer = Math.random() < 0.35 + this.wave * 0.05;
    const pad = 40;
    const x = pad + Math.random() * (width - pad * 2);

    if (isDrainer) {
      this.enemies.push({
        id: this.nextEnemyId++,
        type: "DRAINER",
        x,
        y: -30,
        vx: 0,
        vy: 110,
        width: 28,
        height: 32,
        hp: 45 + this.wave * 12,
        maxHp: 45 + this.wave * 12,
        scoreValue: 120,
        windupTimer: 1.5,
        isTelegraphing: false,
        telegraphAngle: 0,
        attackCooldown: 2.0,
        hitFlashTime: 0,
        phase: Math.random() * 5,
      });
    } else {
      this.enemies.push({
        id: this.nextEnemyId++,
        type: "SWARMER",
        x,
        y: -30,
        vx: 0,
        vy: 80 + Math.random() * 40,
        width: 24,
        height: 24,
        hp: 25 + this.wave * 6,
        maxHp: 25 + this.wave * 6,
        scoreValue: 60,
        windupTimer: 0,
        isTelegraphing: false,
        telegraphAngle: 0,
        attackCooldown: 1.5,
        hitFlashTime: 0,
        phase: Math.random() * 5,
      });
    }
  }

  private spawnTitanBoss(width: number) {
    this.enemies.push({
      id: this.nextEnemyId++,
      type: "TITAN_BOSS",
      x: width / 2,
      y: -60,
      vx: 0,
      vy: 40,
      width: 72,
      height: 64,
      hp: 350 + this.wave * 100,
      maxHp: 350 + this.wave * 100,
      scoreValue: 800,
      windupTimer: 0,
      isTelegraphing: false,
      telegraphAngle: 0,
      attackCooldown: 1.2,
      hitFlashTime: 0,
      phase: 0,
    });
    this.spawnFloatingText(width / 2, 180, "⚠️ FLASH-LOAN DREADNOUGHT DETECTED!", "#ffaa00");
  }

  private updateParticles(dt: number) {
    for (let i = this.particles.length - 1; i >= 0; i--) {
      const p = this.particles[i];
      p.x += p.vx * dt;
      p.y += p.vy * dt;
      p.life += dt;
      p.alpha = Math.max(0, 1 - p.life / p.maxLife);

      if (p.life >= p.maxLife) {
        this.particles.splice(i, 1);
      }
    }
  }

  private updateFloaters(dt: number) {
    for (let i = this.floaters.length - 1; i >= 0; i--) {
      const f = this.floaters[i];
      f.y -= dt * 30;
      f.life += dt;
      f.alpha = Math.max(0, 1 - f.life / 0.9);

      if (f.life >= 0.9) {
        this.floaters.splice(i, 1);
      }
    }
  }

  private createImpactSparks(x: number, y: number, color: string) {
    for (let i = 0; i < 5; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 40 + Math.random() * 70;
      this.particles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        color,
        size: 2,
        alpha: 1,
        life: 0,
        maxLife: 0.2,
      });
    }
  }

  private createExplosionParticles(x: number, y: number, color: string, count = 16) {
    for (let i = 0; i < count; i++) {
      const angle = (i / count) * Math.PI * 2 + (Math.random() - 0.5) * 0.4;
      const speed = 60 + Math.random() * 140;
      this.particles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        color: Math.random() > 0.3 ? color : "#ffffff",
        size: 2.5 + Math.random() * 3,
        alpha: 1,
        life: 0,
        maxLife: 0.35 + Math.random() * 0.25,
      });
    }
  }

  public spawnFloatingText(x: number, y: number, text: string, color = "#00d4ff") {
    this.floaters.push({ x, y, text, color, alpha: 1, life: 0 });
  }

  // Canvas 60fps Rendering
  private render() {
    const width = this.canvas.width / (window.devicePixelRatio || 1);
    const height = this.canvas.height / (window.devicePixelRatio || 1);

    this.ctx.save();

    // Screen shake offset
    if (this.screenShake > 0) {
      const sx = (Math.random() - 0.5) * this.screenShake;
      const sy = (Math.random() - 0.5) * this.screenShake;
      this.ctx.translate(sx, sy);
    }

    // 1. Dark futuristic background
    this.ctx.fillStyle = "#030712";
    this.ctx.fillRect(0, 0, width, height);

    // 2. Cyber-grid perspective lines
    this.renderGrid(width, height);

    // 3. Starfield
    for (const star of this.stars) {
      this.ctx.fillStyle = `rgba(0, 212, 255, ${star.alpha})`;
      this.ctx.fillRect(star.x, star.y, star.size, star.size);
    }

    // 4. Shard pickups
    for (const p of this.pickups) {
      this.renderPickup(p);
    }

    // 5. Enemies and windup telegraphs
    for (const e of this.enemies) {
      this.renderEnemy(e);
    }

    // 6. Bullets
    for (const b of this.bullets) {
      this.renderBullet(b);
    }

    // 7. Player Drone
    if (this.phase === "PLAYING" || this.phase === "PAUSED") {
      this.renderPlayer();
    }

    // 8. Particles
    for (const p of this.particles) {
      this.ctx.fillStyle = p.color;
      this.ctx.globalAlpha = p.alpha;
      this.ctx.beginPath();
      this.ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      this.ctx.fill();
    }
    this.ctx.globalAlpha = 1;

    // 9. Floating texts
    this.ctx.textAlign = "center";
    this.ctx.font = "bold 13px ui-monospace, SFMono-Regular, monospace";
    for (const f of this.floaters) {
      this.ctx.fillStyle = f.color;
      this.ctx.globalAlpha = f.alpha;
      this.ctx.fillText(f.text, f.x, f.y);
    }
    this.ctx.globalAlpha = 1;

    this.ctx.restore();
  }

  private renderGrid(width: number, height: number) {
    this.ctx.strokeStyle = "rgba(0, 82, 255, 0.08)";
    this.ctx.lineWidth = 1;

    // Vertical lines
    const colWidth = 60;
    for (let x = 0; x < width; x += colWidth) {
      this.ctx.beginPath();
      this.ctx.moveTo(x, 0);
      this.ctx.lineTo(x, height);
      this.ctx.stroke();
    }

    // Horizontal moving lines
    const rowHeight = 40;
    for (let y = this.gridOffset; y < height; y += rowHeight) {
      this.ctx.beginPath();
      this.ctx.moveTo(0, y);
      this.ctx.lineTo(width, y);
      this.ctx.stroke();
    }
  }

  private renderPlayer() {
    const p = this.player;

    // Blink if invulnerable
    if (p.invulnerableTime > 0 && Math.floor(Date.now() / 60) % 2 === 0) {
      return;
    }

    this.ctx.save();
    this.ctx.translate(p.x, p.y);
    this.ctx.scale(p.squashX, p.squashY);

    // Glowing shield aura
    this.ctx.strokeStyle = "rgba(0, 212, 255, 0.5)";
    this.ctx.lineWidth = 1.5;
    this.ctx.beginPath();
    this.ctx.arc(0, 0, 22, 0, Math.PI * 2);
    this.ctx.stroke();

    // Drone chassis (Aerospace interceptor shape)
    this.ctx.fillStyle = "#0052ff";
    this.ctx.beginPath();
    this.ctx.moveTo(0, -18);
    this.ctx.lineTo(14, 14);
    this.ctx.lineTo(6, 10);
    this.ctx.lineTo(0, 13);
    this.ctx.lineTo(-6, 10);
    this.ctx.lineTo(-14, 14);
    this.ctx.closePath();
    this.ctx.fill();

    // Cockpit cyber-visor
    this.ctx.fillStyle = "#00d4ff";
    this.ctx.beginPath();
    this.ctx.moveTo(0, -12);
    this.ctx.lineTo(4, -2);
    this.ctx.lineTo(0, 2);
    this.ctx.lineTo(-4, -2);
    this.ctx.closePath();
    this.ctx.fill();

    // Wingtip blaster mounts
    this.ctx.fillStyle = p.weaponLevel >= 2 ? "#00e699" : "#00d4ff";
    this.ctx.fillRect(-14, 2, 3, 8);
    this.ctx.fillRect(11, 2, 3, 8);

    this.ctx.restore();
  }

  private renderEnemy(e: Enemy) {
    this.ctx.save();
    this.ctx.translate(e.x, e.y);

    // Flash white on hit
    const color = e.hitFlashTime > 0 ? "#ffffff" : e.type === "TITAN_BOSS" ? "#ffaa00" : "#ff3366";

    // 1. Windup Telegraph Warning Line (Drainer Interceptors)
    if (e.isTelegraphing) {
      this.ctx.save();
      this.ctx.strokeStyle = "rgba(255, 51, 102, 0.65)";
      this.ctx.lineWidth = 2;
      this.ctx.setLineDash([6, 6]);
      this.ctx.beginPath();
      this.ctx.moveTo(0, 0);
      this.ctx.lineTo(
        Math.cos(e.telegraphAngle) * 320,
        Math.sin(e.telegraphAngle) * 320
      );
      this.ctx.stroke();
      this.ctx.restore();
    }

    if (e.type === "SWARMER") {
      // Diamond cyber-bot
      this.ctx.fillStyle = color;
      this.ctx.beginPath();
      this.ctx.moveTo(0, 12);
      this.ctx.lineTo(12, 0);
      this.ctx.lineTo(0, -12);
      this.ctx.lineTo(-12, 0);
      this.ctx.closePath();
      this.ctx.fill();

      // Red core
      this.ctx.fillStyle = "#111827";
      this.ctx.fillRect(-3, -3, 6, 6);
    } else if (e.type === "DRAINER") {
      // Angular predator scout
      this.ctx.fillStyle = color;
      this.ctx.beginPath();
      this.ctx.moveTo(0, 16);
      this.ctx.lineTo(14, -12);
      this.ctx.lineTo(0, -6);
      this.ctx.lineTo(-14, -12);
      this.ctx.closePath();
      this.ctx.fill();

      this.ctx.fillStyle = "#ffd700";
      this.ctx.fillRect(-2, 2, 4, 4);
    } else if (e.type === "TITAN_BOSS") {
      // Heavy Dreadnought Cruiser
      this.ctx.fillStyle = color;
      this.ctx.beginPath();
      this.ctx.moveTo(0, 32);
      this.ctx.lineTo(36, -8);
      this.ctx.lineTo(24, -28);
      this.ctx.lineTo(-24, -28);
      this.ctx.lineTo(-36, -8);
      this.ctx.closePath();
      this.ctx.fill();

      // Boss shield health bar
      const barW = 60;
      const hpPct = Math.max(0, e.hp / e.maxHp);
      this.ctx.fillStyle = "rgba(0, 0, 0, 0.6)";
      this.ctx.fillRect(-barW / 2, -38, barW, 6);
      this.ctx.fillStyle = "#ffaa00";
      this.ctx.fillRect(-barW / 2, -38, barW * hpPct, 6);
    }

    this.ctx.restore();
  }

  private renderBullet(b: Bullet) {
    this.ctx.save();
    this.ctx.fillStyle = b.color;
    this.ctx.shadowColor = b.color;
    this.ctx.shadowBlur = 8;

    this.ctx.beginPath();
    this.ctx.arc(b.x, b.y, b.radius, 0, Math.PI * 2);
    this.ctx.fill();

    this.ctx.restore();
  }

  private renderPickup(p: ShardPickup) {
    this.ctx.save();
    this.ctx.translate(p.x, p.y);
    const scale = 1 + Math.sin(p.pulse) * 0.15;
    this.ctx.scale(scale, scale);

    this.ctx.fillStyle = p.color;
    this.ctx.shadowColor = p.color;
    this.ctx.shadowBlur = 10;

    // Glowing rotating diamond crystal
    this.ctx.beginPath();
    this.ctx.moveTo(0, -8);
    this.ctx.lineTo(6, 0);
    this.ctx.lineTo(0, 8);
    this.ctx.lineTo(-6, 0);
    this.ctx.closePath();
    this.ctx.fill();

    this.ctx.restore();
  }
}
