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

  // Advanced AI
  portfolioReport: (address: string, portfolio: any) => post<any>("/ai/portfolio-report", { address, portfolio }),
  explainTx: (txHash: string, description?: string) => post<any>("/ai/explain-tx", { txHash, description }),
  contractDiff: (addressA: string, addressB: string) => post<any>("/ai/contract-diff", { addressA, addressB }),
  gasOptimizer: (address: string) => post<any>("/ai/gas-optimizer", { address }),
  addressRisk: (address: string) => post<any>("/ai/address-risk", { address }),
  phishingCheck: (target: string) => post<any>("/ai/phishing-check", { target }),

  // Advanced blockchain
  getApprovals: (a: string) => get<any>(`/wallet/${a}/approvals`),
  getWatchlist: () => get<any[]>("/watchlist"),
  addWatchlist: (label: string, address: string, type?: string, tags?: string[]) =>
    post<any>("/watchlist", { label, address, type, tags }),
  removeWatchlist: (id: string) => post<any>(`/watchlist/${id}`, {}),
  stakingCalculate: (amount: number, aprPercent: number, durationDays: number, compound: boolean) =>
    post<any>("/staking/calculate", { amount, aprPercent, durationDays, compound }),
  simulateProposal: (data: any) => post<any>("/governance/simulate", data),
  getDelegations: () => get<any>("/delegation/explorer"),
  getDelegation: (address: string) => get<any>(`/delegation/${address}`),
};
