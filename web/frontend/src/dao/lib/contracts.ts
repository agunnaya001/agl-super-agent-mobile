/**
 * DAO Governance Contract Constants and Network Settings for Base Mainnet (Chain ID 8453)
 */

export const BASE_CHAIN_ID = 8453;
export const BASE_NETWORK_NAME = "Base Mainnet";

export const GOVERNANCE_CONTRACTS = {
  governor: "0x345Ec3FbfbC85B22a4c1eaAE960dBE7F3B083044",
  timelock: "0x125B6C40f5B50A7Da6d64C3d497D21feFaF1D773",
  token: "0xa27c9ba04d026a7e025aebfa878faeaeb158097d",
  staking: "0x40e2cf70c679a957b42551a37c09c314f85e3a89",
  credits: "0x42fcf0f6fa1e13e2f9f8c6ebfef405b5dfba7f89",
} as const;

export const BASE_RPC_URLS = [
  "https://mainnet.base.org",
  "https://base-rpc.publicnode.com",
  "https://1rpc.io/base",
  "https://base.llamarpc.com",
  "https://base.drpc.org",
] as const;

/**
 * Returns formatted Basescan explorer URL for a given address or transaction hash
 */
export function getBasescanUrl(type: "address" | "tx", hashOrAddress: string): string {
  return `https://basescan.org/${type}/${hashOrAddress}`;
}

/**
 * Shortens an address for clean display (e.g. 0x345E...3044)
 */
export function truncateAddress(address: string, startChars = 6, endChars = 4): string {
  if (!address || address.length < startChars + endChars) return address || "";
  return `${address.slice(0, startChars)}...${address.slice(-endChars)}`;
}
