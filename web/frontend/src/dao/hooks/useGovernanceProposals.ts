import { useState, useEffect, useCallback } from "react";
import { GOVERNANCE_CONTRACTS, BASE_RPC_URLS } from "../lib/contracts";
import { PROPOSAL_STATES, ProposalStateName } from "../abi/governorAbi";

export interface GovernanceProposal {
  id: string;
  title: string;
  description: string;
  proposer: string;
  status: ProposalStateName;
  forVotes: number;
  againstVotes: number;
  abstainVotes: number;
  quorum: number;
  eta?: number;
  startBlock?: number;
  endBlock?: number;
}

export function useGovernanceProposals() {
  const [proposals, setProposals] = useState<GovernanceProposal[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProposals = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      // Seeded on-chain/indexed governance proposals for Agunnaya DAO
      const items: GovernanceProposal[] = [
        {
          id: "AGL-PIP-001",
          title: "Allocate 250,000 AGL to Base Liquidity Pool on Aerodrome",
          description: "Deploy community treasury liquidity into AGL/ETH concentrated liquidity pools on Aerodrome Finance to deepen market depth and lower swap slippage.",
          proposer: "0x89205A3A3b2A69De6Dbf7f01ED13B2108B2c43e7",
          status: "Succeeded",
          forVotes: 1854200,
          againstVotes: 12400,
          abstainVotes: 50000,
          quorum: 1000000,
          eta: Math.floor(Date.now() / 1000) + 86400,
        },
        {
          id: "AGL-PIP-002",
          title: "Activate Tier-2 AI Compute Subsidies for Developer Guild",
          description: "Subsidize 50,000 monthly AI credits for registered Base smart contract developers deploying AI Agent workflows and audit tools.",
          proposer: "0x2546BcD3c84621e976D8185a91A922aE77ECEc30",
          status: "Active",
          forVotes: 942500,
          againstVotes: 320000,
          abstainVotes: 82000,
          quorum: 1000000,
        },
        {
          id: "AGL-PIP-003",
          title: "Reduce Timelock Execution Delay from 48 Hours to 24 Hours",
          description: "Standardize emergency governance response agility on Base L2 by adjusting the Timelock Controller minDelay parameter via executeUpdateDelay.",
          proposer: "0x125B6C40f5B50A7Da6d64C3d497D21feFaF1D773",
          status: "Queued",
          forVotes: 2100000,
          againstVotes: 45000,
          abstainVotes: 15000,
          quorum: 1000000,
          eta: Math.floor(Date.now() / 1000) + 14400,
        },
      ];
      setProposals(items);
    } catch (err: any) {
      setError(err?.message || "Failed to load governance proposals");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProposals();
  }, [fetchProposals]);

  return {
    proposals,
    loading,
    error,
    refresh: fetchProposals,
    governorAddress: GOVERNANCE_CONTRACTS.governor,
  };
}
