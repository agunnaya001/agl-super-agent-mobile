import { ethers } from "ethers";

// ABI fragments for the Base Mainnet ecosystem contracts.
// Only the read functions used by the web port are declared here.

export const ERC20_ABI = [
  "function name() view returns (string)",
  "function symbol() view returns (string)",
  "function decimals() view returns (uint8)",
  "function totalSupply() view returns (uint256)",
  "function balanceOf(address) view returns (uint256)",
  "function allowance(address owner, address spender) view returns (uint256)",
  "function owner() view returns (address)",
];

export const VOTES_WRAPPER_ABI = [
  "function name() view returns (string)",
  "function symbol() view returns (string)",
  "function decimals() view returns (uint8)",
  "function totalSupply() view returns (uint256)",
  "function balanceOf(address) view returns (uint256)",
  "function getVotes(address) view returns (uint256)",
  "function delegates(address) view returns (address)",
  "function numCheckpoints(address) view returns (uint32)",
  "function underlying() view returns (address)",
  "function token() view returns (address)",
];

export const CREDITS_ABI = [
  "function owner() view returns (address)",
  "function paused() view returns (bool)",
  "function aglToken() view returns (address)",
  "function BURN_ADDRESS() view returns (address)",
  "function creditsPerAGL() view returns (uint256)",
  "function totalAGLBurned() view returns (uint256)",
  "function totalAGLBurnedBy(address) view returns (uint256)",
  "function totalCreditsPurchased(address) view returns (uint256)",
  "function previewCredits(uint256) view returns (uint256)",
];

export const STAKING_ABI = [
  "function aglToken() view returns (address)",
  "function owner() view returns (address)",
  "function paused() view returns (bool)",
  "function totalStaked() view returns (uint256)",
  "function rewardPool() view returns (uint256)",
  "function tierCount() view returns (uint256)",
  "function tiers(uint256) view returns (uint256 duration, uint256 aprBps, bool active)",
  "function positions(uint256) view returns (address user, uint256 amount, uint256 startTime, uint256 unlockTime, uint256 tierId, uint256 pendingReward)",
  "function userPositionCount(address) view returns (uint256)",
];

export const GOVERNOR_ABI = [
  "function name() view returns (string)",
  "function token() view returns (address)",
  "function timelock() view returns (address)",
  "function votingDelay() view returns (uint256)",
  "function votingPeriod() view returns (uint256)",
  "function quorumNumerator() view returns (uint256)",
  "function proposalThreshold() view returns (uint256)",
];

export const TIMELOCK_ABI = [
  "function getMinDelay() view returns (uint256)",
];

// Chainlink AggregatorV3Interface
export const ORACLE_ABI = [
  "function latestRoundData() view returns (uint80 roundId, int256 answer, uint256 startedAt, uint256 updatedAt, uint80 answeredInRound)",
  "function decimals() view returns (uint8)",
];

export function makeContract(address: string, abi: string[], provider: ethers.JsonRpcProvider) {
  return new ethers.Contract(address, abi, provider);
}
