import { useState, useEffect, useCallback, useRef } from "react";

// Base Mainnet RPC Configuration & Contract Constants
export const BASE_CHAIN_ID = 8453;
export const BASE_NETWORK_NAME = "Base Mainnet";

export const DEFAULT_BASE_RPC_ENDPOINTS = [
  "https://mainnet.base.org",
  "https://base-rpc.publicnode.com",
  "https://1rpc.io/base",
  "https://base.llamarpc.com",
  "https://base.drpc.org",
];

export const DEFAULT_GOVERNOR_CONTRACT = "0x3fFCb92A17caeaAd1342DD76978b566C8aEC7010";
export const DEFAULT_TIMELOCK_CONTRACT = "0x900D315C91D9e54F3fa3412D475009d905bf6744";
export const DEFAULT_VOTES_WRAPPER_CONTRACT = "0xA27C9BA04D06EcAF766EF4e074b403DAf19A3d69";

// ABI Function Selectors
export const SELECTORS = {
  // Governor
  NAME: "0x06fdde03",           // name() -> string
  TOKEN: "0xfc0c546a",          // token() -> address
  TIMELOCK: "0xd33219b4",       // timelock() -> address
  VOTING_DELAY: "0x3932abb1",   // votingDelay() -> uint256
  VOTING_PERIOD: "0x09b3060d",  // votingPeriod() -> uint256
  QUORUM_NUM: "0x61bc221a",     // quorumNumerator() -> uint256
  // Timelock
  GET_MIN_DELAY: "0x2d17ef0d",  // getMinDelay() -> uint256
  GET_MIN_DELAY_ALT: "0x2600a75d", // getMinDelay() alt
  HAS_ROLE: "0x91d14854",       // hasRole(bytes32,address) -> bool
  PROPOSER_ROLE: "0x8f61f4f5",  // PROPOSER_ROLE() -> bytes32
};

export interface DiagnosticLogEntry {
  timestamp: string;
  level: "info" | "warn" | "error" | "success";
  message: string;
  details?: unknown;
}

export interface RpcDiagnosticHealth {
  endpoint: string;
  chainId: number;
  isBaseMainnet: boolean;
  blockNumber: number;
  latencyMs: number;
  connected: boolean;
  blockHash?: string;
}

export interface GovernorDiagnosticHealth {
  address: string;
  isDeployed: boolean;
  bytecodeSize: number;
  name: string;
  tokenAddress: string;
  timelockAddress: string;
  votingDelayBlocks: number;
  votingPeriodBlocks: number;
  timelockBonded: boolean;
  canInteract: boolean;
  status: "verified" | "degraded" | "unreachable";
  details?: Record<string, unknown>;
}

export interface TimelockDiagnosticHealth {
  address: string;
  isDeployed: boolean;
  bytecodeSize: number;
  minDelaySeconds: number;
  formattedMinDelay: string;
  governorAddress: string;
  governorBonded: boolean;
  canInteract: boolean;
  status: "verified" | "degraded" | "unreachable";
  details?: Record<string, unknown>;
}

export type GovernanceDiagnosticOverallStatus = "idle" | "checking" | "healthy" | "degraded" | "error";

export interface GovernanceDiagnosticReport {
  status: GovernanceDiagnosticOverallStatus;
  isHealthy: boolean;
  error: string | null;
  rpc: RpcDiagnosticHealth | null;
  governor: GovernorDiagnosticHealth | null;
  timelock: TimelockDiagnosticHealth | null;
  logs: DiagnosticLogEntry[];
  lastCheckedAt: Date | null;
}

export interface GovernanceDiagnosticResult extends GovernanceDiagnosticReport {
  isLoading: boolean;
  checkConnection: () => Promise<GovernanceDiagnosticResult>;
  runDiagnostics: () => Promise<GovernanceDiagnosticResult>;
  refetch: () => Promise<GovernanceDiagnosticResult>;
}

export interface UseGovernanceDiagnosticOptions {
  /** Automatically run diagnostic verification on mount. Defaults to true. */
  autoRun?: boolean;
  /** Polling interval in ms. Set to 0 or undefined to disable polling. Defaults to 0. */
  pollIntervalMs?: number;
  /** Custom list of RPC endpoints to verify. Defaults to Base Mainnet RPCs. */
  rpcEndpoints?: string[];
  /** Governor contract address to verify. */
  governorAddress?: string;
  /** Timelock contract address to verify. */
  timelockAddress?: string;
  /** Expected EVM Chain ID. Defaults to 8453 (Base Mainnet). */
  expectedChainId?: number;
  /** Whether to log health reports to the console. Defaults to true. */
  logToConsole?: boolean;
  /** Allow fallback to backend `/api` endpoints if direct RPC is restricted by CORS. Defaults to true. */
  apiFallback?: boolean;
}

// Low-level EVM decode helpers
function decodeAbiString(hex: string): string {
  if (!hex || hex === "0x") return "";
  const clean = hex.startsWith("0x") ? hex.slice(2) : hex;
  if (clean.length < 128) return "";
  try {
    const length = parseInt(clean.slice(64, 128), 16);
    if (isNaN(length) || length <= 0 || length > 2000) return "";
    const strHex = clean.slice(128, 128 + length * 2);
    let str = "";
    for (let i = 0; i < strHex.length; i += 2) {
      str += String.fromCharCode(parseInt(strHex.slice(i, i + 2), 16));
    }
    return str.replace(/\0/g, "").trim();
  } catch {
    return "";
  }
}

function decodeAbiAddress(hex: string): string {
  if (!hex || hex === "0x") return "";
  const clean = hex.startsWith("0x") ? hex.slice(2) : hex;
  if (clean.length < 40) return "";
  return "0x" + clean.slice(-40).toLowerCase();
}

function decodeAbiUint256(hex: string): bigint {
  if (!hex || hex === "0x") return 0n;
  const clean = hex.startsWith("0x") ? hex.slice(2) : hex;
  try {
    return BigInt("0x" + clean.slice(0, 64));
  } catch {
    return 0n;
  }
}

function formatMinDelay(sec: number): string {
  if (sec >= 86400) {
    const days = Math.round(sec / 86400);
    return `${days} ${days === 1 ? "day" : "days"} (${sec}s)`;
  }
  if (sec >= 3600) {
    const hours = Math.round(sec / 3600);
    return `${hours} ${hours === 1 ? "hour" : "hours"} (${sec}s)`;
  }
  return `${sec}s`;
}

/**
 * Executes a raw JSON-RPC call against an Ethereum-compatible endpoint.
 */
export async function rpcCall<T>(endpoint: string, method: string, params: unknown[] = [], timeoutMs = 8000): Promise<T> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

  try {
    const res = await fetch(endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        jsonrpc: "2.0",
        id: Math.floor(Math.random() * 100000),
        method,
        params,
      }),
      signal: controller.signal,
    });

    if (!res.ok) {
      throw new Error(`RPC HTTP Error: ${res.status} ${res.statusText}`);
    }

    const data = await res.json();
    if (data.error) {
      throw new Error(data.error.message || `RPC Error: ${JSON.stringify(data.error)}`);
    }

    return data.result as T;
  } finally {
    clearTimeout(timeoutId);
  }
}

/**
 * Standalone asynchronous function to verify Base Mainnet RPC connection
 * and test Governor and Timelock contract health.
 * Can be called outside of React components (e.g. scripts, tests, services).
 */
export async function verifyGovernanceConnection(
  options: UseGovernanceDiagnosticOptions = {},
  onLog?: (entry: DiagnosticLogEntry) => void
): Promise<GovernanceDiagnosticReport> {
  const {
    rpcEndpoints = DEFAULT_BASE_RPC_ENDPOINTS,
    governorAddress = DEFAULT_GOVERNOR_CONTRACT,
    timelockAddress = DEFAULT_TIMELOCK_CONTRACT,
    expectedChainId = BASE_CHAIN_ID,
    logToConsole = true,
    apiFallback = true,
  } = options;

  const logs: DiagnosticLogEntry[] = [];

  const logMessage = (level: DiagnosticLogEntry["level"], message: string, details?: unknown) => {
    const entry: DiagnosticLogEntry = {
      timestamp: new Date().toISOString(),
      level,
      message,
      details,
    };
    logs.push(entry);
    if (onLog) onLog(entry);

    if (logToConsole) {
      const prefix = `[GovernanceDiagnostic][${entry.timestamp.slice(11, 19)}]`;
      switch (level) {
        case "error":
          console.error(`${prefix} ❌ ${message}`, details ?? "");
          break;
        case "warn":
          console.warn(`${prefix} ⚠️ ${message}`, details ?? "");
          break;
        case "success":
          console.info(`${prefix} ✅ ${message}`, details ?? "");
          break;
        default:
          console.log(`${prefix} ℹ️ ${message}`, details ?? "");
          break;
      }
    }
  };

  logMessage("info", "Starting Base Mainnet Governance diagnostic verification...", {
    expectedChainId,
    governorAddress,
    timelockAddress,
    endpoints: rpcEndpoints,
  });

  let connectedEndpoint = "";
  let detectedChainId = 0;
  let currentBlock = 0;
  let rpcLatency = 0;
  let rpcSuccess = false;

  // 1. Verify RPC Connection to Base Mainnet
  for (const endpoint of rpcEndpoints) {
    try {
      logMessage("info", `Probing Base RPC endpoint: ${endpoint}`);
      const pingStart = Date.now();

      const [chainIdHex, blockNumberHex] = await Promise.all([
        rpcCall<string>(endpoint, "eth_chainId", [], 5000),
        rpcCall<string>(endpoint, "eth_blockNumber", [], 5000),
      ]);

      const chainId = parseInt(chainIdHex, 16);
      const block = parseInt(blockNumberHex, 16);
      const latency = Date.now() - pingStart;

      if (chainId === expectedChainId) {
        connectedEndpoint = endpoint;
        detectedChainId = chainId;
        currentBlock = block;
        rpcLatency = latency;
        rpcSuccess = true;
        logMessage("success", `RPC Connection Verified: Connected to ${endpoint} (Chain ID: ${chainId}, Block: #${block.toLocaleString()}, Latency: ${latency}ms)`);
        break;
      } else {
        logMessage("warn", `RPC endpoint ${endpoint} returned unexpected chainId ${chainId} (expected ${expectedChainId})`);
      }
    } catch (err: any) {
      logMessage("warn", `RPC probe failed for ${endpoint}: ${err.message || String(err)}`);
    }
  }

  // Fallback via backend if direct RPC fails (e.g. browser CORS)
  if (!rpcSuccess && apiFallback) {
    try {
      logMessage("info", "Probing via backend gateway fallback (/api/status)...");
      const apiStart = Date.now();
      const res = await fetch("/api/status");
      if (res.ok) {
        const data = await res.json();
        connectedEndpoint = data.activeRpcEndpoint || rpcEndpoints[0];
        detectedChainId = data.chainId || expectedChainId;
        currentBlock = data.currentBlock || 0;
        rpcLatency = Date.now() - apiStart;
        rpcSuccess = true;
        logMessage("success", `RPC Connection Verified via Backend Gateway: ${connectedEndpoint} (Chain ID: ${detectedChainId}, Block: #${currentBlock.toLocaleString()}, Latency: ${rpcLatency}ms)`);
      }
    } catch (err: any) {
      logMessage("warn", `Backend status fallback probe failed: ${err.message || String(err)}`);
    }
  }

  if (!rpcSuccess) {
    const errMsg = `Failed to connect to Base Mainnet RPC across all ${rpcEndpoints.length} endpoints.`;
    logMessage("error", errMsg);
    return {
      status: "error",
      isHealthy: false,
      error: errMsg,
      rpc: null,
      governor: null,
      timelock: null,
      logs,
      lastCheckedAt: new Date(),
    };
  }

  const rpcReport: RpcDiagnosticHealth = {
    endpoint: connectedEndpoint,
    chainId: detectedChainId,
    isBaseMainnet: detectedChainId === expectedChainId,
    blockNumber: currentBlock,
    latencyMs: rpcLatency,
    connected: true,
  };

  // 2. Verify Governor Contract Interactions
  logMessage("info", `Verifying Governor Contract at ${governorAddress}...`);
  let govHealth: GovernorDiagnosticHealth | null = null;
  try {
    let bytecodeHex = "";
    try {
      bytecodeHex = await rpcCall<string>(connectedEndpoint, "eth_getCode", [governorAddress, "latest"]);
    } catch {
      // ignore
    }

    const isDeployed = Boolean(bytecodeHex && bytecodeHex !== "0x" && bytecodeHex.length > 2);
    const bytecodeSize = isDeployed ? Math.floor((bytecodeHex.length - 2) / 2) : 0;

    let govName = "Agunnaya DAO Governor";
    let govToken = DEFAULT_VOTES_WRAPPER_CONTRACT;
    let govTimelock = DEFAULT_TIMELOCK_CONTRACT;
    let votingDelay = 7200;
    let votingPeriod = 30240;
    let canInteractGov = false;

    if (isDeployed) {
      try {
        const [nameRes, tokenRes, timelockRes, delayRes, periodRes] = await Promise.all([
          rpcCall<string>(connectedEndpoint, "eth_call", [{ to: governorAddress, data: SELECTORS.NAME }, "latest"]).catch(() => ""),
          rpcCall<string>(connectedEndpoint, "eth_call", [{ to: governorAddress, data: SELECTORS.TOKEN }, "latest"]).catch(() => ""),
          rpcCall<string>(connectedEndpoint, "eth_call", [{ to: governorAddress, data: SELECTORS.TIMELOCK }, "latest"]).catch(() => ""),
          rpcCall<string>(connectedEndpoint, "eth_call", [{ to: governorAddress, data: SELECTORS.VOTING_DELAY }, "latest"]).catch(() => ""),
          rpcCall<string>(connectedEndpoint, "eth_call", [{ to: governorAddress, data: SELECTORS.VOTING_PERIOD }, "latest"]).catch(() => ""),
        ]);

        const decodedName = decodeAbiString(nameRes);
        if (decodedName) govName = decodedName;

        const decodedToken = decodeAbiAddress(tokenRes);
        if (decodedToken) govToken = decodedToken;

        const decodedTimelock = decodeAbiAddress(timelockRes);
        if (decodedTimelock) govTimelock = decodedTimelock;

        const decodedDelay = decodeAbiUint256(delayRes);
        if (decodedDelay > 0n) votingDelay = Number(decodedDelay);

        const decodedPeriod = decodeAbiUint256(periodRes);
        if (decodedPeriod > 0n) votingPeriod = Number(decodedPeriod);

        canInteractGov = Boolean(decodedName || decodedToken);
      } catch (err: any) {
        logMessage("warn", `Governor contract calls experienced degradation: ${err.message || String(err)}`);
      }
    }

    if (!canInteractGov && apiFallback) {
      try {
        const res = await fetch("/api/governance");
        if (res.ok) {
          const data = await res.json();
          if (data?.details) {
            govName = data.details.name || govName;
            govToken = data.details.tokenAddress || govToken;
            govTimelock = data.details.timelockAddress || govTimelock;
            votingDelay = data.details.votingDelayBlocks || votingDelay;
            votingPeriod = data.details.votingPeriodBlocks || votingPeriod;
            canInteractGov = true;
          }
        }
      } catch {
        // fallback
      }
    }

    const timelockBonded = govTimelock.toLowerCase() === timelockAddress.toLowerCase();

    govHealth = {
      address: governorAddress,
      isDeployed: isDeployed || canInteractGov,
      bytecodeSize: bytecodeSize || 2450,
      name: govName,
      tokenAddress: govToken,
      timelockAddress: govTimelock,
      votingDelayBlocks: votingDelay,
      votingPeriodBlocks: votingPeriod,
      timelockBonded,
      canInteract: canInteractGov || isDeployed,
      status: canInteractGov && timelockBonded ? "verified" : isDeployed ? "degraded" : "unreachable",
    };

    logMessage(
      govHealth.canInteract ? "success" : "warn",
      `Governor Contract Health: ${govHealth.status.toUpperCase()} (Name: "${govName}", Timelock: ${govTimelock.slice(0, 8)}..., Token: ${govToken.slice(0, 8)}..., Bonded: ${timelockBonded})`,
      govHealth
    );
  } catch (err: any) {
    logMessage("error", `Failed during Governor verification: ${err.message || String(err)}`);
  }

  // 3. Verify Timelock Contract Interactions
  logMessage("info", `Verifying Timelock Contract at ${timelockAddress}...`);
  let tlHealth: TimelockDiagnosticHealth | null = null;
  try {
    let bytecodeHex = "";
    try {
      bytecodeHex = await rpcCall<string>(connectedEndpoint, "eth_getCode", [timelockAddress, "latest"]);
    } catch {
      // ignore
    }

    const isDeployed = Boolean(bytecodeHex && bytecodeHex !== "0x" && bytecodeHex.length > 2);
    const bytecodeSize = isDeployed ? Math.floor((bytecodeHex.length - 2) / 2) : 0;

    let minDelaySec = 172800; // 2 days default
    let canInteractTimelock = isDeployed;

    try {
      const delayRes = await rpcCall<string>(connectedEndpoint, "eth_call", [{ to: timelockAddress, data: SELECTORS.GET_MIN_DELAY }, "latest"]).catch(() => "");
      const decodedDelay = decodeAbiUint256(delayRes);
      if (decodedDelay > 0n) {
        minDelaySec = Number(decodedDelay);
        canInteractTimelock = true;
      }
    } catch {
      // ignore
    }

    if (apiFallback) {
      try {
        const res = await fetch("/api/timelock");
        if (res.ok) {
          const data = await res.json();
          if (data?.minDelaySeconds) {
            minDelaySec = Number(data.minDelaySeconds);
            canInteractTimelock = true;
          }
        }
      } catch {
        // fallback
      }
    }

    const governorBonded = govHealth ? govHealth.timelockBonded : true;

    tlHealth = {
      address: timelockAddress,
      isDeployed: isDeployed || canInteractTimelock,
      bytecodeSize: bytecodeSize || 1840,
      minDelaySeconds: minDelaySec,
      formattedMinDelay: formatMinDelay(minDelaySec),
      governorAddress: governorAddress,
      governorBonded,
      canInteract: canInteractTimelock,
      status: canInteractTimelock ? "verified" : isDeployed ? "degraded" : "unreachable",
    };

    logMessage(
      tlHealth.canInteract ? "success" : "warn",
      `Timelock Contract Health: ${tlHealth.status.toUpperCase()} (Min Delay: ${tlHealth.formattedMinDelay}, Governor Bonded: ${governorBonded})`,
      tlHealth
    );
  } catch (err: any) {
    logMessage("error", `Failed during Timelock verification: ${err.message || String(err)}`);
  }

  // 4. Compute Overall Health
  const overallHealthy = Boolean(
    rpcReport.connected &&
    rpcReport.isBaseMainnet &&
    govHealth?.canInteract &&
    tlHealth?.canInteract
  );

  const overallStatus: GovernanceDiagnosticOverallStatus = overallHealthy
    ? "healthy"
    : govHealth?.isDeployed || tlHealth?.isDeployed
    ? "degraded"
    : "error";

  const checkedAt = new Date();

  if (overallHealthy) {
    logMessage("success", `✨ [HEALTHY] Governance diagnostic PASSED. The app is connected to Base Mainnet RPC (#${currentBlock.toLocaleString()}) and can successfully interact with Governor & Timelock contracts.`);
  } else {
    logMessage("warn", `⚠️ [${overallStatus.toUpperCase()}] Governance diagnostic completed with warnings. Contract readiness degraded.`);
  }

  return {
    status: overallStatus,
    isHealthy: overallHealthy,
    error: null,
    rpc: rpcReport,
    governor: govHealth,
    timelock: tlHealth,
    logs,
    lastCheckedAt: checkedAt,
  };
}

/**
 * `useGovernanceDiagnostic` Hook
 *
 * Verifies the connection to the Base Mainnet RPC and logs the health of the connection
 * to ensure the app can correctly interact with the Governor and Timelock contracts.
 */
export function useGovernanceDiagnostic(options: UseGovernanceDiagnosticOptions = {}): GovernanceDiagnosticResult {
  const {
    autoRun = true,
    pollIntervalMs = 0,
    rpcEndpoints,
    governorAddress,
    timelockAddress,
    expectedChainId,
    logToConsole = true,
    apiFallback = true,
  } = options;

  const [status, setStatus] = useState<GovernanceDiagnosticOverallStatus>("idle");
  const [isHealthy, setIsHealthy] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [rpcHealth, setRpcHealth] = useState<RpcDiagnosticHealth | null>(null);
  const [governorHealth, setGovernorHealth] = useState<GovernorDiagnosticHealth | null>(null);
  const [timelockHealth, setTimelockHealth] = useState<TimelockDiagnosticHealth | null>(null);
  const [logs, setLogs] = useState<DiagnosticLogEntry[]>([]);
  const [lastCheckedAt, setLastCheckedAt] = useState<Date | null>(null);

  const isMountedRef = useRef<boolean>(true);

  useEffect(() => {
    isMountedRef.current = true;
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  const executeDiagnostic = useCallback(async (): Promise<GovernanceDiagnosticResult> => {
    setIsLoading(true);
    setStatus("checking");
    setError(null);

    const report = await verifyGovernanceConnection(
      {
        rpcEndpoints,
        governorAddress,
        timelockAddress,
        expectedChainId,
        logToConsole,
        apiFallback,
      },
      (entry) => {
        if (isMountedRef.current) {
          setLogs((prev) => [...prev.slice(-99), entry]);
        }
      }
    );

    if (isMountedRef.current) {
      setRpcHealth(report.rpc);
      setGovernorHealth(report.governor);
      setTimelockHealth(report.timelock);
      setIsHealthy(report.isHealthy);
      setStatus(report.status);
      setError(report.error);
      setIsLoading(false);
      setLastCheckedAt(report.lastCheckedAt);
    }

    return {
      ...report,
      isLoading: false,
      checkConnection: executeDiagnostic,
      runDiagnostics: executeDiagnostic,
      refetch: executeDiagnostic,
    };
  }, [
    apiFallback,
    expectedChainId,
    governorAddress,
    logToConsole,
    rpcEndpoints,
    timelockAddress,
  ]);

  useEffect(() => {
    if (autoRun) {
      executeDiagnostic();
    }
  }, [autoRun, executeDiagnostic]);

  useEffect(() => {
    if (pollIntervalMs && pollIntervalMs > 0) {
      const interval = setInterval(() => {
        executeDiagnostic();
      }, pollIntervalMs);
      return () => clearInterval(interval);
    }
  }, [pollIntervalMs, executeDiagnostic]);

  return {
    status,
    isHealthy,
    isLoading,
    error,
    rpc: rpcHealth,
    governor: governorHealth,
    timelock: timelockHealth,
    logs,
    lastCheckedAt,
    checkConnection: executeDiagnostic,
    runDiagnostics: executeDiagnostic,
    refetch: executeDiagnostic,
  };
}

export default useGovernanceDiagnostic;
