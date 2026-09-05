// Static / seed data ported from LearningLessonsData.kt & BlockchainService defaults.
import { DEFAULT_DEMO_WALLET, AGL_VOTES_WRAPPER_CONTRACT, AGL_TOKEN_CONTRACT, AGL_CREDITS_CONTRACT, GOVERNOR_CONTRACT, TIMELOCK_CONTRACT } from "../config.js";

export interface QuizOption { id: number; text: string; }
export interface QuizQuestion { id: string; question: string; options: QuizOption[]; correctOptionId: number; explanation: string; }
export interface LearningLesson {
  id: string; title: string; category: string; durationMinutes: number; xpReward: number;
  iconEmoji: string; shortSummary: string; contentMarkdown: string; keyTakeaways: string[]; quiz: QuizQuestion[];
}

export const LESSONS: LearningLesson[] = [
  {
    id: "lesson_blockchain_fundamentals", title: "Blockchain Fundamentals", category: "Core Basics", durationMinutes: 3, xpReward: 100, iconEmoji: "⛓️",
    shortSummary: "Learn how distributed ledgers, cryptographic hashing, and consensus mechanisms power trustless networks.",
    contentMarkdown: "### What is a Blockchain?\nA blockchain is an open, distributed ledger that records transactions across a network of computers. Once recorded, data inside a block cannot be altered without changing all subsequent blocks.\n\n### Key Pillars:\n1. **Decentralization**: No single central authority controls the ledger.\n2. **Immutability**: Cryptographic hash pointers link blocks in an unbreakable chronological chain.\n3. **Consensus Mechanisms**: Protocols like Proof of Stake (PoS) validate transactions and prevent double-spending.",
    keyTakeaways: ["Blockchains are decentralized, immutable ledgers.", "Consensus mechanisms validate transactions without central intermediaries.", "Cryptographic hashing ensures tamper-evident block linkages."],
    quiz: [{ id: "q_bf_1", question: "What makes data recorded on a blockchain immutable?", options: [{id:1,text:"A single administrator locks the database with a password"},{id:2,text:"Cryptographic hashes link each block to the previous one across a decentralized network"},{id:3,text:"Transactions are kept completely secret and private"},{id:4,text:"Data is erased every 24 hours"}], correctOptionId: 2, explanation: "Cryptographic hashing links every block to the previous block's hash." }],
  },
  {
    id: "lesson_ethereum", title: "Ethereum & EVM", category: "Layer 1", durationMinutes: 4, xpReward: 120, iconEmoji: "🔷",
    shortSummary: "Discover the world computer, Ethereum Virtual Machine (EVM), and programmable smart contracts.",
    contentMarkdown: "### The World Computer\nEthereum introduced a Turing-complete state machine: the Ethereum Virtual Machine (EVM).\n\n### How the EVM Works:\n- **State Transition**: Every block updates the global state.\n- **Gas**: Every operation costs 'gas' in ETH to prevent infinite loops.\n- **Smart Contracts**: Self-executing code stored at a deterministic address on-chain.",
    keyTakeaways: ["EVM is a decentralized computing environment.", "Gas compensates validators and prevents computational spam.", "Smart contracts execute autonomously when conditions are met."],
    quiz: [{ id: "q_eth_1", question: "Why does the EVM require users to pay 'gas' for transactions?", options: [{id:1,text:"To purchase physical hardware for miners"},{id:2,text:"To compensate validators and prevent computational denial-of-service spam"},{id:3,text:"To convert ETH into US Dollars automatically"},{id:4,text:"Gas is only needed when transactions fail"}], correctOptionId: 2, explanation: "Gas limits computation and compensates validators." }],
  },
  {
    id: "lesson_base_l2", title: "Base & Layer 2 Rollups", category: "Layer 2", durationMinutes: 3, xpReward: 150, iconEmoji: "🔵",
    shortSummary: "Understand how Base utilizes Optimistic Rollups to scale Ethereum with ultra-low fees and sub-second speeds.",
    contentMarkdown: "### What is Base?\nBase is a secure, low-cost Ethereum Layer 2 (L2) built on the OP Stack, incubated by Coinbase.\n\n### How Optimistic Rollups Work:\n- **Off-chain Execution**: Transactions are processed off Ethereum Mainnet at lightning speeds.\n- **Batching & Compression**: Thousands of transactions are bundled and posted to L1.\n- **Optimism**: Assumes state transitions are valid unless challenged via fraud proofs.",
    keyTakeaways: ["Base is an Optimistic Rollup L2 built on the OP Stack.", "Bundles thousands of transactions for ultra-low fees.", "Inherits Ethereum L1 security without sacrificing speed."],
    quiz: [{ id: "q_base_1", question: "How does Base achieve lower gas fees compared to Ethereum L1?", options: [{id:1,text:"By executing transactions off-chain and batching compressed proofs to L1"},{id:2,text:"By removing cryptographic security entirely"},{id:3,text:"By only allowing single-user transactions"},{id:4,text:"By running on centralized bank servers"}], correctOptionId: 1, explanation: "Base batches off-chain execution and posts compressed data to L1." }],
  },
  {
    id: "lesson_wallets", title: "Web3 Wallets & Self-Custody", category: "Security", durationMinutes: 4, xpReward: 120, iconEmoji: "👛",
    shortSummary: "Master seed phrases, private keys, and watch-only security for non-custodial Web3 wallets.",
    contentMarkdown: "### Self-Custody\nA Web3 wallet stores your private keys — the cryptographic proof of ownership of your on-chain assets.\n\n### Key Concepts:\n- **Seed Phrase**: A human-readable backup of your keys.\n- **Watch-Only**: View balances without exposing private keys (the AGL Super Agent model).\n- **Hardware Wallets**: Cold storage for maximum security.",
    keyTakeaways: ["Never share your seed phrase.", "Watch-only mode lets you monitor without risk.", "Hardware wallets offer the strongest protection."],
    quiz: [{ id: "q_w_1", question: "What does 'watch-only' wallet mode mean?", options: [{id:1,text:"It can view balances and activity without holding private keys"},{id:2,text:"It watches the market and auto-trades"},{id:3,text:"It requires a bank connection"},{id:4,text:"It deletes old transactions"}], correctOptionId: 1, explanation: "Watch-only mode reads on-chain data without exposing private keys." }],
  },
  {
    id: "lesson_tokens", title: "Tokens: ERC-20 & Standards", category: "Core Basics", durationMinutes: 3, xpReward: 100, iconEmoji: "🪙",
    shortSummary: "Explore ERC-20 fungible tokens, total supply, allowances, and the AGL ecosystem utility token.",
    contentMarkdown: "### ERC-20 Standard\nERC-20 defines a common interface for fungible tokens on EVM chains.\n\n### Core Functions:\n- `balanceOf`, `transfer`, `approve`, `allowance`, `totalSupply`\n- Decimals define the smallest divisible unit (typically 18).",
    keyTakeaways: ["ERC-20 is the standard for fungible tokens.", "Allowances enable delegated spending (DeFi).", "AGL is the utility token of the ecosystem."],
    quiz: [{ id: "q_t_1", question: "What ERC-20 function lets a contract spend tokens on your behalf?", options: [{id:1,text:"transfer"},{id:2,text:"approve / allowance"},{id:3,text:"totalSupply"},{id:4,text:"decimals"}], correctOptionId: 2, explanation: "approve() sets an allowance a spender may use." }],
  },
  {
    id: "lesson_nfts", title: "NFTs & Digital Ownership", category: "Core Basics", durationMinutes: 3, xpReward: 100, iconEmoji: "🖼️",
    shortSummary: "Understand ERC-721 non-fungible tokens and provable digital ownership on-chain.",
    contentMarkdown: "### NFTs\nNon-fungible tokens (ERC-721) represent unique digital assets.\n\n### Use Cases:\n- Digital art & collectibles\n- In-game assets (GameFi)\n- On-chain identity & agent registries (AGL Agent Registry).",
    keyTakeaways: ["NFTs are unique (non-fungible).", "ERC-721 is the standard.", "They enable provable digital ownership."],
    quiz: [{ id: "q_n_1", question: "Which standard defines non-fungible tokens?", options: [{id:1,text:"ERC-20"},{id:2,text:"ERC-721"},{id:3,text:"ERC-4626"},{id:4,text:"ERC-2612"}], correctOptionId: 2, explanation: "ERC-721 defines unique non-fungible tokens." }],
  },
  {
    id: "lesson_smart_contracts", title: "Smart Contract Architecture", category: "Core Basics", durationMinutes: 4, xpReward: 130, iconEmoji: "🧠",
    shortSummary: "Learn how smart contracts store logic on-chain, read/write functions, and proxy patterns.",
    contentMarkdown: "### Smart Contracts\nSelf-executing code deployed at a deterministic address.\n\n### Patterns:\n- **Read vs Write** functions\n- **Proxy / Upgradeable** contracts\n- **Owner & access control** (Ownable)",
    keyTakeaways: ["Read functions are free; writes cost gas.", "Proxies enable upgradeability.", "Access control patterns protect critical functions."],
    quiz: [{ id: "q_sc_1", question: "Do read-only contract calls cost gas?", options: [{id:1,text:"Yes, always"},{id:2,text:"No, reads are free (eth_call)"},{id:3,text:"Only on L2"},{id:4,text:"Only for tokens"}], correctOptionId: 2, explanation: "eth_call reads are free; only state-changing writes cost gas." }],
  },
  {
    id: "lesson_defi", title: "DeFi & Automated Market Makers", category: "DeFi", durationMinutes: 4, xpReward: 140, iconEmoji: "📈",
    shortSummary: "Decentralized exchanges, liquidity pools, and AMM pricing on Base.",
    contentMarkdown: "### DeFi\nDecentralized finance replaces intermediaries with smart contracts.\n\n### AMMs:\n- Liquidity providers deposit pairs into pools.\n- Pricing follows x*y=k constant product.\n- Aerodrome is a leading Base DEX.",
    keyTakeaways: ["AMMs price assets via liquidity pools.", "LPs earn fees.", "Slippage increases with trade size vs pool depth."],
    quiz: [{ id: "q_d_1", question: "What formula does a constant-product AMM use?", options: [{id:1,text:"x + y = k"},{id:2,text:"x * y = k"},{id:3,text:"x / y = k"},{id:4,text:"x - y = k"}], correctOptionId: 2, explanation: "Constant product market makers use x*y=k." }],
  },
  {
    id: "lesson_daos", title: "DAOs & Decentralized Governance", category: "Governance", durationMinutes: 4, xpReward: 140, iconEmoji: "🏛️",
    shortSummary: "On-chain voting, Governor contracts, quorum, and timelocks.",
    contentMarkdown: "### DAOs\nDecentralized Autonomous Organizations coordinate via token voting.\n\n### Lifecycle:\n- Propose → Vote → Queue (Timelock) → Execute\n- Quorum is the minimum votes required.\n- wAGL (ERC-20Votes) tracks voting power.",
    keyTakeaways: ["Governor contracts manage proposals.", "Timelocks delay execution for security.", "Voting power comes from delegated tokens."],
    quiz: [{ id: "q_dao_1", question: "What is quorum in governance?", options: [{id:1,text:"The minimum votes required for a proposal to pass"},{id:2,text:"The proposal fee"},{id:3,text:"The block delay"},{id:4,text:"The token supply"}], correctOptionId: 1, explanation: "Quorum is the minimum participation for a valid vote." }],
  },
  {
    id: "lesson_gamefi", title: "GameFi & On-Chain Rewards", category: "Ecosystem", durationMinutes: 3, xpReward: 120, iconEmoji: "🎮",
    shortSummary: "Quests, XP, streaks, and gamified Web3 learning economies.",
    contentMarkdown: "### GameFi\nGame mechanics meet decentralized incentives.\n\n### In AGL Super Agent:\n- Daily check-ins build streaks.\n- Quests award XP + AGL bounties.\n- Leaderboards rank Super Agents.",
    keyTakeaways: ["Streaks reward consistency.", "Quests drive engagement.", "Leaderboards add social competition."],
    quiz: [{ id: "q_g_1", question: "What does a daily check-in streak reward?", options: [{id:1,text:"Nothing"},{id:2,text:"XP and multiplier bonuses"},{id:3,text:"Gas refunds"},{id:4,text:"Admin access"}], correctOptionId: 2, explanation: "Streaks grant XP and bonus multipliers." }],
  },
  {
    id: "lesson_web3_security", title: "Web3 Security & Threat Vectors", category: "Security", durationMinutes: 5, xpReward: 160, iconEmoji: "🛡️",
    shortSummary: "Reentrancy, front-running, malicious approvals, and how to audit contracts.",
    contentMarkdown: "### Threats\n- **Reentrancy**: Recursive calls before state update.\n- **Front-running**: MEV bots exploit pending txs.\n- **Malicious approvals**: Unlimited token allowances.\n\n### Defense\nAudit contracts, use timelocks, limit allowances.",
    keyTakeaways: ["Reentrancy is a top smart contract risk.", "Limit token allowances.", "Timelocks protect governance."],
    quiz: [{ id: "q_sec_1", question: "What is a reentrancy attack?", options: [{id:1,text:"A contract re-enters itself before state updates complete"},{id:2,text:"A fast block producer"},{id:3,text:"A wallet backup"},{id:4,text:"A price oracle"}], correctOptionId: 1, explanation: "Reentrancy exploits external calls that re-enter before state settles." }],
  },
  {
    id: "lesson_onchain_txs", title: "On-Chain Transactions & Gas", category: "Core Basics", durationMinutes: 3, xpReward: 110, iconEmoji: "⛽",
    shortSummary: "Anatomy of a transaction, gas, nonce, and Base's low-fee model.",
    contentMarkdown: "### Transactions\nA tx contains: nonce, to, value, data, gas limit & price.\n\n### On Base\nGas is priced in gwei; Base fees are fractions of a cent. EIP-4844 blobs further reduce L1 data costs.",
    keyTakeaways: ["Nonce orders transactions per account.", "Gas = limit × price.", "Base offers sub-cent fees."],
    quiz: [{ id: "q_tx_1", question: "What prevents transaction replay on an account?", options: [{id:1,text:"The nonce"},{id:2,text:"The gas price"},{id:3,text:"The chain id only"},{id:4,text:"The block hash"}], correctOptionId: 1, explanation: "The nonce increments per account to order and prevent replay." }],
  },
];

export interface QuestItem {
  id: string; title: string; description: string; xpReward: number; aglReward: number;
  category: "DAILY" | "WEEKLY" | "ECOSYSTEM" | "SECURITY_CHALLENGE"; currentProgress: number; maxProgress: number; isClaimed: boolean; iconEmoji: string;
}

export const QUESTS: QuestItem[] = [
  { id: "quest_daily_checkin", title: "Super Agent Daily Check-In", description: "Open the AGL Command Center and sync your Base on-chain telemetry.", xpReward: 50, aglReward: 2.5, category: "DAILY", currentProgress: 1, maxProgress: 1, isClaimed: false, iconEmoji: "⚡" },
  { id: "quest_ai_explain", title: "Ask AI to Explain a Transaction", description: "Use the AI Web3 Assistant to decode complex on-chain transaction data into simple English.", xpReward: 100, aglReward: 5.0, category: "DAILY", currentProgress: 0, maxProgress: 1, isClaimed: false, iconEmoji: "🤖" },
  { id: "quest_scan_contract", title: "Scan a Base Smart Contract", description: "Run a security and architecture analysis on any Base contract address.", xpReward: 150, aglReward: 10.0, category: "WEEKLY", currentProgress: 1, maxProgress: 1, isClaimed: false, iconEmoji: "🧠" },
  { id: "quest_learn_lessons", title: "Complete 3 Learning Quizzes", description: "Advance your Web3 knowledge in the Learning Center by acing 3 educational quizzes.", xpReward: 300, aglReward: 20.0, category: "WEEKLY", currentProgress: 1, maxProgress: 3, isClaimed: false, iconEmoji: "📚" },
  { id: "quest_stake_agl", title: "Stake AGL in AI Compute Vault", description: "Commit AGL tokens into the decentralized staking vault to secure agent nodes.", xpReward: 500, aglReward: 35.0, category: "ECOSYSTEM", currentProgress: 1, maxProgress: 1, isClaimed: true, iconEmoji: "🔒" },
  { id: "quest_security_audit", title: "Perform Full Security Risk Audit", description: "Audit an address, transaction hash, or token contract using the Web3 Security Assistant.", xpReward: 250, aglReward: 15.0, category: "SECURITY_CHALLENGE", currentProgress: 0, maxProgress: 1, isClaimed: false, iconEmoji: "🛡️" },
];

export interface Badge { id: string; title: string; description: string; iconEmoji: string; isUnlocked: boolean; unlockedAt: number | null; }

export const BADGES: Badge[] = [
  { id: "badge_genesis", title: "Genesis Agent", description: "Joined the AGL Super Agent network on Base.", iconEmoji: "🚀", isUnlocked: true, unlockedAt: Date.now() - 86400000 * 7 },
  { id: "badge_security_guardian", title: "Security Sentinel", description: "Completed smart contract scans and audited risky permissions.", iconEmoji: "🛡️", isUnlocked: true, unlockedAt: Date.now() - 86400000 * 3 },
  { id: "badge_scholar", title: "Web3 Scholar", description: "Completed 5 lessons in the Web3 Learning Center.", iconEmoji: "🎓", isUnlocked: false, unlockedAt: null },
  { id: "badge_staking_master", title: "Vault Archon", description: "Staked over 250 AGL tokens into the AI Compute Vault.", iconEmoji: "💎", isUnlocked: true, unlockedAt: Date.now() - 86400000 * 2 },
  { id: "badge_ai_whisperer", title: "AI Strategist", description: "Queried the AI Web3 Assistant for deep blockchain analytics 10+ times.", iconEmoji: "🧠", isUnlocked: false, unlockedAt: null },
  { id: "badge_base_explorer", title: "Base Pioneer", description: "Explored 5+ verified Base L2 contracts and transaction hashes.", iconEmoji: "🌐", isUnlocked: true, unlockedAt: Date.now() - 86400000 * 4 },
];

export type LeaderboardTimeframe = "DAILY" | "WEEKLY" | "MONTHLY" | "ALL_TIME";
export type Tier = "BRONZE" | "SILVER" | "GOLD" | "DIAMOND";
export interface LeaderboardUser { rank: number; address: string; username: string; xp: number; level: number; tier: Tier; questsCompleted: number; isCurrentUser: boolean; avatarEmoji: string; }

const LB_BASE: Omit<LeaderboardUser, "xp" | "questsCompleted">[] = [
  { rank: 1, address: "0x981A...73f1", username: "BaseArchon.eth", level: 38, tier: "DIAMOND", isCurrentUser: false, avatarEmoji: "👑" },
  { rank: 2, address: "0x44B2...290C", username: "SatoshiBase", level: 34, tier: "DIAMOND", isCurrentUser: false, avatarEmoji: "⚡" },
  { rank: 3, address: "0x89C1...F091", username: "CyberAuditor", level: 29, tier: "GOLD", isCurrentUser: false, avatarEmoji: "🛡️" },
  { rank: 4, address: "0x742d...f44e", username: "You (Super Agent)", level: 24, tier: "GOLD", isCurrentUser: true, avatarEmoji: "🤖" },
  { rank: 5, address: "0x22D3...90AA", username: "DegenScholar", level: 21, tier: "SILVER", isCurrentUser: false, avatarEmoji: "🎩" },
  { rank: 6, address: "0x55E9...11CB", username: "AeroPilot", level: 18, tier: "SILVER", isCurrentUser: false, avatarEmoji: "✈️" },
];

const LB_XP = [18500, 16200, 14100, 12450, 10800, 9200];

export function getLeaderboard(timeframe: LeaderboardTimeframe): LeaderboardUser[] {
  const multiplier = timeframe === "DAILY" ? 1.0 : timeframe === "WEEKLY" ? 4.5 : timeframe === "MONTHLY" ? 18.0 : 42.0;
  return LB_BASE.map((u, i) => ({
    ...u,
    xp: Math.round(LB_XP[i] * multiplier),
    questsCompleted: Math.round((LB_XP[i] * multiplier) / 4 / 200) + 5,
  }));
}

export interface UserProfile {
  walletAddress: string; totalXp: number; level: number; tier: Tier;
  questsCompletedCount: number; lessonsCompletedCount: number; contractsAnalyzedCount: number;
  totalRewardsClaimedAgl: number; consecutiveStreakDays: number; badges: Badge[];
}

export function getUserProfile(walletAddress: string = DEFAULT_DEMO_WALLET): UserProfile {
  const totalXp = 12450;
  const tier: Tier = totalXp >= 20000 ? "DIAMOND" : totalXp >= 10000 ? "GOLD" : totalXp >= 5000 ? "SILVER" : "BRONZE";
  return {
    walletAddress,
    totalXp,
    level: 24,
    tier,
    questsCompletedCount: 14,
    lessonsCompletedCount: 3,
    contractsAnalyzedCount: 8,
    totalRewardsClaimedAgl: 185.0,
    consecutiveStreakDays: 7,
    badges: BADGES,
  };
}

export interface EcosystemStats {
  tokenSymbol: string; currentPriceUsd: number; marketCapUsd: number; circulatingSupply: string;
  totalStakedAgl: string; stakingAprPercent: number; totalRewardsDistributedUsd: number;
  totalActiveAgents: number; totalContractAuditsCompleted: number;
}

export function getEcosystemStats(priceUsd = 3.42): EcosystemStats {
  return {
    tokenSymbol: "AGL",
    currentPriceUsd: priceUsd,
    marketCapUsd: 3420000000.0,
    circulatingSupply: "1,000,000,000 AGL",
    totalStakedAgl: "64,200,000 wAGL",
    stakingAprPercent: 18.5,
    totalRewardsDistributedUsd: 2840000.0,
    totalActiveAgents: 52400,
    totalContractAuditsCompleted: 210800,
  };
}

export interface BaseTransaction {
  hash: string; fromAddress: string; toAddress: string; value: string; tokenSymbol: string;
  type: string; status: string; blockNumber: number; gasUsedGwei: number; gasFeeUsd: number;
  timestamp: number; methodCalled: string | null; contractAddress: string | null; simpleExplanation: string | null;
}

function heuristicSummary(tx: Omit<BaseTransaction, "simpleExplanation">): string {
  const map: Record<string, string> = {
    STAKE_AGL: `Wrapped ${tx.value} AGL into wAGL to activate governance voting power on Base.`,
    TRANSFER_IN: `Received ${tx.value} ${tx.tokenSymbol} into this watch-only wallet.`,
    TRANSFER_OUT: `Sent ${tx.value} ${tx.tokenSymbol} from this wallet on Base Mainnet.`,
    SWAP: `Swapped tokens via Aerodrome router on Base for ${tx.value} ${tx.tokenSymbol}.`,
    CONTRACT_CALL: `Interacted with a Base smart contract (${tx.methodCalled ?? "call"}).`,
    MINT: `Minted ${tx.value} ${tx.tokenSymbol}.`,
    CLAIM_REWARD: `Claimed ${tx.value} ${tx.tokenSymbol} staking/governance reward.`,
  };
  return map[tx.type] ?? `On-chain ${tx.type} activity of ${tx.value} ${tx.tokenSymbol}.`;
}

export function getMockTransactions(walletAddress: string): BaseTransaction[] {
  const now = Date.now();
  const min = 60000, hr = 3600000, day = 86400000;
  const raw: Omit<BaseTransaction, "simpleExplanation">[] = [
    { hash: "0x9f1a2384a8c9b19e872d41b0231d683a429074b1e592750e3940172bf4821a01", fromAddress: walletAddress, toAddress: AGL_VOTES_WRAPPER_CONTRACT, value: "250.00", tokenSymbol: "AGL", type: "STAKE_AGL", status: "SUCCESS", blockNumber: 50742110, gasUsedGwei: 0.0012, gasFeeUsd: 0.004, timestamp: now - 2 * hr, methodCalled: "depositFor", contractAddress: AGL_VOTES_WRAPPER_CONTRACT },
    { hash: "0x3b7e2c1d9a4f5e6d7c8b9a0f1e2d3c4b5a6f7e8d9c0b1a2f3e4d5c6b7a8f9e0d", fromAddress: "0x1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b", toAddress: walletAddress, value: "1200.00", tokenSymbol: "AGL", type: "TRANSFER_IN", status: "SUCCESS", blockNumber: 50741880, gasUsedGwei: 0.001, gasFeeUsd: 0.003, timestamp: now - 5 * hr, methodCalled: null, contractAddress: AGL_TOKEN_CONTRACT },
    { hash: "0x5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b0a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c", fromAddress: walletAddress, toAddress: "0xcF77a3Ba9A5CA399B7c97c748561549285932570", value: "500.00", tokenSymbol: "AGL", type: "SWAP", status: "SUCCESS", blockNumber: 50741550, gasUsedGwei: 0.0018, gasFeeUsd: 0.006, timestamp: now - 9 * hr, methodCalled: "swapExactTokensForTokens", contractAddress: "0xcF77a3Ba9A5CA399B7c97c748561549285932570" },
    { hash: "0x7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f", fromAddress: AGL_CREDITS_CONTRACT, toAddress: walletAddress, value: "15.00", tokenSymbol: "AGL", type: "CLAIM_REWARD", status: "SUCCESS", blockNumber: 50741280, gasUsedGwei: 0.0011, gasFeeUsd: 0.004, timestamp: now - 1 * day, methodCalled: "claimReward", contractAddress: AGL_CREDITS_CONTRACT },
    { hash: "0x9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a", fromAddress: walletAddress, toAddress: GOVERNOR_CONTRACT, value: "0.00", tokenSymbol: "AGL", type: "CONTRACT_CALL", status: "SUCCESS", blockNumber: 50740921, gasUsedGwei: 0.0015, gasFeeUsd: 0.005, timestamp: now - 2 * day, methodCalled: "castVote(1, 1)", contractAddress: GOVERNOR_CONTRACT },
    { hash: "0xb1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b", fromAddress: walletAddress, toAddress: TIMELOCK_CONTRACT, value: "0.00", tokenSymbol: "ETH", type: "CONTRACT_CALL", status: "PENDING", blockNumber: 50740500, gasUsedGwei: 0.0014, gasFeeUsd: 0.005, timestamp: now - 3 * day, methodCalled: "execute", contractAddress: TIMELOCK_CONTRACT },
  ];
  return raw.map((tx) => ({ ...tx, simpleExplanation: heuristicSummary(tx) }));
}

export interface AiSuggestion {
  id: string; category: string; title: string; description: string; prompt: string;
  badge: string; impactTag: string | null; icon: string; isActionable: boolean;
  actionLabel: string; targetAiTab: string | null; contractAddress: string | null;
}

export const AI_SUGGESTIONS: AiSuggestion[] = [
  { id: "sug_audit_agl", category: "SECURITY", title: "Audit AGL Token Contract", description: "Run a full security audit on the AGL token contract for owner backdoors and fee taxes.", prompt: "Audit the AGL token contract for security risks", badge: "Recommended", impactTag: "High Impact", icon: "🛡️", isActionable: true, actionLabel: "Audit Now", targetAiTab: "SECURITY_AUDIT", contractAddress: AGL_TOKEN_CONTRACT },
  { id: "sug_explain_stake", category: "PORTFOLIO", title: "Explain Your Staking Transaction", description: "Decode your recent wAGL deposit transaction into plain English.", prompt: "Explain my recent staking transaction in simple terms", badge: "New", impactTag: null, icon: "💎", isActionable: true, actionLabel: "Ask Agent", targetAiTab: "CHAT", contractAddress: null },
  { id: "sug_yield", category: "OPTIMIZATION", title: "Optimize Yield Strategy", description: "Compare AGL staking APR vs Aerodrome LP yields on Base.", prompt: "Compare AGL staking APR vs Aerodrome LP yields and recommend a strategy", badge: "Recommended", impactTag: "Yield", icon: "⚡", isActionable: true, actionLabel: "Ask Agent", targetAiTab: "CHAT", contractAddress: null },
  { id: "sug_vote", category: "GOVERNANCE", title: "Review Active Proposal", description: "Summarize the active governance proposal and your voting power.", prompt: "Summarize the active governance proposal and my voting power", badge: "Active", impactTag: null, icon: "🏛️", isActionable: true, actionLabel: "Ask Agent", targetAiTab: "CHAT", contractAddress: null },
  { id: "sug_analyze_credits", category: "CONTRACT", title: "Analyze Credits Contract", description: "Inspect the AGL Compute Credits contract architecture and functions.", prompt: "Analyze the AGL Compute Credits contract", badge: "Recommended", impactTag: null, icon: "📜", isActionable: true, actionLabel: "Analyze", targetAiTab: "CONTRACT_ANALYZER", contractAddress: AGL_CREDITS_CONTRACT },
];

export const FOLLOW_UP_SUGGESTIONS = [
  "Explain my latest transaction",
  "Audit the AGL token contract",
  "What is my current voting power?",
  "Compare staking vs LP yields",
  "Summarize the active governance proposal",
];
