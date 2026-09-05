// Base Mainnet contract registry & RPC configuration.
// Mirrors com.example.data.remote.blockchain.config.BaseBlockchainConfig

export const CHAIN_ID = 8453;
export const NETWORK_NAME = "Base Mainnet";
export const EXPLORER_BASE_URL = "https://basescan.org";

export const RPC_ENDPOINTS = [
  "https://mainnet.base.org",
  "https://base-rpc.publicnode.com",
  "https://1rpc.io/base",
  "https://base.llamarpc.com",
  "https://base.drpc.org",
];

export const AGL_TOKEN_CONTRACT = "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698";
export const AGL_CREDITS_CONTRACT = "0x13866F31c60822Ff70684213b9727915Ddf2c183";
export const AGL_VOTES_WRAPPER_CONTRACT = "0xA27C9BA04D06EcAF766EF4e074b403DAf19A3d69";
export const STAKING_CONTRACT = "0xd4B61B4876c15e78e0275EbA52cf62D55ED5fD30";
export const GOVERNOR_CONTRACT = "0x3fFCb92A17caeaAd1342DD76978b566C8aEC7010";
export const TIMELOCK_CONTRACT = "0x900D315C91D9e54F3fa3412D475009d905bf6744";
export const DEFAULT_DEMO_WALLET = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e";

export const CHAINLINK_ETH_USD_FEED = "0x71041dddad35715971640cFBB525E6307146B0FB";
export const AGL_CHAINLINK_ORACLE_FEED = "0x19273c5bF7A74E9A4B6a2D927E259Fe4d21658bE";

export interface EcosystemContract {
  id: string;
  name: string;
  purpose: string;
  contractAddress: string;
  network: string;
  status: string;
  verified: boolean;
  type: string;
  iconEmoji: string;
}

export const ECOSYSTEM_CONTRACTS: EcosystemContract[] = [
  { id: "agl_token", name: "Agunnaya Labs (AGL)", purpose: "Core utility, staking, and ecosystem currency on Base Mainnet", contractAddress: AGL_TOKEN_CONTRACT, network: NETWORK_NAME, status: "Active", verified: true, type: "ERC-20 Token", iconEmoji: "🪙" },
  { id: "agl_credits", name: "AGL Compute Credits", purpose: "On-chain compute and AI execution credit billing contract", contractAddress: AGL_CREDITS_CONTRACT, network: NETWORK_NAME, status: "Active", verified: true, type: "Compute Protocol", iconEmoji: "⚡" },
  { id: "agl_votes_wrapper", name: "Wrapped AGL (wAGL)", purpose: "ERC-20 Votes wrapper for DAO governance snapshot checkpointing", contractAddress: AGL_VOTES_WRAPPER_CONTRACT, network: NETWORK_NAME, status: "Active", verified: true, type: "ERC20Votes Wrapper", iconEmoji: "🗳️" },
  { id: "agl_staking", name: "AGL Staking", purpose: "Time-locked yield staking, reward pool distribution, and tier management", contractAddress: STAKING_CONTRACT, network: NETWORK_NAME, status: "Active", verified: true, type: "Staking Pool", iconEmoji: "💎" },
  { id: "agl_governor", name: "Agunnaya DAO Governor", purpose: "On-chain governance voting, proposal lifecycle, and quorum tracking", contractAddress: GOVERNOR_CONTRACT, network: NETWORK_NAME, status: "Active", verified: true, type: "DAO Governor", iconEmoji: "🏛️" },
  { id: "agl_timelock", name: "Timelock Controller", purpose: "Security timelock executing community-approved governance actions", contractAddress: TIMELOCK_CONTRACT, network: NETWORK_NAME, status: "Active", verified: true, type: "Timelock Controller", iconEmoji: "⏳" },
];

export function explorerAddressUrl(address: string): string {
  return `${EXPLORER_BASE_URL}/address/${address}`;
}
export function explorerTxUrl(hash: string): string {
  return `${EXPLORER_BASE_URL}/tx/${hash}`;
}
