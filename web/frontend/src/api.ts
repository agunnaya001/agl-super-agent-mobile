// API client for the AGL Super Agent backend.

const BASE = "/api";

async function get<T>(path: string): Promise<T> {
  const r = await fetch(`${BASE}${path}`);
  if (!r.ok) throw new Error(`GET ${path} failed: ${r.status}`);
  return r.json();
}

async function post<T>(path: string, body: unknown): Promise<T> {
  const r = await fetch(`${BASE}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return r.json();
}

export const api = {
  getStatus: () => get<any>("/status"),
  getDiagnostics: () => get<any>("/diagnostics"),
  getEcosystem: () => get<any>("/ecosystem"),
  getOracle: () => get<any>("/oracle"),
  simulateOracle: (priceUsd: number | null) => post<any>("/oracle/simulate", { priceUsd }),

  getAglToken: () => get<any>("/agl/token"),
  getWallet: (a: string) => get<any>(`/wallet/${a}`),
  getTokens: (a: string) => get<any[]>(`/wallet/${a}/tokens`),
  getPortfolio: (a: string) => get<any>(`/wallet/${a}/portfolio`),
  getTransactions: (a: string) => get<any[]>(`/wallet/${a}/transactions`),
  getCredits: (a: string) => get<any>(`/agl/credits/${a}`),
  getWagl: (a: string) => get<any>(`/wagl/${a}`),
  getStaking: () => get<any>("/staking"),
  getStakingPositions: (a: string) => get<any[]>(`/staking/${a}/positions`),
  getGovernance: () => get<any>("/governance"),
  getTimelock: () => get<any>("/timelock"),

  getQuests: () => get<any[]>("/quests"),
  getLessons: () => get<any[]>("/lessons"),
  getLeaderboard: (tf: string) => get<any[]>(`/leaderboard?timeframe=${tf}`),
  getProfile: (a: string) => get<any>(`/profile?address=${a}`),
  getAiSuggestions: () => get<any[]>("/ai-suggestions"),
  getFollowUps: () => get<string[]>("/follow-ups"),

  aiChat: (message: string, history: { role: string; text: string }[]) =>
    post<any>("/ai/chat", { message, history }),
  analyzeContract: (address: string) => post<any>("/ai/analyze-contract", { address }),
  auditSecurity: (target: string) => post<any>("/ai/audit-security", { target }),
};
