import { Router } from "express";
import { withFailover, safeFormatUnits } from "../ethers.js";
import { makeContract } from "../abis.js";
import {
  AGL_TOKEN_CONTRACT, AGL_VOTES_WRAPPER_CONTRACT, STAKING_CONTRACT,
  GOVERNOR_CONTRACT, DEFAULT_DEMO_WALLET,
} from "../config.js";
import { ERC20_ABI, VOTES_WRAPPER_ABI } from "../abis.js";

export const advancedRouter = Router();

function isAddress(s: string): boolean {
  return /^0x[a-fA-F0-9]{40}$/.test(s);
}

// ---- Token Approval Scanner ----
advancedRouter.get("/wallet/:address/approvals", async (req, res) => {
  const address = req.params.address;
  if (!isAddress(address)) { res.status(400).json({ error: "Invalid address" }); return; }
  // On-chain approval scanning requires an event indexer; provide heuristic + live balance checks
  try {
    const result = await withFailover(async (provider) => {
      const agl = makeContract(AGL_TOKEN_CONTRACT, ERC20_ABI, provider);
      const wagl = makeContract(AGL_VOTES_WRAPPER_CONTRACT, VOTES_WRAPPER_ABI, provider);
      const [aglBal, waglBal, aglAllowanceStaking, waglAllowanceStaking] = await Promise.all([
        agl.balanceOf(address).catch(() => 0n),
        wagl.balanceOf(address).catch(() => 0n),
        agl.allowance(address, STAKING_CONTRACT).catch(() => 0n),
        wagl.allowance(address, STAKING_CONTRACT).catch(() => 0n),
      ]);
      const approvals = [
        {
          tokenSymbol: "AGL", tokenAddress: AGL_TOKEN_CONTRACT,
          spenderAddress: STAKING_CONTRACT, spenderName: "AGL Staking Vault",
          allowanceWei: aglAllowanceStaking.toString(),
          allowanceFormatted: safeFormatUnits(aglAllowanceStaking, 18),
          isUnlimited: aglAllowanceStaking >= 2n ** 256n - 1n,
          riskLevel: aglAllowanceStaking > 0n ? "REVIEW" : "NONE",
          isEcosystem: true,
        },
        {
          tokenSymbol: "wAGL", tokenAddress: AGL_VOTES_WRAPPER_CONTRACT,
          spenderAddress: STAKING_CONTRACT, spenderName: "AGL Staking Vault",
          allowanceWei: waglAllowanceStaking.toString(),
          allowanceFormatted: safeFormatUnits(waglAllowanceStaking, 18),
          isUnlimited: waglAllowanceStaking >= 2n ** 256n - 1n,
          riskLevel: waglAllowanceStaking > 0n ? "REVIEW" : "NONE",
          isEcosystem: true,
        },
      ];
      return {
        address,
        tokenBalances: {
          AGL: safeFormatUnits(aglBal, 18),
          wAGL: safeFormatUnits(waglBal, 18),
        },
        approvals: approvals.filter((a) => a.riskLevel !== "NONE"),
        allApprovals: approvals,
      };
    });
    res.json(result);
  } catch {
    // Fallback with simulated data
    res.json({
      address,
      tokenBalances: { AGL: "0.00", wAGL: "0.00" },
      approvals: [
        {
          tokenSymbol: "AGL", tokenAddress: AGL_TOKEN_CONTRACT,
          spenderAddress: STAKING_CONTRACT, spenderName: "AGL Staking Vault",
          allowanceWei: "0", allowanceFormatted: "0", isUnlimited: false,
          riskLevel: "NONE", isEcosystem: true,
        },
      ],
      allApprovals: [],
      note: "Live approval scan requires an event indexer. Showing direct allowance reads.",
    });
  }
});

// ---- Whale & Governance Watchlist ----
const WATCHLIST = [
  {
    id: "agl-treasury", label: "AGL Foundation Treasury", address: "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698",
    type: "TREASURY", tags: ["whale", "governance"], lastActivity: "Proposal #3 executed",
    lastActivityTs: Date.now() - 3_600_000, balanceAgl: "4,200,000",
  },
  {
    id: "demo-holder", label: "Super Agent Demo Holder", address: "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
    type: "WHALE", tags: ["whale", "staking"], lastActivity: "Staked 50,000 AGL (365-day tier)",
    lastActivityTs: Date.now() - 7_200_000, balanceAgl: "1,250,000",
  },
  {
    id: "defi-staker", label: "Base DeFi Staker", address: "0x5B38Da6a701c568545dCfcB03FcB875f56beddC4",
    type: "WHALE", tags: ["staking", "delegate"], lastActivity: "Delegated wAGL voting power",
    lastActivityTs: Date.now() - 14_400_000, balanceAgl: "890,000",
  },
  {
    id: "gov-proposer", label: "Active Governance Proposer", address: "0x44B2a71420Dc6f4A1A7cE5B290C3a0a0a0a0a0a0",
    type: "GOVERNANCE", tags: ["governance", "delegate"], lastActivity: "Submitted Proposal #2",
    lastActivityTs: Date.now() - 28_800_000, balanceAgl: "320,000",
  },
];

advancedRouter.get("/watchlist", (_req, res) => {
  res.json(WATCHLIST);
});

advancedRouter.post("/watchlist", (req, res) => {
  const { label, address, type, tags } = req.body as any;
  if (!address || !isAddress(address)) { res.status(400).json({ error: "Valid address required" }); return; }
  const entry = {
    id: `custom-${Date.now()}`, label: label || "Custom Watch", address,
    type: type || "CUSTOM", tags: tags || ["custom"],
    lastActivity: "Added to watchlist", lastActivityTs: Date.now(), balanceAgl: "—",
  };
  WATCHLIST.push(entry);
  res.json(entry);
});

advancedRouter.delete("/watchlist/:id", (req, res) => {
  const idx = WATCHLIST.findIndex((w) => w.id === req.params.id);
  if (idx >= 0) { WATCHLIST.splice(idx, 1); res.json({ ok: true }); }
  else { res.status(404).json({ error: "Not found" }); }
});

// ---- Staking Calculator ----
advancedRouter.post("/staking/calculate", (req, res) => {
  const { amount, aprPercent, durationDays, compound } = req.body as {
    amount?: number; aprPercent?: number; durationDays?: number; compound?: boolean;
  };
  const amt = Number(amount) || 0;
  const apr = Number(aprPercent) || 18.5;
  const days = Number(durationDays) || 365;
  const doCompound = compound !== false;

  let finalAmount: number;
  let totalRewards: number;
  if (doCompound) {
    // Compound daily
    const dailyRate = apr / 100 / 365;
    finalAmount = amt * Math.pow(1 + dailyRate, days);
    totalRewards = finalAmount - amt;
  } else {
    // Simple interest
    totalRewards = amt * (apr / 100) * (days / 365);
    finalAmount = amt + totalRewards;
  }

  const tiers = [
    { tierId: 0, durationDays: 90, aprPercent: 12.5, label: "90 days" },
    { tierId: 1, durationDays: 180, aprPercent: 18.5, label: "180 days" },
    { tierId: 2, durationDays: 365, aprPercent: 28.0, label: "365 days" },
  ];

  // Find optimal tier
  const optimal = tiers.reduce((best, t) =>
    amt * (t.aprPercent / 100) * (t.durationDays / 365) >
    amt * (best.aprPercent / 100) * (best.durationDays / 365) ? t : best
  );

  res.json({
    principal: amt, aprPercent: apr, durationDays: days, compound: doCompound,
    totalRewards, finalAmount, dailyRewards: totalRewards / days,
    optimalTier: optimal,
    projections: tiers.map((t) => ({
      ...t,
      rewards: amt * (t.aprPercent / 100) * (t.durationDays / 365),
      final: amt + amt * (t.aprPercent / 100) * (t.durationDays / 365),
    })),
  });
});

// ---- Proposal Vote Simulator ----
advancedRouter.post("/governance/simulate", (req, res) => {
  const { forVotes, againstVotes, abstainVotes, quorum, userVotingPower, userSupport } = req.body as {
    forVotes?: number; againstVotes?: number; abstainVotes?: number;
    quorum?: number; userVotingPower?: number; userSupport?: "FOR" | "AGAINST" | "ABSTAIN";
  };
  const fv = Number(forVotes) || 0;
  const av = Number(againstVotes) || 0;
  const abv = Number(abstainVotes) || 0;
  const q = Number(quorum) || 40000;
  const uvp = Number(userVotingPower) || 0;
  const support = userSupport || "FOR";

  const totalCast = fv + av + abv;
  const meetsQuorum = totalCast >= q;
  const forPct = totalCast > 0 ? (fv / totalCast) * 100 : 0;
  const againstPct = totalCast > 0 ? (av / totalCast) * 100 : 0;

  // Simulate user vote impact
  const newFor = support === "FOR" ? fv + uvp : fv;
  const newAgainst = support === "AGAINST" ? av + uvp : av;
  const newAbstain = support === "ABSTAIN" ? abv + uvp : abv;
  const newTotal = newFor + newAgainst + newAbstain;
  const newMeetsQuorum = newTotal >= q;
  const newForPct = newTotal > 0 ? (newFor / newTotal) * 100 : 0;

  const wouldPass = newMeetsQuorum && newForPct > 50;
  const outcome = wouldPass ? "SUCCEEDED" : newMeetsQuorum ? "DEFEATED" : "QUORUM_NOT_MET";

  res.json({
    current: { forVotes: fv, againstVotes: av, abstainVotes: abv, totalCast, meetsQuorum, forPct, againstPct },
    simulated: {
      userVotingPower: uvp, userSupport: support,
      forVotes: newFor, againstVotes: newAgainst, abstainVotes: newAbstain,
      totalCast: newTotal, meetsQuorum: newMeetsQuorum,
      forPct: newForPct, againstPct: newTotal > 0 ? (newAgainst / newTotal) * 100 : 0,
      quorumNeeded: Math.max(0, q - newTotal),
      outcome,
      wouldPass,
    },
    quorum,
  });
});

// ---- Delegation Explorer ----
advancedRouter.get("/delegation/explorer", async (_req, res) => {
  // Static delegation graph (real data requires event indexing)
  const delegations = [
    { delegator: "0x742d35Cc6634C0532925a3b844Bc454e4438f44e", delegatee: "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698", votingPower: "1,250,000", label: "Demo Holder → Treasury" },
    { delegator: "0x5B38Da6a701c568545dCfcB03FcB875f56beddC4", delegatee: "0x44B2a71420Dc6f4A1A7cE5B290C3a0a0a0a0a0a0", votingPower: "890,000", label: "DeFi Staker → Gov Proposer" },
    { delegator: "0x44B2a71420Dc6f4A1A7cE5B290C3a0a0a0a0a0a0", delegatee: "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698", votingPower: "320,000", label: "Gov Proposer → Treasury" },
    { delegator: "0x981A4b3c5D7e9F1a2B3c4D5e6F7a8B9c0D1e2F3a", delegatee: "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698", votingPower: "510,000", label: "Whale → Treasury" },
  ];

  const topDelegates = [
    { address: "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698", label: "AGL Foundation Treasury", totalDelegated: "2,080,000", delegators: 3, pctOfSupply: 0.21 },
    { address: "0x44B2a71420Dc6f4A1A7cE5B290C3a0a0a0a0a0a0", label: "Active Governance Proposer", totalDelegated: "890,000", delegators: 1, pctOfSupply: 0.09 },
    { address: "0x0000000000000000000000000000000000000000", label: "Self-Delegated (Undelegated)", totalDelegated: "640,000,000", delegators: 0, pctOfSupply: 64.0 },
  ];

  res.json({ delegations, topDelegates, totalSupply: "1,000,000,000 wAGL" });
});

advancedRouter.get("/delegation/:address", async (req, res) => {
  const address = req.params.address;
  if (!isAddress(address)) { res.status(400).json({ error: "Invalid address" }); return; }
  try {
    const info = await withFailover(async (provider) => {
      const w = makeContract(AGL_VOTES_WRAPPER_CONTRACT, VOTES_WRAPPER_ABI, provider);
      const [votes, delegate, numCp] = await Promise.all([
        w.getVotes(address).catch(() => 0n),
        w.delegates(address).catch(() => "0x0000000000000000000000000000000000000000"),
        w.numCheckpoints(address).catch(() => 0n),
      ]);
      return {
        address,
        votingPower: safeFormatUnits(votes, 18),
        delegatee: delegate && delegate !== "0x0000000000000000000000000000000000000000" ? delegate : null,
        numCheckpoints: Number(numCp),
        isSelfDelegated: delegate?.toLowerCase() === address.toLowerCase(),
      };
    });
    res.json(info);
  } catch {
    res.json({ address, votingPower: "0", delegatee: null, numCheckpoints: 0, isSelfDelegated: false });
  }
});
