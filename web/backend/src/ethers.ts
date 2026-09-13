import { ethers, JsonRpcProvider, formatUnits } from "ethers";
import { RPC_ENDPOINTS, CHAIN_ID } from "./config.js";

// Provider with automatic multi-RPC failover, mirroring the Android BaseRpcService.
let currentProvider: JsonRpcProvider | null = null;
let activeRpcIndex = 0;

function makeProvider(url: string): JsonRpcProvider {
  return new JsonRpcProvider(url, CHAIN_ID, { staticNetwork: true });
}

export function getProvider(): JsonRpcProvider {
  if (currentProvider) return currentProvider;
  currentProvider = makeProvider(RPC_ENDPOINTS[0]);
  return currentProvider;
}

async function tryCall<T>(fn: () => Promise<T>): Promise<T> {
  try {
    return await fn();
  } catch (err) {
    throw err;
  }
}

// Run an RPC-dependent read, failing over across endpoints on error.
export async function withFailover<T>(fn: (provider: JsonRpcProvider) => Promise<T>): Promise<T> {
  let lastErr: unknown;
  for (let i = 0; i < RPC_ENDPOINTS.length; i++) {
    const idx = (activeRpcIndex + i) % RPC_ENDPOINTS.length;
    const provider = makeProvider(RPC_ENDPOINTS[idx]);
    try {
      const result = await fn(provider);
      activeRpcIndex = idx;
      currentProvider = provider;
      return result;
    } catch (err) {
      lastErr = err;
    }
  }
  throw lastErr ?? new Error("All RPC endpoints failed");
}

export async function getNetworkStatus() {
  return withFailover(async (provider) => {
    const start = Date.now();
    const [chainIdBig, blockNumber] = await Promise.all([
      provider.send("eth_chainId", []),
      provider.send("eth_blockNumber", []),
    ]);
    const latency = Date.now() - start;
    return {
      chainId: Number(BigInt(chainIdBig)),
      isBaseMainnet: Number(BigInt(chainIdBig)) === CHAIN_ID,
      currentBlock: Number(BigInt(blockNumber)),
      rpcLatencyMs: latency,
      activeRpcEndpoint: RPC_ENDPOINTS[activeRpcIndex],
    };
  });
}

export function safeFormatUnits(value: bigint, decimals: number, maxFraction = 4): string {
  try {
    const formatted = formatUnits(value, decimals);
    const [whole, frac] = formatted.split(".");
    if (!frac) return whole;
    return `${whole}.${frac.slice(0, maxFraction)}`;
  } catch {
    return "0";
  }
}

export { ethers };
